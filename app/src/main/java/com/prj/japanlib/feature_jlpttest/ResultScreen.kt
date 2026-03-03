package com.prj.japanlib.feature_jlpttest

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prj.domain.model.testscreen.JLPTTest
import com.prj.japanlib.R
import com.prj.japanlib.common.utils.formatTime
import com.prj.japanlib.feature_dictionary.components.ErrorScreen
import com.prj.japanlib.feature_jlpttest.components.TestQuestionShimmer
import com.prj.japanlib.feature_jlpttest.viewmodel.implementation.JLPTTestViewModel
import com.prj.japanlib.ui.theme.darkBackground
import com.prj.japanlib.ui.theme.darkSurface
import com.prj.japanlib.ui.theme.primaryContainerDark
import com.prj.japanlib.ui.theme.successGreen
import com.prj.japanlib.uistate.TestScreenUiState

@Composable
fun ResultScreen(
    viewModel: JLPTTestViewModel,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    onReviewAnswers: () -> Unit,
) {
    val testQuestionUiState by viewModel.testQuestions.collectAsStateWithLifecycle()
    val selectedAnswers by viewModel.selectedAnswers.collectAsState()
    val score by viewModel.score.collectAsState()
    val timeTaken by viewModel.timeTaken.collectAsStateWithLifecycle()

    ResultScreenContent(
        testQuestionUiState = testQuestionUiState,
        selectedAnswers = selectedAnswers,
        score = score,
        timeValue = timeTaken,
        onRetry = onRetry,
        onReviewAnswers = onReviewAnswers,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreenContent(
    testQuestionUiState: TestScreenUiState<JLPTTest>,
    selectedAnswers: Map<Int, Int>,
    score: Int?,
    timeValue: Long,
    onRetry: () -> Unit,
    onReviewAnswers: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = darkSurface,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .height(56.dp)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close_dialog),
                            tint = Color.White
                        )
                    }

                    Text(
                        text = stringResource(R.string.result_screen_title),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        containerColor = darkBackground,
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
                    var numberOfQuestions = 0
                    for (section in sectionsOfQuestions) {
                        numberOfQuestions += section.questions.size
                    }

                    ResultContent(
                        selectedAnswers = selectedAnswers,
                        score = score ?: 0,
                        total = numberOfQuestions,
                        timeValue = timeValue,
                        onRetry = onRetry,
                        onReviewAnswers = onReviewAnswers,
                        onNavigateBack = onNavigateBack
                    )
                }

                is TestScreenUiState.Error -> {
                    ErrorScreen(
                        errorMessage = stringResource(R.string.result_failed_to_load),
                        onRetry = onRetry
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ResultContent(
    selectedAnswers: Map<Int, Int>,
    score: Int,
    total: Int,
    timeValue: Long,
    onRetry: () -> Unit,
    onReviewAnswers: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = stringResource(R.string.result_congratulations),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.result_summary_subtitle),
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Circular Progress
        val percentage = if (total > 0) (score * 100) / total else 0
        CircularScoreIndicator(
            percentage = percentage
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = stringResource(R.string.result_correct_answers_label),
                value = "$score/$total"
            )

            StatItem(
                label = stringResource(R.string.result_time_taken_label),
                value = timeValue.formatTime()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Performance by Section
        Text(
            text = stringResource(R.string.result_performance_by_section),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Review Answers Button
        Button(
            onClick = onReviewAnswers,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = successGreen
            ),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(
                text = stringResource(R.string.result_review_answers_button),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Retake Test Button
        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = successGreen
            ),
            border = androidx.compose.foundation.BorderStroke(2.dp, successGreen),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(
                text = stringResource(R.string.result_retake_test_button),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Return to Test List
        TextButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.result_return_to_list_button),
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun CircularScoreIndicator(
    percentage: Int,
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val currentPercentage by animateFloatAsState(
        targetValue = if (animationPlayed) percentage.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "percentage"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp)
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            // Background circle
            drawArc(
                color = primaryContainerDark,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )

            // Progress circle
            drawArc(
                color = successGreen,
                startAngle = -90f,
                sweepAngle = (360f * currentPercentage) / 100f,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${currentPercentage.toInt()}%",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = stringResource(R.string.result_accuracy_label),
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
