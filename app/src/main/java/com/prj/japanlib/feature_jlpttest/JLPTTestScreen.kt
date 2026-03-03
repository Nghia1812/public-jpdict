package com.prj.japanlib.feature_jlpttest

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prj.japanlib.uistate.TestScreenUiState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.prj.domain.model.testscreen.BaseTestSection
import com.prj.domain.model.testscreen.JLPTTest
import com.prj.domain.model.testscreen.Level
import com.prj.domain.model.testscreen.Source
import com.prj.japanlib.feature_jlpttest.components.QuestionCard
import com.prj.japanlib.common.utils.formatTime
import com.prj.japanlib.feature_dictionary.components.ErrorScreen
import com.prj.japanlib.feature_jlpttest.components.AudioPlayerManager
import com.prj.japanlib.feature_jlpttest.components.TestQuestionShimmer
import com.prj.japanlib.feature_jlpttest.viewmodel.implementation.JLPTTestViewModel
import com.prj.japanlib.ui.theme.sectionProgressBar
import com.prj.japanlib.ui.theme.progressTrack
import com.prj.japanlib.R

@Composable
fun JLPTTestScreen(
    viewModel: JLPTTestViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit,
    level: Level,
    id: String,
    skill: String
) {
    val testQuestionUiState by viewModel.testQuestions.collectAsStateWithLifecycle()
    val selectedAnswers by viewModel.selectedAnswers.collectAsStateWithLifecycle()
    val timeValue by viewModel.timer.collectAsStateWithLifecycle()
    val source = Source.CUSTOM
    val testState by viewModel.testState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val audioPlayerManager = remember { AudioPlayerManager(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(level, id, skill) {
        viewModel.getTestQuestions(source, level, id, skill)
    }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayerManager.release()
        }
    }

    // Make sure behavior of pressing system back button is same as pressing app back button
    BackHandler(enabled = true) {
        onNavigateBack()
    }

    // only auto-pause
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.pauseTest()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (testState == JLPTTestViewModel.TestState.RESET) {
                viewModel.resetExam(level)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        JLPTTestScreenContent(
            timeValue = timeValue,
            testQuestionUiState = testQuestionUiState,
            selectedAnswers = selectedAnswers,
            onAnswerSelect = viewModel::selectAnswer,
            onSubmit = viewModel::submitExam,
            onRetry = { viewModel.resetExam(level) },
            onNavigateBack = {
                onNavigateBack()
            },
            audioPlayerManager = audioPlayerManager,
            onNavigateToResult = onNavigateToResult
        )

        // Show pause overlay when paused
        if (testState == JLPTTestViewModel.TestState.PAUSED) {
            PauseOverlay(
                timeValue = timeValue,
                onResume = { viewModel.resumeTest() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JLPTTestScreenContent(
    timeValue: Long = 0,
    testQuestionUiState: TestScreenUiState<JLPTTest>,
    selectedAnswers: Map<Int, Int>,
    onAnswerSelect: (Int, Int) -> Unit,
    onSubmit: () -> Unit,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
    audioPlayerManager: AudioPlayerManager,
    onNavigateToResult: () -> Unit
) {
    Scaffold(
        topBar = {
            JLPTTopAppBar(
                title = stringResource(id = R.string.jlpt_test_title),
                timer = timeValue.formatTime(),
                onCloseClick = onNavigateBack
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (testQuestionUiState) {
                is TestScreenUiState.Loading -> {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        repeat(5) {
                            TestQuestionShimmer()
                        }
                    }
                }

                is TestScreenUiState.Success -> {
                    val sectionsOfQuestions = testQuestionUiState.data.sections
                    TestQuestionsScreen(
                        sectionsOfQuestions = sectionsOfQuestions,
                        selectedAnswers = selectedAnswers,
                        onAnswerSelect = onAnswerSelect,
                        onSubmit = {
                            onSubmit()
                            onNavigateToResult()
                        },
                        audioPlayerManager = audioPlayerManager
                    )
                }

                is TestScreenUiState.Error -> {
                    ErrorScreen(
                        errorMessage = stringResource(id = R.string.error_loading_test),
                        onRetry = onRetry
                    )
                }

                else -> {
                    // No internet -> Already handle with dialog
                }
            }
        }
    }
}

@Composable
fun TestQuestionsScreen(
    sectionsOfQuestions: List<BaseTestSection>,
    selectedAnswers: Map<Int, Int>,
    onAnswerSelect: (Int, Int) -> Unit,
    onSubmit: () -> Unit,
    audioPlayerManager: AudioPlayerManager,
) {
    val listState = rememberLazyListState()

    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex =
                layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            totalItems > 0 && lastVisibleItemIndex == totalItems - 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val numberOfQuestions = sectionsOfQuestions.sumOf {
            it.questions.size
        }

        TestProgressBar(
            answeredCount = selectedAnswers.size,
            totalCount = numberOfQuestions
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            sectionsOfQuestions.forEach { section ->
                item {
                    Text(
                        text = section.description,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(section.questions) { question ->
                    QuestionCard(
                        question = question,
                        selectedIndex = selectedAnswers[question.number],
                        onSelect = { index ->
                            onAnswerSelect(question.number, index)
                        },
                        audioPlayerManager = audioPlayerManager,
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedVisibility(
            visible = isAtBottom,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.submit_test),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


@Composable
fun TestProgressBar(
    answeredCount: Int,
    totalCount: Int
) {
    Column {
        Text(
            text = stringResource(id = R.string.answered_progress, answeredCount, totalCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = if (totalCount > 0) answeredCount.toFloat() / totalCount else 0f,
            color = sectionProgressBar,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun JLPTTopAppBar(
    title: String,
    timer: String,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .height(56.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Close button
            IconButton(onClick = onCloseClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.close_dialog),
                    tint = Color.White
                )
            }

            // Title (centered)
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                textAlign = TextAlign.Start
            )

            // Timer chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = timer,
                    color = progressTrack,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun PauseOverlay(
    timeValue: Long,
    onResume: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onResume()
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .widthIn(max = 400.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E293B)
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Paused",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF10B981)
                )

                Text(
                    text = stringResource(id = R.string.test_paused),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = stringResource(id = R.string.test_paused_message),
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                // Show current time
                Text(
                    text = stringResource(id = R.string.timer_label, timeValue.formatTime()),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF10B981)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.tap_to_continue),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Text(
                    text = stringResource(id = R.string.progress_saved),
                    fontSize = 12.sp,
                    color = Color.Gray.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
