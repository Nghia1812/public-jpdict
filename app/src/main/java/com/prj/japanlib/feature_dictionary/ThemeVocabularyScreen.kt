package com.prj.japanlib.feature_dictionary

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prj.japanlib.feature_dictionary.components.ErrorListScreen
import com.prj.japanlib.feature_dictionary.components.LoadingIndicator
import com.prj.japanlib.feature_dictionary.components.VocabularyList
import com.prj.japanlib.feature_dictionary.viewmodel.implemetations.ThemeListViewModel
import com.prj.japanlib.ui.theme.onPrimaryLight
import com.prj.japanlib.uistate.DictionaryUiState

/**
 * A composable screen that displays a list of vocabulary words for a specific theme.
 *
 *
 * @param themeName The name of the theme for which to display the vocabulary list.
 * @param onWordClick A callback function that is invoked when a word in the list is clicked,
 *                    passing the word's ID.
 */
@Composable
fun ThemeVocabularyScreen(
    themeName: String,
    themeId: Int,
    onWordClick: (Int) -> Unit,
    onBackClick: () -> Unit
){
    val themeListViewModel: ThemeListViewModel = hiltViewModel()
    // Collect the UI state from the ViewModel
    val state by themeListViewModel.wordListMutableStateFlow.collectAsStateWithLifecycle()
    val showLeftSwipeHint by themeListViewModel.showLeftSwipeHint.collectAsStateWithLifecycle()
    val showRightSwipeHint by themeListViewModel.showRightSwipeHint.collectAsStateWithLifecycle()

    // Triggers fetching the word list when the themeName changes.
    LaunchedEffect(themeId) {
        themeListViewModel.getCustomWordList(themeId)
    }
    Column(modifier = Modifier.fillMaxSize()) {
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

            Text(
                text = themeName,
                color = onPrimaryLight
            )
        }
        // Handle UI states
        when (state) {
            // Show a loading state in the vocabulary list
            is DictionaryUiState.Loading -> {
                LoadingIndicator()
            }

            // On success, display the fetched list of words
            is DictionaryUiState.Success -> {
                VocabularyList(
                    listName = themeName,
                    words = (state as DictionaryUiState.Success).data,
                    onWordClick = onWordClick,
                    showLeftSwipeHint = showLeftSwipeHint,
                    showRightSwipeHint = showRightSwipeHint,
                    onLeftSwipeHintDismissed = themeListViewModel::onLeftSwipeHintDismissed,
                    onRightSwipeHintDismissed = themeListViewModel::onRightSwipeHintDismissed,
                    onPageChanged = themeListViewModel::onPageChanged,
                    baseViewModel = themeListViewModel,
                )
            }

        // If an error occurs, show an error screen
        is DictionaryUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ErrorListScreen(
                    errorMessage = (state as DictionaryUiState.Error).message,
                    onRetry = {
                        themeListViewModel.getCustomWordList(themeId)
                    }
                )
            }
        }

            // Treat the Empty state as a loading state
            DictionaryUiState.Empty -> {
                LoadingIndicator()
            }
        }
    }
}