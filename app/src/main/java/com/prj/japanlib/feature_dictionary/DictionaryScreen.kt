package com.prj.japanlib.feature_dictionary

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prj.japanlib.feature_dictionary.components.WordCategoryItem
import com.prj.japanlib.feature_dictionary.viewmodel.implemetations.DictionaryViewModel
import com.prj.domain.model.dictionaryscreen.ThemeCount
import com.prj.japanlib.R
import com.prj.japanlib.common.JLPT_LEVELS
import com.prj.japanlib.common.utils.ClickEventDebouncer
import com.prj.japanlib.feature_dictionary.components.ErrorListScreen
import com.prj.japanlib.feature_dictionary.components.ThemeCardShimmer
import com.prj.japanlib.feature_dictionary.components.WordCategoryItemShimmer
import com.prj.japanlib.ui.theme.JapanlibTheme
import com.prj.japanlib.uistate.DictionaryUiState
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.domain.model.dictionaryscreen.WordList


/**
 * The main screen for the dictionary feature, serves as the entry point for browsing
 * different vocabulary lists like JLPT, custom lists, and thematic packs.
 *
 * @param onSearchClick Callback invoked when the search button or FAB is clicked.
 * @param onJlptWordListClick Callback invoked when a JLPT level item is clicked, passing the level name.
 * @param onCustomWordListClick Callback invoked when a custom list item is clicked, passing the list name.
 * @param onThemeListClick Callback invoked when a theme pack item is clicked, passing the theme name.
 */
@Composable
fun DictionaryScreen(
    onSearchClick: () -> Unit,
    onJlptWordListClick: (String) -> Unit,
    onCustomWordListClick: (String, String) -> Unit,
    onThemeListClick: (Int, String) -> Unit
) {
    val dictionaryViewModel: DictionaryViewModel = hiltViewModel()
    // Handle multiple click across components
    val mDebouncer = remember { ClickEventDebouncer() }
    // Collect UI states for different vocabulary categories.
    val wordCountUiState by dictionaryViewModel.wordCountUiState.collectAsStateWithLifecycle()
    val customWordListUiState by dictionaryViewModel.customWordListUiState.collectAsStateWithLifecycle()
    val themeListUiState by dictionaryViewModel.themeListUiState.collectAsStateWithLifecycle()
    val searchResultsUiState by dictionaryViewModel.searchResultsUiState.collectAsStateWithLifecycle()
    val addWordResultUiState by dictionaryViewModel.addWordResultUiState.collectAsStateWithLifecycle()
    val removeWordResultUiState by dictionaryViewModel.removeWordResultUiState.collectAsStateWithLifecycle()
    val themeWordsUiState by dictionaryViewModel.themeWordsUiState.collectAsStateWithLifecycle()

    // State for showing the add word dialog
    var showAddWordDialog by remember { mutableStateOf(false) }

    // Delegate the UI rendering to a stateless content composable.
    DictionaryScreenContent(
        onSearchClick = {
            mDebouncer.processClick { onSearchClick() }
        },
        onJlptWordListClick = { listId, _ ->
            mDebouncer.processClick { onJlptWordListClick(listId) }
        },
        onCustomWordListClick = { listName, listId ->
            mDebouncer.processClick { onCustomWordListClick(listName, listId) }
        },
        onThemeListClick = {themeId, themeName ->
            mDebouncer.processClick { onThemeListClick(themeId, themeName) }
        },
        wordCountUiState = wordCountUiState,
        wordListUiState = customWordListUiState,
        themeListUiState = themeListUiState,
        onRetry = {
            // Provide a retry mechanism to re-fetch all data.
            dictionaryViewModel.getWordCount(JLPT_LEVELS)
            dictionaryViewModel.getAllCustomWordLists()
            dictionaryViewModel.getAllThemeWordLists()
        },
        onShowAddWordDialog = { showAddWordDialog = true }
    )

    // Add Word to Theme Dialog
    if (showAddWordDialog) {
        AddWordToThemeDialog(
            themeListUiState = themeListUiState,
            searchResultsUiState = searchResultsUiState,
            addWordResultUiState = addWordResultUiState,
            removeWordResultUiState = removeWordResultUiState,
            themeWordsUiState = themeWordsUiState,
            onDismiss = { 
                showAddWordDialog = false
                dictionaryViewModel.resetSearchResults()
                dictionaryViewModel.resetAddWordResult()
                dictionaryViewModel.resetRemoveWordResult()
                dictionaryViewModel.resetThemeWords()
            },
            onSearch = { query -> dictionaryViewModel.searchWords(query) },
            onAddWord = { themeId, entryId -> dictionaryViewModel.addWordToTheme(themeId, entryId) },
            onRemoveWord = { themeId, entryId -> dictionaryViewModel.removeWordFromTheme(themeId, entryId) },
            onLoadThemeWords = { themeId -> dictionaryViewModel.loadThemeWords(themeId) },
            onResetAddResult = { dictionaryViewModel.resetAddWordResult() },
            onResetRemoveResult = { dictionaryViewModel.resetRemoveWordResult() }
        )
    }

}

/**
 * The stateless content of the DictionaryScreen. It displays the UI based on the provided states.
 *
 * @param onSearchClick Callback for search actions.
 * @param onJlptWordListClick Callback for JLPT list clicks.
 * @param onCustomWordListClick Callback for custom list clicks.
 * @param onThemeListClick Callback for theme list clicks.
 * @param wordCountUiState The UI state for JLPT word counts.
 * @param wordListUiState The UI state for custom word lists.
 * @param themeListUiState The UI state for thematic packs.
 * @param onRetry Callback to retry fetching data in case of an error.
 */
@Composable
fun DictionaryScreenContent(
    onSearchClick: () -> Unit,
    onJlptWordListClick: (String, String) -> Unit,
    onCustomWordListClick: (String, String) -> Unit,
    onThemeListClick: (Int, String) -> Unit,
    wordCountUiState: DictionaryUiState<List<WordList>>,
    wordListUiState: DictionaryUiState<List<WordList>>,
    themeListUiState: DictionaryUiState<List<ThemeCount>>,
    onRetry: () -> Unit,
    onShowAddWordDialog: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                // FAB for adding words to theme (testing feature)
                FloatingActionButton(
                    onClick = onShowAddWordDialog,
                    containerColor = Color(0xFF28A745),
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Word to Theme")
                }
                // Floating Action Button to navigate to the search screen.
                FloatingActionButton(
                    onClick = onSearchClick,
                    containerColor = Color(0xFF007BFF),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        // Use LazyColumn for a scrollable list of sections.
        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // --- Thematic Packs Section ---
            item {
                TopBar(onSearchClick)
                SectionTitle(stringResource(R.string.theme_pack_title))
                // Handle different UI states for the theme list.
                when (themeListUiState) {
                    is DictionaryUiState.Loading -> {
                        LazyHorizontalGrid(
                            rows = GridCells.Fixed(2),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.height(360.dp)
                        ) {
                            items(4) {
                                ThemeCardShimmer()
                            }
                        }
                    }
                    is DictionaryUiState.Success -> {
                        val themeList = themeListUiState.data
                        if (themeList.isNotEmpty()) {
                            ThematicPackGrid(
                                themeList = themeList,
                                onThemeListClick = onThemeListClick
                            )
                        } else {
                            EmptyStateItem(stringResource(R.string.no_theme_lists))
                        }
                    }
                    is DictionaryUiState.Empty -> {
                        EmptyStateItem(stringResource(R.string.no_theme_lists))
                    }
                    is DictionaryUiState.Error -> {
                        ErrorListScreen(
                            errorMessage = stringResource(R.string.error_loading_data),
                            onRetry = onRetry
                        )
                    }
                }
            }
            // --- JLPT Lists Section ---
            item {
                SectionTitle(stringResource(R.string.jlpt_list_title))
                // Handle different UI states for the JLPT list.
                when (wordCountUiState) {
                    is DictionaryUiState.Loading -> {
                        Column(Modifier.padding(horizontal = 16.dp)){
                            repeat(4){
                                WordCategoryItemShimmer()
                            }
                        }
                    }
                    is DictionaryUiState.Success -> {
                        val jlptLevels = wordCountUiState.data
                        if (jlptLevels.isNotEmpty()) {
                            JLPTSection(
                                jlptLevels = jlptLevels,
                                onWordListClick = onJlptWordListClick
                            )
                        } else {
                            EmptyStateItem(stringResource(R.string.no_jlpt_lists))
                        }
                    }
                    is DictionaryUiState.Empty -> {
                        EmptyStateItem(stringResource(R.string.no_jlpt_lists))
                    }
                    is DictionaryUiState.Error -> {
                        ErrorListScreen(
                            errorMessage = stringResource(R.string.error_loading_data),
                            onRetry = onRetry
                        )
                    }
                }
            }
            // --- Custom Lists Section ---
            item {
                SectionTitle(stringResource(R.string.custom_list_title))
                // Handle different UI states for the custom list.
                when (wordListUiState) {
                    is DictionaryUiState.Loading -> {
                        Column(Modifier.padding(horizontal = 16.dp)){
                            repeat(4){
                                WordCategoryItemShimmer()
                            }
                        }
                    }
                    is DictionaryUiState.Success -> {
                        val customList = wordListUiState.data
                        if (customList.isNotEmpty()) {
                            CustomListSection(customList, onCustomWordListClick)
                        } else {
                            EmptyStateItem(stringResource(R.string.no_custom_lists))
                        }
                    }
                    is DictionaryUiState.Empty -> {
                        EmptyStateItem(stringResource(R.string.no_custom_lists))
                    }
                    is DictionaryUiState.Error -> {
                        ErrorListScreen(
                            errorMessage = stringResource(R.string.error_loading_data),
                            onRetry = onRetry
                        )
                    }
                }
            }
        }
    }
}

/**
 * A composable to display when a list is empty.
 *
 * @param message The message to display.
 */
@Composable
fun EmptyStateItem(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * A composable that displays a vertical list of custom vocabulary lists.
 *
 * @param wordList The list of custom word lists with their counts.
 * @param onWordListClick Callback invoked when a list item is clicked.
 */
@Composable
fun CustomListSection(wordList: List<WordList>, onWordListClick: (String, String) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        wordList.forEach {
            WordCategoryItem(topicList = it, onClick = onWordListClick)
            Spacer(Modifier.height(8.dp))
        }
    }
}

/**
 * The top bar of the screen, containing the title and a search icon.
 *
 * @param onSearchClick Callback invoked when the search icon is clicked.
 */
@Composable
fun TopBar(onSearchClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.learning),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        IconButton(
            onClick = onSearchClick
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * A composable for displaying section titles (e.g., "Thematic Packs", "JLPT Lists").
 *
 * @param title The text to display as the title.
 */
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 8.dp)
    )
}


/**
 * A grid that displays thematic vocabulary packs.
 *
 * @param themeList The list of themes to display.
 * @param onThemeListClick Callback invoked when a theme card is clicked.
 */
@Composable
fun ThematicPackGrid(
    themeList: List<ThemeCount>,
    onThemeListClick: (Int, String) -> Unit
) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.height(360.dp)
    ) {
        items(themeList) { theme ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121821)),
                modifier = Modifier
                    .height(180.dp)
                    .width(180.dp)
                    .clickable { onThemeListClick(theme.id, theme.name) }
            ) {
                Column {
                    // FULLY rounded image
                    Image(
                        bitmap = theme.image.toImageBitmap(),
                        contentDescription = theme.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )

                    Column(Modifier.padding(2.dp)) {
                        Text(
                            text = theme.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${theme.count} Words",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * A grid that displays JLPT vocabulary packs.
 *
 * @param jlptLevels The list of levels to display.
 * @param onWordListClick Callback invoked when a list card is clicked.
 */
@Composable
fun JLPTSection(jlptLevels: List<WordList>, onWordListClick: (String, String) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        jlptLevels.forEach { level ->
            WordCategoryItem(
                topicList = level,
                onClick = onWordListClick
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

/**
 * Converts a ByteArray to an ImageBitmap.
 */
fun ByteArray.toImageBitmap(maxWidthPx: Int? = null): ImageBitmap {
    val originalBitmap = BitmapFactory.decodeByteArray(this, 0, size)
    val resizedBitmap = if (maxWidthPx != null) {
        val ratio = maxWidthPx.toFloat() / originalBitmap.width
        val targetHeight = (originalBitmap.height * ratio).toInt()
        Bitmap.createScaledBitmap(originalBitmap, maxWidthPx, targetHeight, true)
    } else {
        originalBitmap
    }
    return resizedBitmap.asImageBitmap()
}

/**
 * Dialog for adding words to theme vocabulary manually (for testing purposes).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordToThemeDialog(
    themeListUiState: DictionaryUiState<List<ThemeCount>>,
    searchResultsUiState: DictionaryUiState<List<JapaneseWord>>,
    addWordResultUiState: DictionaryUiState<Unit>,
    removeWordResultUiState: DictionaryUiState<Unit>,
    themeWordsUiState: DictionaryUiState<List<JapaneseWord>>,
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit,
    onAddWord: (Int, Int) -> Unit,
    onRemoveWord: (Int, Int) -> Unit,
    onLoadThemeWords: (Int) -> Unit,
    onResetAddResult: () -> Unit,
    onResetRemoveResult: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTheme by remember { mutableStateOf<ThemeCount?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    // Get word IDs that are in the selected theme
    val wordIdsInTheme = remember(themeWordsUiState) {
        when (themeWordsUiState) {
            is DictionaryUiState.Success -> themeWordsUiState.data.map { it.id }.toSet()
            else -> emptySet()
        }
    }

    // Load theme words when theme selection changes
    LaunchedEffect(selectedTheme) {
        selectedTheme?.let { theme ->
            onLoadThemeWords(theme.id)
        }
    }

    // Handle add word result
    LaunchedEffect(addWordResultUiState) {
        when (addWordResultUiState) {
            is DictionaryUiState.Success -> {
                // Successfully added, refresh theme words
                selectedTheme?.let { onLoadThemeWords(it.id) }
                onResetAddResult()
            }
            is DictionaryUiState.Error -> {
                // Error occurred
                onResetAddResult()
            }
            else -> {}
        }
    }

    // Handle remove word result
    LaunchedEffect(removeWordResultUiState) {
        when (removeWordResultUiState) {
            is DictionaryUiState.Success -> {
                // Successfully removed, refresh is handled in ViewModel
                onResetRemoveResult()
            }
            is DictionaryUiState.Error -> {
                // Error occurred
                onResetRemoveResult()
            }
            else -> {}
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Word to Theme",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Theme Selection Dropdown
                when (themeListUiState) {
                    is DictionaryUiState.Success -> {
                        val themes = themeListUiState.data
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedTheme?.name ?: "Select Theme",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                themes.forEach { theme ->
                                    DropdownMenuItem(
                                        text = { Text(theme.name) },
                                        onClick = {
                                            selectedTheme = theme
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        Text("Loading themes...", color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Word") },
                    placeholder = { Text("Enter word to search...") },
                    trailingIcon = {
                        IconButton(onClick = { onSearch(searchQuery) }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onSearch(searchQuery)
                            focusManager.clearFocus()
                            defaultKeyboardAction(ImeAction.Done)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search Results
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color(0xFF121821), RoundedCornerShape(8.dp))
                ) {
                    when (searchResultsUiState) {
                        is DictionaryUiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        is DictionaryUiState.Success -> {
                            val words = searchResultsUiState.data
                            LazyColumn(
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(words) { word ->
                                    WordItemForSelection(
                                        word = word,
                                        selectedTheme = selectedTheme,
                                        isInTheme = wordIdsInTheme.contains(word.id),
                                        onAddWord = onAddWord,
                                        onRemoveWord = onRemoveWord,
                                        addWordResultUiState = addWordResultUiState,
                                        removeWordResultUiState = removeWordResultUiState
                                    )
                                }
                            }
                        }
                        is DictionaryUiState.Empty -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Search for words to add",
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        is DictionaryUiState.Error -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Error: ${searchResultsUiState.message}",
                                    color = Color.Red,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

/**
 * Individual word item in the selection list.
 */
@Composable
fun WordItemForSelection(
    word: JapaneseWord,
    selectedTheme: ThemeCount?,
    isInTheme: Boolean,
    onAddWord: (Int, Int) -> Unit,
    onRemoveWord: (Int, Int) -> Unit,
    addWordResultUiState: DictionaryUiState<Unit>,
    removeWordResultUiState: DictionaryUiState<Unit>
) {
    var isProcessing by remember { mutableStateOf(false) }

    // Reset processing state when operation completes
    LaunchedEffect(addWordResultUiState, removeWordResultUiState) {
        if (addWordResultUiState is DictionaryUiState.Success || 
            addWordResultUiState is DictionaryUiState.Error ||
            removeWordResultUiState is DictionaryUiState.Success ||
            removeWordResultUiState is DictionaryUiState.Error) {
            isProcessing = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = word.kanji ?: word.reading ?: "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (word.kanji != null && word.reading != null) {
                    Text(
                        text = word.reading!!,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
                word.meaning?.let {
                    Text(
                        text = it,
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Show Remove button if word is in theme, otherwise show Add button
            if (isInTheme) {
                Button(
                    onClick = {
                        selectedTheme?.let { theme ->
                            isProcessing = true
                            onRemoveWord(theme.id, word.id)
                        }
                    },
                    enabled = selectedTheme != null && !isProcessing,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC3545) // Red color for remove
                    )
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(20.dp).height(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Remove")
                    }
                }
            } else {
                Button(
                    onClick = {
                        selectedTheme?.let { theme ->
                            isProcessing = true
                            onAddWord(theme.id, word.id)
                        }
                    },
                    enabled = selectedTheme != null && !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(20.dp).height(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewDictionaryScreen() {
    JapanlibTheme {
        DictionaryScreenContent(
            onSearchClick = {},
            onJlptWordListClick = {_,_ -> },
            onCustomWordListClick = {_,_ -> },
            wordCountUiState = DictionaryUiState.Error("a"),
            wordListUiState = DictionaryUiState.Loading,
            themeListUiState = DictionaryUiState.Error("a"),
            onThemeListClick = {_,_ ->},
            onRetry = {}
        )
    }
}