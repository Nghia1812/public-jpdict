package com.prj.japanlib.feature_jlpttest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prj.domain.model.testscreen.ListWordCountWithState
import com.prj.domain.model.testscreen.WordListType
import com.prj.japanlib.R
import com.prj.japanlib.common.components.CustomGlassButton
import com.prj.japanlib.common.utils.ClickEventDebouncer
import com.prj.japanlib.feature_jlpttest.components.LearningPreferencesDialog
import com.prj.japanlib.feature_jlpttest.components.OverviewShimmer
import com.prj.japanlib.feature_jlpttest.components.StatisticCard
import com.prj.japanlib.feature_jlpttest.viewmodel.implementation.FlashcardOverviewViewModel
import com.prj.japanlib.ui.theme.forgotColor
import com.prj.japanlib.ui.theme.forgotContainerColor
import com.prj.japanlib.ui.theme.rememberedColor
import com.prj.japanlib.ui.theme.rememberedContainerColor
import com.prj.japanlib.uistate.TestScreenUiState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

// ═══════════════════════════════════════════════════════════
// STATEFUL COMPOSABLE
// ═══════════════════════════════════════════════════════════
@Composable
fun FlashcardOverviewScreen(
    listId: String,
    listType: WordListType,
    onNavigateBack: () -> Unit,
    onStartFlashcards: () -> Unit,
    onReviewMissedWords: () -> Unit,
    onLearnRemaining: () -> Unit,
    flashcardOverviewViewModel: FlashcardOverviewViewModel = hiltViewModel()
) {
    // Load data based on listId and listType
    LaunchedEffect(listId, listType) {
        flashcardOverviewViewModel.loadLearningStatus(listId, listType)
    }

    val uiState by flashcardOverviewViewModel.learningStateUiState.collectAsStateWithLifecycle()
    val shuffleWords by flashcardOverviewViewModel.shuffleWords.collectAsStateWithLifecycle()
    val showMeaningFirst by flashcardOverviewViewModel.showMeaningFirst.collectAsStateWithLifecycle()
    val mDebouncer = remember { ClickEventDebouncer() }

    FlashcardOverviewScreenContent(
        uiState = uiState,
        shuffleWords = shuffleWords,
        showMeaningFirst = showMeaningFirst,
        onNavigateBack = {
            mDebouncer.processClick { onNavigateBack() }
        },
        onStartFlashcards = {
            mDebouncer.processClick { onStartFlashcards() }
        },
        onReviewMissedWords = {
            mDebouncer.processClick { onReviewMissedWords() }
        },
        onLearnRemaining = {
            mDebouncer.processClick { onLearnRemaining() }
        },
        onPreferencesChanged = { shuffle, meaningFirst ->
            if (shuffle != flashcardOverviewViewModel.shuffleWords.value ||
                meaningFirst != flashcardOverviewViewModel.showMeaningFirst.value
            ) {
                flashcardOverviewViewModel.setShuffleWordsPreference(shuffle)
                flashcardOverviewViewModel.setShowMeaningFirstPreference(meaningFirst)
            }
        }
    )
}

// ═══════════════════════════════════════════════════════════
// STATELESS COMPOSABLE
// ═══════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun FlashcardOverviewScreenContent(
    uiState: TestScreenUiState<ListWordCountWithState>,
    shuffleWords: Boolean,
    showMeaningFirst: Boolean,
    onNavigateBack: () -> Unit,
    onStartFlashcards: () -> Unit,
    onReviewMissedWords: () -> Unit,
    onLearnRemaining: () -> Unit,
    onPreferencesChanged: (shuffleWords: Boolean, showMeaningFirst: Boolean) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val hazeState = remember { HazeState() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.learning_status),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        when (uiState) {
            is TestScreenUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    //Shimmer
                    OverviewShimmer()
                }
            }

            is TestScreenUiState.Success -> {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(scrollState)
                ) {
                    Spacer(Modifier.height(24.dp))

                    // Icon and Title
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_remember),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        Text(
                            text = uiState.data.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    // Statistics Cards
                    StatisticsSection(
                        totalWords = uiState.data.totalCount,
                        rememberedWords = uiState.data.rememberedCount,
                        notRememberedWords = uiState.data.forgotCount
                    )

                    Spacer(Modifier.height(32.dp))

                    // Action Buttons
                    CustomGlassButton(
                        onClick = onStartFlashcards,
                        enabled = true,
                        icon = Icons.Default.PlayArrow,
                        text = stringResource(R.string.start_flashcards),
                        isPrimary = true,
                    )

                    Spacer(Modifier.height(16.dp))

                    CustomGlassButton(
                        onClick = onLearnRemaining,
                        enabled = (uiState.data.rememberedCount + uiState.data.forgotCount) != uiState.data.totalCount,
                        icon = Icons.Default.KeyboardArrowRight,
                        text = stringResource(R.string.learn_remaining_words),
                        isPrimary = (uiState.data.rememberedCount + uiState.data.forgotCount) != uiState.data.totalCount
                    )

                    Spacer(Modifier.height(16.dp))

                    CustomGlassButton(
                        onClick = onReviewMissedWords,
                        enabled = uiState.data.forgotCount != 0,
                        icon = Icons.Default.Refresh,
                        text = stringResource(R.string.review_forgot_words),
                        isPrimary = uiState.data.forgotCount != 0,
                    )

                    Spacer(Modifier.height(24.dp))
                }
            }

            is TestScreenUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.message ?: stringResource(R.string.unknown_error),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { /* Retry */ }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }

            is TestScreenUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_data_available),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            TestScreenUiState.NoInternet -> {}
        }
    }
    if (showDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeChild(
                    state = hazeState,
                    style = HazeMaterials.ultraThin()
                )
                .background(Color.White.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            LearningPreferencesDialog(
                onDismiss = { showDialog = false },
                initialShuffleWords = shuffleWords,
                initialShowMeaningFirst = showMeaningFirst,
                onPreferencesChanged = onPreferencesChanged
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SUB-COMPONENTS
// ══════════════════════════════════════════════════════════

@Composable
private fun StatisticsSection(
    totalWords: Int,
    rememberedWords: Int,
    notRememberedWords: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Total Words
        StatisticCard(
            icon = Icons.Default.List,
            iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            label = stringResource(R.string.total_words),
            value = totalWords.toString(),
            backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )

        // Remembered
        StatisticCard(
            icon = Icons.Default.CheckCircle,
            iconTint = rememberedColor,
            label = stringResource(R.string.remembered),
            value = rememberedWords.toString(),
            backgroundColor = rememberedContainerColor
        )

        // Not Remembered
        StatisticCard(
            icon = Icons.Default.Warning,
            iconTint = forgotColor,
            label = stringResource(R.string.not_remembered),
            value = notRememberedWords.toString(),
            backgroundColor = forgotContainerColor
        )
    }
}

@Preview
@Composable
fun FlashcardOverviewScreenPreview() {
    FlashcardOverviewScreenContent(
        uiState = TestScreenUiState.Empty,
        shuffleWords = true,
        showMeaningFirst = true,
        onNavigateBack = {},
        onStartFlashcards = {},
        onReviewMissedWords = {},
        onLearnRemaining = {},
        onPreferencesChanged = { _, _ -> },
    )

}
