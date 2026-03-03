package com.prj.japanlib.feature_dictionary.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prj.domain.model.dictionaryscreen.CustomWordListWithEntries
import com.prj.domain.model.dictionaryscreen.JapaneseWord
import com.prj.japanlib.R
import com.prj.japanlib.common.components.shimmer
import com.prj.japanlib.ui.theme.JapanlibTheme
import kotlinx.coroutines.delay
import timber.log.Timber

/**
 * The Modal Bottom Sheet for saving a word to a custom list.
 * Show when user want to save words
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveWordModal(
    word: JapaneseWord,
    folders: List<CustomWordListWithEntries>,
    onFolderSelected: (CustomWordListWithEntries) -> Unit,
    onCreateNewFolder: (String) -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false
) {
    var creatingFolder by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scrollState = rememberScrollState()
    LaunchedEffect(creatingFolder) {
        if (creatingFolder) {
            delay(100) // keyboard animation
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState) // Make the column scrollable
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .imePadding()
        ) {
            // ───────────────────────
            // HEADER
            // ───────────────────────
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .background(Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                        .align(Alignment.TopCenter)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color(0xFF8FA0B3)
                    )
                }
            }
            Text(
                stringResource(R.string.save_to_custom_list),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // ───────────────────────
            // WORD PREVIEW CARD
            // ───────────────────────
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1B2434),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    word.reading?.let { Text(it, color = Color(0xFF8FA0B3), fontSize = 14.sp) }
                    word.kanji?.let { Text(it, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold) }
                    word.meaning?.let { Text(it, color = Color(0xFF8FA0B3), fontSize = 14.sp) }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ───────────────────────
            // TITLE
            // ───────────────────────
            Text(
                text = stringResource(R.string.sheet_subtitle),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(16.dp))

            // ───────────────────────
            // FOLDER GRID
            // ───────────────────────
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp) // Keep scrollable grid
            ) {
                if (isLoading) {
                    items(4) {
                        ShimmerFolderItemCard()
                    }
                } else if (folders.isEmpty()){
                    item {
                        Text(stringResource(R.string.no_custom_lists))
                    }
                } else {
                    items(folders) { folder ->
                        Timber.d("Custom Folder: ${folder.list.name} - ${folder.list.listId}")
                        val isSaved = folder.entries.any { it.id == word.id }
                        FolderItemCard(
                            folder = folder,
                            onClick = { onFolderSelected(folder) },
                            isSaved = isSaved
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ───────────────────────
            // "Create Folder" ROW
            // ───────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { creatingFolder = !creatingFolder }
                    .background(Color(0xFF1B2434))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF203046)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.bookmark_ic),
                        contentDescription = null,
                        tint = Color(0xFF4F7BFF)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.create_custom_list), color = Color.White)
            }

            // ───────────────────────
            // NEW FOLDER NAME FIELD
            // ───────────────────────
            if (creatingFolder) {
                val keyboardController = LocalSoftwareKeyboardController.current
                val focusManager = LocalFocusManager.current
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        placeholder = {
                            Text(
                                stringResource(R.string.enter_folder_name),
                                color = Color(0xFF8FA0B3)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1B2434),
                            unfocusedContainerColor = Color(0xFF1B2434),
                            focusedBorderColor = Color(0xFF2F3E52),
                            unfocusedBorderColor = Color(0xFF2F3E52),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (newFolderName.isNotBlank()) {
                                    onCreateNewFolder(newFolderName.trim())
                                    newFolderName = ""
                                }
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        )
                    )

                    Button(
                        onClick = {
                            if (newFolderName.isNotBlank()) {
                                onCreateNewFolder(newFolderName.trim())
                                newFolderName = ""
                            }
                        },
                        enabled = newFolderName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F7BFF),
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(56.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        Text(
                            stringResource(R.string.create),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun FolderItemCard(
    folder: CustomWordListWithEntries,
    onClick: () -> Unit,
    isSaved: Boolean = false
) {
    val backgroundColor = if (isSaved) {
        Color(0xFF243354)
    } else {
        Color(0xFF1B2434)
    }

    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Icon(
                painter = painterResource(id = R.drawable.folder_ic),
                contentDescription = null,
                tint = Color(0xFF4F7BFF),
                modifier = Modifier.size(40.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = folder.list.name,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = stringResource(R.string.word_count, folder.entries.size),
                    color = Color(0xFF9FB3FF),
                    fontSize = 11.sp
                )
            }
        }

        if (isSaved) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.TopEnd)
                    .offset((-6).dp, 6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4F7BFF))
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun ShimmerFolderItemCard() {
    Surface(
        color = Color(0xFF1B2434),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .height(110.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Shimmer for the Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmer(cornerRadius = 8.dp)
            )

            // Shimmer for the Text lines
            Column {
                // Shimmer for Folder Name
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f) // 70% of card width
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer(cornerRadius = 4.dp)
                )
                Spacer(Modifier.height(8.dp))
                // Shimmer for Item Count
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f) // 40% of card width
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer(cornerRadius = 4.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun ModalReview(){
    JapanlibTheme() {
        SaveWordModal(
            word = JapaneseWord(
                id = 1,
                kanji = "",
                reading = "",
                meaning = "",
                type = ""
            ),
            folders = emptyList(),
            onFolderSelected = {},
            onCreateNewFolder = {},
            onDismiss = {},
            isLoading = false
        )
    }
}