package com.prj.japanlib.feature_dictionary

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prj.domain.model.dictionaryscreen.CustomWordListWithEntries
import com.prj.domain.model.dictionaryscreen.ExampleWithFurigana
import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.model.dictionaryscreen.KanjiDetail
import com.prj.domain.model.dictionaryscreen.Token
import com.prj.japanlib.R
import com.prj.japanlib.common.TextUtils
import com.prj.japanlib.common.components.shimmer
import com.prj.japanlib.common.utils.ClickEventDebouncer
import com.prj.japanlib.common.utils.SpeechUtils.rememberTextToSpeech
import com.prj.japanlib.common.utils.extractKanji
import com.prj.japanlib.feature_dictionary.components.ExpandableCard
import com.prj.japanlib.feature_dictionary.components.KanjiDetailItem
import com.prj.japanlib.feature_dictionary.components.SaveWordModal
import com.prj.japanlib.feature_dictionary.viewmodel.implemetations.WordDetailViewModel
import com.prj.japanlib.ui.theme.JapanlibTheme
import com.prj.japanlib.uistate.DictionaryUiState
import timber.log.Timber

@Composable
fun WordDetailScreen(
    wordId: Int,
    wordDetailViewModel: WordDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onRelatedWordClick: (Int) -> Unit
) {
    val examples by wordDetailViewModel.examples.collectAsStateWithLifecycle()
    val wordEntity by wordDetailViewModel.word.collectAsStateWithLifecycle()
    val customLists by wordDetailViewModel.customWordList.collectAsStateWithLifecycle()
    var showSheet by remember { mutableStateOf(false) }
    val kanjiDetails by wordDetailViewModel.kanjiDetail.collectAsStateWithLifecycle()
    val relatedWords by wordDetailViewModel.relatedWords.collectAsStateWithLifecycle()
    val mDebouncer = remember { ClickEventDebouncer() }

    LaunchedEffect(wordId) {
        wordDetailViewModel.getWord(wordId)
        wordDetailViewModel.loadExamples(wordId)
        wordDetailViewModel.getAllCustomLists()
    }
    LaunchedEffect(wordEntity) {
        if (wordEntity.kanji != null) {
            val listKanji: List<String> = extractKanji(wordEntity.kanji!!)
            wordDetailViewModel.getKanjiInfo(listKanji)
            wordDetailViewModel.getRelatedWords(wordId, listKanji)
        }
    }
    WordDetailScreenContent(
        showSheet = showSheet,
        wordEntity = wordEntity,
        examplesUiState = examples,
        customLists = customLists,
        kanjiDetails = kanjiDetails,
        relatedWords = relatedWords,
        onBookmark = { showSheet = true },
        onDismissModal = { showSheet = false },
        onCreateNewFolder = { wordDetailViewModel.createNewCustomList(it) },
        onFolderSelected = {
            mDebouncer.processClick {
                if (it.entries.contains(wordEntity)) {
                    Timber.d("WordDetailScreen: Removing word from custom list")
                    wordDetailViewModel.removeWordFromCustomList(it.list.listId, wordId)
                } else {
                    Timber.d("WordDetailScreen: Adding word to custom list")
                    wordDetailViewModel.addWordToCustomList(it.list.listId, wordId)
                }
            }
        },
        onBack = onBack,
        onRelatedWordClick = {
            mDebouncer.processClick { onRelatedWordClick(it) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreenContent(
    showSheet: Boolean,
    wordEntity: JapaneseWord,
    examplesUiState: DictionaryUiState<List<ExampleWithFurigana>>,
    customLists: DictionaryUiState<List<CustomWordListWithEntries>>,
    kanjiDetails: DictionaryUiState<List<KanjiDetail>>,
    relatedWords: DictionaryUiState<List<JapaneseWord>>,
    onBack: () -> Unit = {},
    onBookmark: () -> Unit = {},
    onPlayAudio: () -> Unit = {},
    onRelatedWordClick: (Int) -> Unit = {},
    onDismissModal: () -> Unit = {},
    onCreateNewFolder: (String) -> Unit = {},
    onFolderSelected: (CustomWordListWithEntries) -> Unit = {},
) {
    val cardColor = MaterialTheme.colorScheme.onSecondary
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val blueAccent = MaterialTheme.colorScheme.primary
    val tts = rememberTextToSpeech()
    var isSpeaking by remember { mutableStateOf(false) }
    var isDefExpanded by remember { mutableStateOf(false) }
    var isKanjiDetailExpanded by remember { mutableStateOf(false) }
    var isExampleExpanded by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }
                    Text(
                        stringResource(R.string.screen_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    IconButton(onClick = onBookmark) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.bookmark_ic),
                            contentDescription = "Bookmark",
                            tint = textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(top = 10.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 30.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (wordEntity.kanji != null) {
                            wordEntity.kanji
                        } else {
                            wordEntity.reading
                        }?.let {
                            Text(
                                it,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                lineHeight = 44.sp
                            )
                        }
                        wordEntity.reading?.let {
                            Text(
                                it,
                                fontSize = 16.sp,
                                color = textSecondary
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(100))
                            .background(blueAccent.copy(alpha = 0.15f))
                            .clickable { onPlayAudio() },
                        contentAlignment = Alignment.Center
                    ) {
                        // Play Audio Button
                        IconButton(
                            onClick = {
                                val engine = tts.value ?: return@IconButton
                                if (engine.isSpeaking) {
                                    engine.stop()
                                } else {
                                    engine.speak(
                                        wordEntity.reading,
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        wordEntity.id.toString() // utteranceId
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(
                                    if (isSpeaking) R.drawable.speaker_on else R.drawable.speaker_off
                                ),
                                contentDescription = null
                            )
                        }
                    }
                }
            }

            item {
                // Word type badge
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(cardColor)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    wordEntity.type?.let {
                        Text(
                            it.substringBefore(","),
                            color = blueAccent,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }

            item {
                // Expandable Sections
                ExpandableCard(
                    title = stringResource(R.string.definition_card),
                    cardColor = cardColor,
                    textColor = textPrimary,
                    expanded = isDefExpanded,
                    onExpand = { isDefExpanded = !isDefExpanded }
                ) {
                    wordEntity.meaning?.let { Text(it, color = textSecondary, fontSize = 12.sp) }
                }
                Spacer(Modifier.height(10.dp))
            }

            item {
                ExpandableCard(
                    title = stringResource(R.string.example_card),
                    cardColor = cardColor,
                    textColor = textPrimary,
                    expanded = isExampleExpanded,
                    onExpand = { isExampleExpanded = !isExampleExpanded }
                ) {
                    when (examplesUiState) {
                        is DictionaryUiState.Empty -> {
                            Text(stringResource(R.string.no_example), color = textSecondary,  fontSize = 12.sp)
                        }

                        is DictionaryUiState.Loading -> {
                            Text(stringResource(R.string.loading_example), color = textSecondary, fontSize = 12.sp)
                        }

                        is DictionaryUiState.Error -> {
                            Text(stringResource(R.string.error_example), color = textSecondary, fontSize = 12.sp)
                        }

                        is DictionaryUiState.Success -> {
                            val examples = examplesUiState.data
                            if (examples.isEmpty()) {
                                Text(stringResource(R.string.no_example), color = textSecondary, fontSize = 12.sp)
                            } else {
                                Column {
                                    examples.forEach { example ->
                                        ExampleItem(example, textSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            item {
                ExpandableCard(
                    title = stringResource(R.string.kanji_card),
                    cardColor = cardColor,
                    textColor = textPrimary,
                    expanded = isKanjiDetailExpanded,
                    onExpand = { isKanjiDetailExpanded = !isKanjiDetailExpanded }
                ) {
                    when (kanjiDetails) {
                        is DictionaryUiState.Success -> {
                            Column {
                                kanjiDetails.data.forEach {
                                    KanjiDetailItem(it)
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        }
                        else -> {
                            Text(
                                stringResource(R.string.no_kanji),
                                color = textPrimary,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
            }

            item {
                // Related Words
                Text(
                    stringResource(R.string.related_words),
                    color = textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                when (relatedWords) {
                    is DictionaryUiState.Success -> {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(relatedWords.data) {
                                RelatedWordCard(
                                    word = it,
                                    onClick = { onRelatedWordClick(it.id) },
                                    cardColor = cardColor,
                                    textPrimary = textPrimary,
                                    textSecondary = textSecondary
                                )
                            }
                        }
                    }
                    is DictionaryUiState.Loading -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            repeat(4) {
                                RelatedWordCardShimmer()
                            }
                        }
                    }

                    is DictionaryUiState.Error -> {
                        Text(stringResource(R.string.error_related_words))
                    }

                    is DictionaryUiState.Empty -> {
                        Text(stringResource(R.string.error_related_words))
                    }
                }
            }
        }
    }

    if (showSheet) {
        when (customLists) {
            is DictionaryUiState.Success -> {
                SaveWordModal(
                    word = wordEntity,
                    folders = customLists.data,
                    onFolderSelected = onFolderSelected,
                    onCreateNewFolder = onCreateNewFolder,
                    onDismiss = onDismissModal
                )
            }
            is DictionaryUiState.Loading -> {
                SaveWordModal(
                    word = wordEntity,
                    folders = emptyList(), // Pass an empty list
                    onFolderSelected = onFolderSelected,
                    onCreateNewFolder = onCreateNewFolder,
                    onDismiss = onDismissModal,
                    isLoading = true
                )
            }
            is DictionaryUiState.Error -> {
                // Show an error state inside the modal.
                ModalBottomSheet(onDismissRequest = onDismissModal) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp), contentAlignment = Alignment.Center) {
                        customLists.message?.let { Text(it) }
                    }
                    Timber.e("Error WordDetailScreen:" + customLists.message)
                }
            }
            is DictionaryUiState.Empty -> {
                SaveWordModal(
                    word = wordEntity,
                    folders = emptyList(), // Pass an empty list
                    onFolderSelected = onFolderSelected,
                    onCreateNewFolder = onCreateNewFolder,
                    onDismiss = onDismissModal
                )
            }
        }
    }
}


@Composable
fun RelatedWordCard(
    word: JapaneseWord,
    onClick: () -> Unit,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cardColor,
        modifier = Modifier
            .size(width = 140.dp, height = 100.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            (word.kanji ?: word.reading)?.let { Text(it, color = textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            word.meaning?.let { Text(it, color = textSecondary, fontSize = 12.sp, maxLines = 2) }
        }
    }
}

@Composable
fun RelatedWordCardShimmer(){
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1B2434),
        modifier = Modifier
            .size(width = 140.dp, height = 90.dp)
            .fillMaxWidth()
            .shimmer(cornerRadius = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
            )
        }
    }
}

@Composable
fun ExampleItem(example: ExampleWithFurigana, textColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        FuriganaSentence(tokens = example.tokens, textColor)

        Spacer(modifier = Modifier.height(4.dp))

        // English translation
        Text(
            text = example.english,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontStyle = FontStyle.Italic,
                color = textColor
            ),
            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
        )
    }
}

@Composable
fun FuriganaSentence(tokens: List<Token>, textColor: Color) {
    val style = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.SemiBold,
        color = textColor
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        tokens.forEach { (word, furigana) ->
            FuriganaWord(
                word = word, furigana = furigana, style = style, textColor = textColor
            )
        }
    }
}

@Composable
fun FuriganaWord(
    word: String,
    furigana: String,
    style: TextStyle,
    textColor: Color
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .wrapContentWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Furigana (small text above)
        Text(
            text = if (TextUtils.containsKanji(word)) furigana else " ".repeat(furigana.length),
            fontSize = 10.sp,
            color = textColor,
            textAlign = TextAlign.Center
        )

        // Word (main text)
        Text(
            text = word,
            style = style,
            color = textColor
        )
    }
}

@Preview
@Composable
fun preview() {
    JapanlibTheme() {
        RelatedWordCardShimmer()
    }
}