package com.prj.japanlib.feature_jlpttest

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.model.testscreen.LearningState
import com.prj.japanlib.R
import com.prj.japanlib.feature_jlpttest.viewmodel.implementation.FlashcardDetailViewModel
import com.prj.domain.model.testscreen.WordListType
import com.prj.japanlib.common.components.ActionButton
import com.prj.japanlib.common.components.AdaptiveText
import com.prj.japanlib.common.utils.SpeechUtils.rememberTextToSpeech
import com.prj.japanlib.feature_jlpttest.components.CongratDialog
import com.prj.japanlib.ui.theme.forgotColor
import com.prj.japanlib.ui.theme.rememberedColor
import com.prj.japanlib.ui.theme.surfaceContainerDeepBlue
import com.prj.japanlib.uistate.TestScreenUiState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.HazeMaterials
import timber.log.Timber
import kotlin.math.roundToInt


@Composable
fun FlashcardDetailsScreen(
    listId: String,
    listType: WordListType,
    learningState: LearningState,
    onNavigateBack: () -> Unit
) {
    val flashcardViewModel = hiltViewModel<FlashcardDetailViewModel>()
    val wordsUiState by flashcardViewModel.wordsUiState.collectAsState()
    val isShowMeaningFirst by flashcardViewModel.showMeaning.collectAsState()

    LaunchedEffect(listId) {
        flashcardViewModel.getWordsByLearningState(listId, learningState, listType)
    }
    if (wordsUiState is TestScreenUiState.Success) {
        FlashcardDetailsScreenContent(
            words = (wordsUiState as TestScreenUiState.Success<List<JapaneseWord>>).data,
            isShowMeaningFirst = isShowMeaningFirst,
            onNavigateBack = onNavigateBack,
            onRememberClick = { entryId ->
                Timber.d("Remember clicked for entryId: $entryId, listId: $listId")
                flashcardViewModel.updateLearningState(
                    listId,
                    entryId,
                    LearningState.REMEMBERED,
                    listType
                )
            },
            onForgotClick = { entryId ->
                flashcardViewModel.updateLearningState(
                    listId,
                    entryId,
                    LearningState.FORGOT,
                    listType
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardTopBar(
    progress: String,
    onNavigateBack: () -> Unit,
) {
    TopAppBar(
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = progress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun FlashcardDetailsScreenContent(
    words: List<JapaneseWord>,
    isShowMeaningFirst: Boolean,
    onNavigateBack: () -> Unit,
    onRememberClick: (Int) -> Unit,
    onForgotClick: (Int) -> Unit
) {
    var currentIndex by rememberSaveable() { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    val currentWord = words.getOrNull(currentIndex)
    var showDialog by remember { mutableStateOf(false) }
    val tts = rememberTextToSpeech()
    var isSpeaking by remember { mutableStateOf(false) }
    val hazeState = remember { HazeState() }

    // Listen for TTS playback completion
    LaunchedEffect(tts.value) {
        tts.value?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isSpeaking = true
            }

            override fun onDone(utteranceId: String?) {
                isSpeaking = false
            }

            override fun onError(utteranceId: String?) {
                isSpeaking = false
            }

            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                isSpeaking = false
            }
        })
    }

    val onPlayAudio: (JapaneseWord) -> Unit = onPlayAudio@{ wordEntity ->
        val engine = tts.value ?: return@onPlayAudio

        if (engine.isSpeaking) {
            engine.stop()
        } else {
            engine.speak(
                wordEntity.reading,
                TextToSpeech.QUEUE_FLUSH,
                null,
                wordEntity.id.toString()
            )
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
            CongratDialog(
                onGoBack = {
                    showDialog = false
                    onNavigateBack()
                },
                onRetry = {
                    showDialog = false
                    currentIndex = 0
                    isFlipped = false
                }
            )
        }
    }
    Scaffold(
        topBar = {
            FlashcardTopBar(
                progress = stringResource(R.string.progress_ratio, currentIndex + 1, words.size),
                onNavigateBack = onNavigateBack,
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Progress bar
            LinearProgressIndicator(
                progress = (currentIndex + 1).toFloat() / words.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.primary
            )

            // Flashcard with swipe
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                currentWord?.let { word ->
                    FlashcardWithSwipe(
                        word = word,
                        isShowMeaningFirst = isShowMeaningFirst,
                        isFlipped = isFlipped,
                        onFlip = { isFlipped = !isFlipped },
                        onSwipeLeft = {
                            if (currentIndex == words.size - 1) {
                                showDialog = true
                                return@FlashcardWithSwipe
                            }
                            if (currentIndex < words.size - 1) {
                                currentIndex++
                                isFlipped = false
                            }
                        },
                        onSwipeRight = {
                            if (currentIndex > 0) {
                                currentIndex--
                                isFlipped = false
                            }
                        },
                        onPlayAudio = onPlayAudio
                    )
                }
            }

            LearnButtonsSections(
                onRememberClick = {
                    currentWord?.let { word ->
                        onRememberClick(word.id)
                        // Move to next word
                        if (currentIndex == words.size - 1) {
                            showDialog = true
                            return@LearnButtonsSections
                        }
                        if (currentIndex < words.size - 1) {
                            currentIndex++
                            isFlipped = false
                        }
                    }
                },
                onForgotClick = {
                    currentWord?.let { word ->
                        onForgotClick(word.id)
                        if (currentIndex == words.size - 1) {
                            showDialog = true
                            return@LearnButtonsSections
                        }
                        // Move to next word
                        if (currentIndex < words.size - 1) {
                            currentIndex++
                            isFlipped = false
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FlashcardWithSwipe(
    isShowMeaningFirst: Boolean,
    word: JapaneseWord,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onPlayAudio: (JapaneseWord) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(400), label = ""
    )

    val swipeState = rememberSwipeableState(initialValue = 0)
    val anchors = mapOf(
        -1000f to -1, // Swipe left
        0f to 0,      // Center
        1000f to 1    // Swipe right
    )

    LaunchedEffect(swipeState.currentValue) {
        when (swipeState.currentValue) {
            -1 -> {
                onSwipeLeft()
                swipeState.snapTo(0)
            }
            1 -> {
                onSwipeRight()
                swipeState.snapTo(0)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .aspectRatio(0.7f)
            .swipeable(
                state = swipeState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                orientation = Orientation.Horizontal
            )
            .offset { IntOffset(swipeState.offset.value.roundToInt(), 0) }
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .clickable(onClick = onFlip),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            // Internal container to handle content flipping independently of the card's rotation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // If card is rotated more than 90 degrees, looking at the back.
                        // Counter-rotate the content by 180 degrees so it's not mirrored.
                        if (rotation > 90f) {
                            rotationY = 180f
                        }
                    }
            ) {
                if (rotation <= 90f) {
                    // Front of the physical card
                    if (isShowMeaningFirst) {
                        FlashcardBack(word = word)
                    } else {
                        FlashcardFront(word = word, onPlayAudio = onPlayAudio)
                    }
                } else {
                    // Back of the physical card
                    if (isShowMeaningFirst) {
                        FlashcardFront(word = word, onPlayAudio = onPlayAudio)
                    } else {
                        FlashcardBack(word = word)
                    }
                }
            }
        }
    }
}

@Composable
fun FlashcardFront(word: JapaneseWord, onPlayAudio: (JapaneseWord) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceContainerDeepBlue)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = { onPlayAudio(word) }) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.speaker_on),
                contentDescription = stringResource(R.string.play_audio)
            )
        }
        Text(
            text = stringResource(R.string.word_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        AdaptiveText(
            text = word.kanji ?: word.reading ?: "",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            maxFontSize = 64.sp,
            minFontSize = 24.sp,
            scaleFactor = 1.0f
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = word.reading ?: "",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.tap_to_flip),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun FlashcardBack(word: JapaneseWord) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.meaning_label),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        word.meaning?.let {
            AdaptiveText(
                text = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 5,
                maxFontSize = 64.sp,
                minFontSize = 24.sp,
                scaleFactor = 1.0f
            )
        }

        word.reading?.let { reading ->
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.reading_label),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = reading,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LearnButtonsSections(
    onRememberClick: () -> Unit,
    onForgotClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionButton(
            text = stringResource(R.string.forgot),
            onClick = onForgotClick,
            containerColor = forgotColor,
            borderColor = forgotColor,
            contentColor = forgotColor,
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            text = stringResource(R.string.remember),
            onClick = onRememberClick,
            containerColor = rememberedColor,
            modifier = Modifier.weight(1f)
        )
    }
}
