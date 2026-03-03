package com.prj.japanlib.feature_jlpttest

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prj.domain.model.testscreen.BaseTestSection
import com.prj.domain.model.testscreen.Level
import com.prj.domain.model.testscreen.Source
import com.prj.domain.model.testscreen.TestSectionType
import com.prj.japanlib.R
import com.prj.japanlib.common.components.shimmer
import com.prj.japanlib.feature_dictionary.components.ErrorScreen
import com.prj.japanlib.feature_jlpttest.components.NoInternetDialog
import com.prj.japanlib.feature_jlpttest.viewmodel.implementation.TestSectionsViewModel
import com.prj.japanlib.ui.theme.primaryLight
import com.prj.japanlib.ui.theme.sectionProgressBar
import com.prj.japanlib.uistate.TestScreenUiState
import timber.log.Timber

@get:DrawableRes
val TestSectionType.iconRes: Int
    get() = when (this) {
        TestSectionType.LISTENING -> R.drawable.ic_listening
        TestSectionType.READING -> R.drawable.ic_reading
        TestSectionType.VOCABULARY -> R.drawable.ic_vocabulary
        TestSectionType.GRAMMAR -> R.drawable.ic_grammar
    }

@get:StringRes
val TestSectionType.displayNameRes: Int
    get() = when (this) {
        TestSectionType.LISTENING -> R.string.section_listening
        TestSectionType.READING -> R.string.section_reading
        TestSectionType.VOCABULARY -> R.string.section_vocabulary
        TestSectionType.GRAMMAR -> R.string.section_grammar
    }

// Stateful composable
@Composable
fun TestSectionsScreen(
    testId: String,
    level: Level,
    onNavigateBack: () -> Unit,
    onSectionClick: (String, String, String) -> Unit
) {
    val testSectionsViewModel: TestSectionsViewModel = hiltViewModel()
    val testSectionsUiState by testSectionsViewModel.testSections.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        testSectionsViewModel.getTestSections(Source.CUSTOM, level, testId)
    }
    var showNoInternetDialog by remember { mutableStateOf(false) }
    LaunchedEffect(testSectionsUiState) {
        showNoInternetDialog = when (testSectionsUiState) {
            is TestScreenUiState.NoInternet -> true
            else -> false
        }
    }

    NoInternetDialog(
        showDialog = showNoInternetDialog,
        onDismiss = {
            showNoInternetDialog = false
            onNavigateBack()
        }
    )

    TestSectionsScreenContent(
        testId = testId,
        level = level,
        sectionsUiState = testSectionsUiState,
        onNavigateBack = onNavigateBack,
        onSectionClick = onSectionClick
    )
}

// Stateless composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestSectionsScreenContent(
    testId: String,
    level: Level,
    sectionsUiState: TestScreenUiState<List<BaseTestSection>>,
    onNavigateBack: () -> Unit,
    onSectionClick: (String, String, String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.test_sections_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        when (sectionsUiState) {
            is TestScreenUiState.Success -> {
                val sections = sectionsUiState.data
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sections) { section ->
                        TestSectionCard(
                            section = section,
                            onClick = {
                                val sectionValue = section.type.value
                                val levelValue = level.value
                                if (testId.isNotBlank() && sectionValue.isNotBlank() && levelValue.isNotBlank()) {
                                    onSectionClick(testId, sectionValue, levelValue)
                                } else {
                                    Timber.e("Invalid testId, sectionValue, or levelValue")
                                }
                            },
                            icon = section.type.iconRes,
                            iconBackgroundColor = primaryLight
                        )
                    }
                }
            }

            is TestScreenUiState.Empty -> {
                ErrorScreen(stringResource(id = R.string.no_data_available)) { }
            }

            is TestScreenUiState.Error -> {
                ErrorScreen(
                    sectionsUiState.message ?: stringResource(id = R.string.unknown_error)
                ) { }
            }

            is TestScreenUiState.Loading -> {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    repeat(4) {
                        TestSectionCardShimmer()
                    }
                }
            }

            is TestScreenUiState.NoInternet -> {
                // NOP
            }
        }

    }
}

@Composable
fun TestSectionCard(
    section: BaseTestSection,
    onClick: () -> Unit,
    icon: Int,
    iconBackgroundColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                onClick()
            }),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = icon),
                    contentDescription = section.type.value,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title and progress
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(id = section.type.displayNameRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(id = R.string.questions_count, section.questions.size),
                    style = MaterialTheme.typography.bodySmall,
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (section.correctAnswerCount != 0) {
                    val progress =
                        section.correctAnswerCount.toFloat() / section.questions.size.toFloat()
                    Text(
                        text = stringResource(
                            id = R.string.progress_complete,
                            (progress * 100).toInt()
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = sectionProgressBar,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.status_not_started),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Chevron icon
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun TestSectionCardShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2434)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // --- Shimmer icon ---
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .shimmer()
            )

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title shimmer
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )

                Spacer(Modifier.height(8.dp))

                // Subtitle shimmer
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }
        }
    }
}
