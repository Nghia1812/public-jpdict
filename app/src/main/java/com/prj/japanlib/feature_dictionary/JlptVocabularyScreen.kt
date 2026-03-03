package com.prj.japanlib.feature_dictionary

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.prj.japanlib.feature_dictionary.viewmodel.implemetations.JlptVocabularyViewModel
import com.prj.japanlib.ui.theme.onPrimaryLight
import com.prj.japanlib.uistate.DictionaryUiState

/**
 * A composable screen displays a list of jlpt vocabulary words.
 *
 * @param level The name of the jlpt vocabulary list to display. This is used to fetch
 *                 the corresponding word list from the ViewModel.
 * @param onWordClick A lambda function to be invoked when a word in the list is clicked,
 *                    passing the word's ID.
 */
@Composable
fun JlptVocabularyScreen(
    level: String,
    onWordClick: (Int) -> Unit,
    onBackClick: () -> Unit
){
    val jlptVocabularyViewModel: JlptVocabularyViewModel = hiltViewModel()
    val wordListUiState by jlptVocabularyViewModel.wordListMutableStateFlow.collectAsStateWithLifecycle()
    val showLeftSwipeHint by jlptVocabularyViewModel.showLeftSwipeHint.collectAsStateWithLifecycle()
    val showRightSwipeHint by jlptVocabularyViewModel.showRightSwipeHint.collectAsStateWithLifecycle()

    LaunchedEffect(level) {
        jlptVocabularyViewModel.getJlptWordList(level)
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
                text = level,
                color = onPrimaryLight
            )
        }
        // Handle UI states
        when (wordListUiState) {
            // Show a loading state in the vocabulary list
            is DictionaryUiState.Loading -> {
                LoadingIndicator()
            }

            // On success, display the fetched list of words
            is DictionaryUiState.Success -> {
                VocabularyList(
                    listName = level,
                    words = (wordListUiState as DictionaryUiState.Success).data,
                    onWordClick = onWordClick,
                    showLeftSwipeHint = showLeftSwipeHint,
                    showRightSwipeHint = showRightSwipeHint,
                    onLeftSwipeHintDismissed = jlptVocabularyViewModel::onLeftSwipeHintDismissed,
                    onRightSwipeHintDismissed = jlptVocabularyViewModel::onRightSwipeHintDismissed,
                    onPageChanged = jlptVocabularyViewModel::onPageChanged,
                    baseViewModel = jlptVocabularyViewModel,
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
                    errorMessage = (wordListUiState as DictionaryUiState.Error).message,
                    onRetry = {
                        jlptVocabularyViewModel.getJlptWordList(level)
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