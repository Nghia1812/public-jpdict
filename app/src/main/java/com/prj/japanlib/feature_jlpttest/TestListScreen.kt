package com.prj.japanlib.feature_jlpttest

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prj.japanlib.common.JLPT_LEVELS
import com.prj.domain.model.testscreen.Level
import com.prj.domain.model.testscreen.Source
import com.prj.domain.model.testscreen.TestItem
import com.prj.domain.model.testscreen.TestStatus
import com.prj.japanlib.R
import com.prj.japanlib.feature_jlpttest.components.NoInternetDialog
import com.prj.japanlib.feature_jlpttest.viewmodel.implementation.TestListViewModel
import com.prj.japanlib.uistate.TestScreenUiState


@Composable
fun TestListScreen(
    onTestClick: (String, Int) -> Unit,
    onNavigateBack: () -> Unit
)
{
    val viewModel: TestListViewModel = hiltViewModel()
    var selectedTab by remember { mutableStateOf(0) }
    val testsForLevel by viewModel.testList.collectAsStateWithLifecycle()
    var source by remember { mutableStateOf(Source.CUSTOM) }
    val selectedLevel = remember(selectedTab) {
        when (selectedTab) {
            0 -> Level.N5
            1 -> Level.N4
            2 -> Level.N3
            3 -> Level.N2
            4 -> Level.N1
            else -> Level.N5
        }
    }
    var showNoInternetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        viewModel.getTestsForLevel(source, selectedLevel)
    }

    LaunchedEffect(testsForLevel) {
        showNoInternetDialog = when (testsForLevel) {
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

    TestListScreenContent(
        testsForLevel,
        selectedTab,
        onTabSelected = { index ->
            selectedTab = index
            source = Source.CUSTOM
        },
        onTestClick = onTestClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestListScreenContent(
    testsForLevelState: TestScreenUiState<List<TestItem>> = TestScreenUiState.Empty,
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onTestClick: (String, Int) -> Unit
) {
    Scaffold(
        containerColor = Color(0xFF1C1C1E),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.jlpt_testing_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back */ }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1C1E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1C1C1E),
                contentColor = Color.White,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF0A84FF),
                        height = 2.dp
                    )
                },
                divider = {}
            ) {
                JLPT_LEVELS.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                        text = {
                            Text(
                                title,
                                fontSize = 16.sp,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedTab == index) Color.White else Color(0xFF8E8E93)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (testsForLevelState) {
                is TestScreenUiState.Success -> {
                    val currentTests = testsForLevelState.data
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(currentTests.size) { index ->
                            val testInfo = currentTests[index]
                            TestItemCard(
                                test = testInfo,
                                onTestClick = onTestClick,
                                selectedTab = selectedTab
                            )
                        }
                    }
                }

                is TestScreenUiState.Error -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        testsForLevelState.message?.let {
                            Text(
                                text = it,
                                color = Color.White
                            )
                        }
                    }
                }

                is TestScreenUiState.Loading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        CircularProgressIndicator(color = Color(0xFF0A84FF))
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun TestItemCard(
    test: TestItem,
    selectedTab: Int,
    onTestClick: (String, Int) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onTestClick(test.id, selectedTab) }),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C2E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFFFCC00), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_test),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = test.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Status
                val (statusText, statusColor) = when (test.status) {
                    TestStatus.COMPLETED -> stringResource(id = R.string.status_completed) to Color(0xFF34C759)
                    TestStatus.IN_PROGRESS -> stringResource(id = R.string.status_in_progress) to Color(0xFF0A84FF)
                    TestStatus.NOT_STARTED -> stringResource(id = R.string.status_not_started) to Color(0xFF8E8E93)
                    else -> stringResource(id = R.string.status_not_started) to Color(0xFF8E8E93)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        fontSize = 13.sp,
                        color = statusColor
                    )
                }
            }

            // Arrow
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = Color(0xFF8E8E93),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1C1E)
@Composable
fun PreviewJLPTTestScreen() {
    MaterialTheme {
        TestListScreenContent(
            testsForLevelState = TestScreenUiState.Success(
                listOf(
                    TestItem(
                        id = "1",
                        title = "N1 Mock Test #1",
                        status = TestStatus.COMPLETED
                    ),
                    TestItem(
                        id = "2",
                        title = "N1 Mock Test #2",
                        status = TestStatus.IN_PROGRESS
                    ),
                    TestItem(
                        id = "3",
                        title = "N1 Mock Test #3",
                        status = TestStatus.NOT_STARTED
                    )
                )
            ),
            selectedTab = 0,
            onTestClick = {_, _ ->}
        )
    }
}
