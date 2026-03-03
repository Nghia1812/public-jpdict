package com.prj.japanlib.feature_dictionary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prj.domain.model.dictionaryscreen.CustomWordListWithEntries
import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.japanlib.R
import com.prj.japanlib.common.utils.ClickEventDebouncer
import com.prj.japanlib.feature_dictionary.components.JWordItem
import com.prj.japanlib.feature_dictionary.components.SaveWordModal
import com.prj.japanlib.feature_dictionary.viewmodel.implemetations.SearchResultViewModel
import com.prj.japanlib.ui.theme.displayFontFamily
import com.prj.japanlib.ui.theme.onPrimaryLight
import com.prj.japanlib.uistate.DictionaryUiState
import kotlinx.coroutines.delay
import timber.log.Timber

/**
 * Stateful composable that manages the state for the search results screen.
 *
 *
 * @param searchQuery The initial query to search for, passed from the previous screen.
 * @param onWordClick A callback function invoked when a word item in the results list is clicked. It passes the word's ID.
 * @param onBackClick A callback function invoked when the user clicks the back button.
 */
@Composable
fun SearchResultScreen(searchQuery: String, onWordClick: (Int) -> Unit, onBackClick: () -> Unit) {
    val viewModel: SearchResultViewModel = hiltViewModel()
    val wordListUiState by viewModel.wordListUiStateFlow.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf(searchQuery) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val mDebouncer = remember { ClickEventDebouncer() }
    val customLists by viewModel.customWordList.collectAsStateWithLifecycle()
    LaunchedEffect(search) {
        if (search.isNotEmpty()) {
            mDebouncer.processClick { viewModel.getSearchWordList(search) }
            viewModel.getAllCustomLists()
        }
    }
    SearchResultScreenContent(
        wordListUiState = wordListUiState,
        customLists = customLists,
        searchQuery = search,
        onSearchChange = { search = it },
        onDone = {
            keyboardController?.hide()
            focusManager.clearFocus()
        },
        onWordClick = {
            mDebouncer.processClick { onWordClick(it) }
        },
        onBackClick = {
            mDebouncer.processClick { onBackClick() }
        },
        onFolderSelected = { it, wordEntity ->
            mDebouncer.processClick {
                val isWordInList = it.entries.any { it.id == wordEntity.id }
                if (isWordInList) {
                    Timber.d("VocabularyList: Removing word from custom list")
                    viewModel.removeWordFromCustomList(it.list.listId, wordEntity.id)
                } else {
                    Timber.d("VocabularyList: Adding word to custom list")
                    viewModel.addWordToCustomList(it.list.listId, wordEntity.id)
                }
            }
        },
        onCreateNewFolder = {

        }
    )


}

/**
 * A stateless composable that displays the UI for the search results screen.
 *
 *
 * @param wordListUiState The current state of the word list, which determines what UI to show.
 * @param searchQuery The current text in the search bar.
 * @param onSearchChange A callback function invoked when the search query text changes.
 * @param onDone A callback function invoked when the user presses the 'Done' action on the keyboard.
 * @param onWordClick A callback function for when a word item is clicked.
 * @param onBackClick A callback function for when the back button is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreenContent(
    wordListUiState: DictionaryUiState<List<JapaneseWord>>,
    customLists: DictionaryUiState<List<CustomWordListWithEntries>>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onDone: (KeyboardActionScope.() -> Unit)?,
    onWordClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    onFolderSelected: (CustomWordListWithEntries, JapaneseWord) -> Unit,
    onCreateNewFolder: (String) -> Unit
) {
    // Save scroll state
    val listState = rememberLazyListState()
    var selectedWord by remember { mutableStateOf<JapaneseWord?>(null) }
    val showSheet = selectedWord != null
    
    val starredWordIds = remember(customLists) {
        if (customLists is DictionaryUiState.Success) {
            customLists.data.flatMap { it.entries }.map { it.id }.toSet()
        } else {
            emptySet()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Top bar with back button and search query
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = onPrimaryLight
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            TextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                singleLine = true,
                keyboardActions = KeyboardActions(
                    onDone = onDone
                ),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                placeholder = {
                    Text(
                        fontFamily = displayFontFamily,
                        text = stringResource(R.string.search_hint)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(36.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                )
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp), contentAlignment = Alignment.Center
                        ) {
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

        // Results list
        when (wordListUiState) {
            DictionaryUiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_results),
                        color = onPrimaryLight,
                        fontSize = 16.sp
                    )
                }
            }

            is DictionaryUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.error_loading),
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                }
            }

            DictionaryUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DictionaryUiState.Success -> {
                val results = wordListUiState.data
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(results) { result ->
                        JWordItem(
                            japaneseWord = result,
                            isStarred = starredWordIds.contains(result.id),
                            onClick = onWordClick,
                            onBookmarkClick = { selectedWord = result },
                        )
                    }
                }
            }
        }
    }
}
