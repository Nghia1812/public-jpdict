package com.prj.japanlib.feature_jlpttest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prj.japanlib.R
import com.prj.japanlib.feature_dictionary.components.ErrorScreen
import com.prj.japanlib.feature_jlpttest.components.AudioPlayerManager
import com.prj.japanlib.feature_jlpttest.components.ResultQuestionCard
import com.prj.japanlib.feature_jlpttest.components.TestQuestionShimmer
import com.prj.japanlib.feature_jlpttest.viewmodel.implementation.JLPTTestViewModel
import com.prj.japanlib.uistate.TestScreenUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewAnswersScreen(
    viewModel: JLPTTestViewModel,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
) {
    val selectedAnswers by viewModel.selectedAnswers.collectAsStateWithLifecycle()
    val score by viewModel.score.collectAsStateWithLifecycle()
    val questionsState by viewModel.testQuestions.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val audioPlayerManager = remember { AudioPlayerManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayerManager.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(id = R.string.review_answers_title),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (questionsState is TestScreenUiState.Success) {
                            val test = (questionsState as TestScreenUiState.Success).data
                            val numberOfQuestions = test.sections.sumOf { it.questions.size }
                            Text(
                                text = stringResource(id = R.string.score_format, score ?: 0, numberOfQuestions),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        when (questionsState) {
            is TestScreenUiState.Success -> {
                val test = (questionsState as TestScreenUiState.Success).data
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(padding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    test.sections.forEach { section ->
                        item {
                            Text(
                                text = section.description,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        items(section.questions) { question ->
                            ResultQuestionCard(
                                question = question,
                                selectedIndex = selectedAnswers[question.number],
                                audioPlayerManager = audioPlayerManager
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }

            is TestScreenUiState.Error -> {
                ErrorScreen(
                    errorMessage = stringResource(id = R.string.error_loading_test),
                    onRetry = onRetry
                )
            }

            is TestScreenUiState.Loading -> {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    repeat(5) {
                        TestQuestionShimmer()
                    }
                }
            }

            else -> {
                // NOP
            }
        }
    }
}
