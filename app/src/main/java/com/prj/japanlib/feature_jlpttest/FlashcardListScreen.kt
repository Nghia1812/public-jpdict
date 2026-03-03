package com.prj.japanlib.feature_jlpttest

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prj.domain.model.testscreen.ListWordCountWithState
import com.prj.japanlib.R
import com.prj.japanlib.common.utils.ClickEventDebouncer
import com.prj.japanlib.feature_dictionary.EmptyStateItem
import com.prj.japanlib.feature_dictionary.components.ErrorListScreen
import com.prj.japanlib.feature_jlpttest.viewmodel.implementation.FlashcardListViewModel
import com.prj.japanlib.uistate.TestScreenUiState

@Composable
fun FlashcardListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToThemeDetail: (String) -> Unit = {},
    onNavigateToJlptDetail: (String) -> Unit = {},
    onNavigateToCustomDetail: (String) -> Unit = {}
) {
    val viewModel: FlashcardListViewModel = hiltViewModel()
    val customListUiState by viewModel.customWordListUiState.collectAsStateWithLifecycle()
    val themeListUiState by viewModel.themeListUiState.collectAsStateWithLifecycle()
    val jlptWordCountUiState by viewModel.jlptWordCountUiState.collectAsStateWithLifecycle()
    val mDebouncer = remember { ClickEventDebouncer() }

    FlashcardListContent(
        jlptWordCountUiState = jlptWordCountUiState,
        customListUiState = customListUiState,
        themeListUiState = themeListUiState,
        onNavigateBack = {
            mDebouncer.processClick { onNavigateBack() }
        },
        onThemePackClick = { it ->
            mDebouncer.processClick { onNavigateToThemeDetail(it) }
        },
        onJlptClick = { it ->
            mDebouncer.processClick { onNavigateToJlptDetail(it) }
        },
        onCustomClick = { it ->
            mDebouncer.processClick { onNavigateToCustomDetail(it) }
        }
    )
}

@Composable
fun FlashcardListContent(
    jlptWordCountUiState: TestScreenUiState<List<ListWordCountWithState>>,
    customListUiState: TestScreenUiState<List<ListWordCountWithState>>,
    themeListUiState: TestScreenUiState<List<ListWordCountWithState>>,
    onNavigateBack: () -> Unit,
    onThemePackClick: (String) -> Unit,
    onJlptClick: (String) -> Unit,
    onCustomClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header with Back Button and Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.flashcards_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        LazyColumn {
            // 1. Thematic Vocabulary Packs
            item {
                SectionHeader(
                    title = stringResource(R.string.section_thematic_vocabulary),
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                )
            }
            when(themeListUiState) {
                is TestScreenUiState.Success -> {
                    items(themeListUiState.data) { pack ->
                        FlashcardListItem(
                            list = pack,
                            onClick = { onThemePackClick(pack.id) },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }

                is TestScreenUiState.Error -> {
                    item {
                        ErrorListScreen(
                            errorMessage = stringResource(R.string.error_loading_data),
                            onRetry = {}
                        )
                    }
                }

                is TestScreenUiState.Empty -> {
                    item {
                        EmptyStateItem(stringResource(R.string.no_theme_lists))
                    }
                }
                else -> {}
            }

            // 2. JLPT Vocabulary
            item {
                SectionHeader(
                    title = stringResource(R.string.section_jlpt_vocabulary),
                    modifier = Modifier.padding(top = 32.dp, bottom = 12.dp)
                )
            }
            when(jlptWordCountUiState) {
                is TestScreenUiState.Success -> {
                    items(jlptWordCountUiState.data) { list ->
                        FlashcardListItem(
                            list = list,
                            onClick = { onJlptClick(list.id) },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }

                is TestScreenUiState.Error -> {
                    item {
                        ErrorListScreen(
                            errorMessage = stringResource(R.string.error_loading_data),
                            onRetry = {}
                        )
                    }
                }

                is TestScreenUiState.Empty -> {
                    item {
                        EmptyStateItem(stringResource(R.string.no_jlpt_lists))
                    }
                }
                else -> {}
            }

            // 3. Custom Lists
            item {
                SectionHeader(
                    title = stringResource(R.string.section_custom_lists),
                    modifier = Modifier.padding(top = 32.dp, bottom = 12.dp)
                )
            }
            when(customListUiState) {
                is TestScreenUiState.Success -> {
                    items(customListUiState.data) { list ->
                        FlashcardListItem(
                            list = list,
                            onClick = { onCustomClick(list.id) },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }

                is TestScreenUiState.Error -> {
                    item {
                        ErrorListScreen(
                            errorMessage = stringResource(R.string.error_loading_data),
                            onRetry = {}
                        )
                    }
                }

                is TestScreenUiState.Empty -> {
                    item {
                        EmptyStateItem(stringResource(R.string.no_custom_lists))
                    }
                }

                else -> {}
            }
        }
    }
}

// =============================================
// Reusable Components
// =============================================
@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

@Composable
private fun FlashcardListItem(
    list: ListWordCountWithState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (list.totalCount > 0) {
        list.rememberedCount.toFloat() / list.totalCount.toFloat()
    } else {
        0f
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = list.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = stringResource(R.string.learning_progress, (progress * 100).toInt()),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.navigate_content_description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview
@Composable
fun ScreenFlashCardPreview(){
    FlashcardListContent(
        jlptWordCountUiState = TestScreenUiState.Empty,
        customListUiState = TestScreenUiState.Empty,
        themeListUiState = TestScreenUiState.Empty,
        onNavigateBack = {},
        onThemePackClick = {},
        onJlptClick = {},
        onCustomClick = {}
    )
}
