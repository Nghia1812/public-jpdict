package com.prj.japanlib.feature_dictionary.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prj.domain.model.dictionaryscreen.CustomWordListWithEntries
import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.japanlib.R
import com.prj.japanlib.common.components.SwipeDirection
import com.prj.japanlib.common.components.SwipeHintOverlay
import com.prj.japanlib.common.utils.ClickEventDebouncer
import com.prj.japanlib.feature_dictionary.viewmodel.implemetations.BaseVocabularyViewModel
import com.prj.japanlib.uistate.DictionaryUiState
import timber.log.Timber

/**
 * Composable to display list of vocabulary
 *
 * @param listName name of the list
 * @param words List of displayed vocab
 * @param onWordClick Callback when user click on a word
 * @param showLeftSwipeHint A boolean flag to determine if the "swipe left" tutorial hint should be shown.
 * @param showRightSwipeHint A boolean flag to determine if the "swipe right" tutorial hint should be shown.
 * @param onLeftSwipeHintDismissed A callback invoked when the user dismisses the "swipe left" hint.
 * @param onRightSwipeHintDismissed A callback invoked when the user dismisses the "swipe right" hint.
 * @param onPageChanged A callback invoked when the pager's current page changes, passing the new page index.
 */
@Composable
fun VocabularyList(
    baseViewModel: BaseVocabularyViewModel,
    listName: String,
    words: List<JapaneseWord>,
    onWordClick: (Int) -> Unit,
    showLeftSwipeHint: Boolean,
    showRightSwipeHint: Boolean,
    onLeftSwipeHintDismissed: () -> Unit,
    onRightSwipeHintDismissed: () -> Unit,
    onPageChanged: (Int) -> Unit,
) {
    val customLists by baseViewModel.customWordList.collectAsStateWithLifecycle()
    LaunchedEffect(words) {
        baseViewModel.getAllCustomLists()
    }
    val mDebouncer = remember { ClickEventDebouncer() }

    // List of words
    WordPagerContent(
        words = words,
        onWordClick = onWordClick,
        showLeftSwipeHint = showLeftSwipeHint,
        showRightSwipeHint = showRightSwipeHint,
        onLeftSwipeHintDismissed = onLeftSwipeHintDismissed,
        onRightSwipeHintDismissed = onRightSwipeHintDismissed,
        onPageChanged = onPageChanged,
        customLists = customLists,
        onFolderSelected = { it, wordEntity ->
            mDebouncer.processClick {
                val isWordInList = it.entries.any { it.id == wordEntity.id }
                if (isWordInList) {
                    Timber.d("VocabularyList: Removing word from custom list")
                    baseViewModel.removeWordFromCustomList(it.list.listId, wordEntity.id)
                } else {
                    Timber.d("VocabularyList: Adding word to custom list")
                    baseViewModel.addWordToCustomList(it.list.listId, wordEntity.id)
                }
            }
        },
        onCreateNewFolder = {baseViewModel.createNewCustomList(it) }
    )
}


/**
 * Composable handle content of the pager
 *
 * @param words List of words
 * @param onWordClick Callback
 * @param showLeftSwipeHint A boolean flag to determine if the "swipe left" tutorial hint should be shown.
 * @param showRightSwipeHint A boolean flag to determine if the "swipe right" tutorial hint should be shown.
 * @param onLeftSwipeHintDismissed A callback invoked when the user dismisses the "swipe left" hint.
 * @param onRightSwipeHintDismissed A callback invoked when the user dismisses the "swipe right" hint.
 * @param onPageChanged A callback invoked when the pager's current page changes, passing the new page index.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WordPagerContent(
    words: List<JapaneseWord>,
    onWordClick: (Int) -> Unit,
    customLists: DictionaryUiState<List<CustomWordListWithEntries>>,
    showLeftSwipeHint: Boolean,
    showRightSwipeHint: Boolean,
    onLeftSwipeHintDismissed: () -> Unit,
    onRightSwipeHintDismissed: () -> Unit,
    onPageChanged: (Int) -> Unit,
    onFolderSelected: (CustomWordListWithEntries, JapaneseWord) -> Unit,
    onCreateNewFolder: (String) -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    // Check if user has seen the tutorial
    var showLeftSwipeHint by remember {
        mutableStateOf(!sharedPrefs.getBoolean("has_seen_left_swipe", false))
    }
    var showRightSwipeHint by remember {
        mutableStateOf(false)
    }

    // Split the word list to chunk - 30 words/page
    val chunks = remember(words) {
        words.chunked(30)
    }

    // state for HorizontalPager
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { chunks.size }
    )
    var selectedWord by remember { mutableStateOf<JapaneseWord?>(null) }
    val showSheet = selectedWord != null

    // Detect when user swipes to page 2 (index 1)
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 1 &&
            !sharedPrefs.getBoolean("has_seen_right_swipe", false)
        ) {
            showLeftSwipeHint = false
            showRightSwipeHint = true
        }
    }

    val starredWordIds = remember(customLists) {
        if (customLists is DictionaryUiState.Success) {
            customLists.data.flatMap { it.entries }.map { it.id }.toSet()
        } else {
            emptySet()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // HorizontalPager swipe to change page
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                // display chunk of words for current page
                WordListPage(
                    words = chunks[page],
                    onWordClick = onWordClick,
                    starredWordIds = starredWordIds,
                    onBookmarkClick = { word ->
                        Timber.d("WordDetailScreen: Bookmark clicked for word: ${word.id}")
                        selectedWord = word
                    }
                )
            }

            // Page indicator to show current page
            PageIndicator(
                currentPage = pagerState.currentPage,
                totalPages = chunks.size
            )
        }

        if (showSheet && selectedWord != null) {
            when (customLists) {
                is DictionaryUiState.Success -> {
                    SaveWordModal(
                        word = selectedWord!!,
                        folders = customLists.data,
                        onFolderSelected = { entries ->
                            onFolderSelected(entries, selectedWord!!)
                        },
                        onCreateNewFolder = onCreateNewFolder,
                        onDismiss = { selectedWord = null }
                    )
                }
                is DictionaryUiState.Loading -> {
                    SaveWordModal(
                        word = selectedWord!!,
                        folders = emptyList(), // Pass an empty list
                        onFolderSelected = { entries ->
                            onFolderSelected(entries, selectedWord!!)
                        },
                        onCreateNewFolder = onCreateNewFolder,
                        onDismiss = { selectedWord = null },
                        isLoading = true
                    )
                }
                is DictionaryUiState.Error -> {
                    // Show an error state inside the modal.
                    ModalBottomSheet(onDismissRequest = { selectedWord = null }) {
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
                        word = selectedWord!!,
                        folders = emptyList(), // Pass an empty list
                        onFolderSelected = { entries ->
                            onFolderSelected(entries, selectedWord!!)
                        },
                        onCreateNewFolder = onCreateNewFolder,
                        onDismiss = { selectedWord = null }
                    )
                }
            }
        }

        // Left swipe hint (on first page)
        if (showLeftSwipeHint && chunks.size > 1) {
            SwipeHintOverlay(
                direction = SwipeDirection.LEFT,
                onDismiss = {
                    showLeftSwipeHint = false
                    sharedPrefs.edit().putBoolean("has_seen_left_swipe", true).apply()
                }
            )
        }

        // Right swipe hint (on second page)
        if (showRightSwipeHint) {
            SwipeHintOverlay(
                direction = SwipeDirection.RIGHT,
                onDismiss = {
                    showRightSwipeHint = false
                    sharedPrefs.edit().putBoolean("has_seen_right_swipe", true).apply()
                }
            )
        }
    }
}

/**
 * Composable display a list of words for a page
 *
 * @param words Word chunk for current page
 * @param onWordClick Callback
 */
@Composable
private fun WordListPage(
    words: List<JapaneseWord>,
    starredWordIds: Set<Int>,
    onWordClick: (Int) -> Unit,
    onBookmarkClick: (JapaneseWord) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        items(words) { word ->
            JWordItem(
                japaneseWord = word,
                isStarred = starredWordIds.contains(word.id),
                onClick = onWordClick,
                onBookmarkClick = { onBookmarkClick(word) }
            )
        }
    }
}

/**
 * Composable Page indicator to show current page
 *
 * @param currentPage Current page
 * @param totalPages
 */
@Composable
private fun PageIndicator(
    currentPage: Int,
    totalPages: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(
                id = R.string.page_indicator,
                currentPage + 1,
                totalPages
            )
        )
    }
}