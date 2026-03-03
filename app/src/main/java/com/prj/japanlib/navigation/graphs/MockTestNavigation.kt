package com.prj.japanlib.navigation.graphs

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.prj.domain.model.testscreen.LearningState
import com.prj.domain.model.testscreen.Level
import com.prj.japanlib.navigation.DestinationRoute.EXAM_ROUTE
import com.prj.japanlib.navigation.DestinationRoute.MOCK_TEST_QUESTIONS
import com.prj.japanlib.navigation.DestinationRoute.MOCK_TEST_LIST
import com.prj.japanlib.feature_jlpttest.TestListScreen
import com.prj.japanlib.feature_jlpttest.FlashcardListScreen
import com.prj.japanlib.feature_jlpttest.FlashcardDetailsScreen
import com.prj.japanlib.feature_jlpttest.JLPTTestScreen
import com.prj.japanlib.feature_jlpttest.LearningModeScreen
import com.prj.japanlib.navigation.DestinationRoute.FLASHCARD_DETAILS
import com.prj.japanlib.navigation.DestinationRoute.FLASHCARD_LIST
import com.prj.japanlib.navigation.DestinationRoute.LEARNING_MODE
import com.prj.domain.model.testscreen.WordListType
import com.prj.japanlib.feature_jlpttest.FlashcardOverviewScreen
import com.prj.japanlib.feature_jlpttest.ResultScreen
import com.prj.japanlib.feature_jlpttest.ReviewAnswersScreen
import com.prj.japanlib.feature_jlpttest.TestSectionsScreen
import com.prj.japanlib.feature_jlpttest.viewmodel.implementation.JLPTTestViewModel
import com.prj.japanlib.navigation.DestinationRoute.FLASHCARD_OVERVIEW
import com.prj.japanlib.navigation.DestinationRoute.MOCK_TEST_RESULT
import com.prj.japanlib.navigation.DestinationRoute.MOCK_TEST_SECTIONS
import com.prj.japanlib.navigation.DestinationRoute.REVIEW_ANSWER
import timber.log.Timber

sealed class MockTestNavigation(val route: String) {
    data object TestList : MockTestNavigation(MOCK_TEST_LIST)
    data object JLPTTestQuestions : MockTestNavigation(MOCK_TEST_QUESTIONS) {
        fun createRoute(level: Level, id: String, skill: String) = "test/$level/$id/$skill"
        const val LEVEL_ARG = "level"
        const val ID_ARG = "id"
        const val SKILL_ARG = "skill"
    }

    data object JLPTTestResult : MockTestNavigation(MOCK_TEST_RESULT) {
        fun createRoute(level: Level, id: String, skill: String) = "result/$level/$id/$skill"
        const val LEVEL_ARG = "level"
        const val ID_ARG = "id"
        const val SKILL_ARG = "skill"
    }

    data object JLPTTestSections : MockTestNavigation(MOCK_TEST_SECTIONS) {
        fun createRoute(level: Level, id: String) = "test/$level/$id"
        const val LEVEL_ARG = "level"
        const val ID_ARG = "id"
    }

    data object LearningMode : MockTestNavigation(LEARNING_MODE)

    data object FlashCardList : MockTestNavigation(FLASHCARD_LIST)

    data object FlashCardDetails : MockTestNavigation(FLASHCARD_DETAILS) {
        fun createRoute(id: String, listType: WordListType, learningState: LearningState) =
            "flashcard_detail/$id/$listType/$learningState"

        const val ID_ARG = "id"
        const val TYPE_ARG = "listType"
        const val LEARNING_STATE_ARG = "learningState"
    }

    data object FlashOverview : MockTestNavigation(FLASHCARD_OVERVIEW) {
        fun createRoute(id: String, listType: WordListType) = "flashcard_overview/$id/$listType"
        const val ID_ARG = "id"
        const val TYPE_ARG = "listType"
    }
}

fun NavGraphBuilder.testGraph(navController: NavHostController) {
    navigation(
        startDestination = MockTestNavigation.LearningMode.route,
        route = EXAM_ROUTE
    ) {
        composable(MockTestNavigation.LearningMode.route) {
            LearningModeScreen(
                onNavigateToJLPT = { navController.navigate(MockTestNavigation.TestList.route) },
                onNavigateToFlashcard = {
                    navController.navigate(MockTestNavigation.FlashCardList.route)
                }
            )
        }

        composable(
            route = MockTestNavigation.TestList.route,
        ) {
            TestListScreen(
                onTestClick = { id, selectedTab ->
                    val level = when (selectedTab) {
                        0 -> Level.N5
                        1 -> Level.N4
                        2 -> Level.N3
                        3 -> Level.N2
                        4 -> Level.N1
                        else -> Level.N5
                    }
                    navController.navigate(
                        MockTestNavigation.JLPTTestSections.createRoute(
                            level = level,
                            id = id,
                        )
                    )
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = MockTestNavigation.FlashCardList.route,
            deepLinks = listOf(navDeepLink { uriPattern = "myapp://flashcard_list" })
        ) {
            FlashcardListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToThemeDetail = { listId ->
                    navController.navigate(
                        MockTestNavigation.FlashOverview.createRoute(
                            listId,
                            WordListType.THEME
                        )
                    )
                },
                onNavigateToJlptDetail = { listId ->
                    navController.navigate(
                        MockTestNavigation.FlashOverview.createRoute(
                            listId,
                            WordListType.JLPT
                        )
                    )
                },
                onNavigateToCustomDetail = { listId ->
                    navController.navigate(
                        MockTestNavigation.FlashOverview.createRoute(
                            listId,
                            WordListType.CUSTOM
                        )
                    )
                }
            )
        }

        composable(
            route = MockTestNavigation.FlashCardDetails.route,
            arguments = listOf(
                navArgument(MockTestNavigation.FlashCardDetails.ID_ARG) {
                    type = NavType.StringType
                },
                navArgument(MockTestNavigation.FlashCardDetails.TYPE_ARG) {
                    type = NavType.StringType
                },
                navArgument(MockTestNavigation.FlashCardDetails.LEARNING_STATE_ARG) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val listId =
                backStackEntry.arguments?.getString(MockTestNavigation.FlashCardDetails.ID_ARG)
                    ?: return@composable
            val listTypeString =
                backStackEntry.arguments?.getString("listType") ?: return@composable
            val listType = WordListType.valueOf(listTypeString)
            val learningStateString = backStackEntry.arguments?.getString("learningState")
                ?: return@composable
            val learningState = LearningState.valueOf(learningStateString)
            FlashcardDetailsScreen(
                listId = listId,
                listType = listType,
                learningState = learningState,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = MockTestNavigation.FlashOverview.route,
            arguments = listOf(
                navArgument(MockTestNavigation.FlashOverview.ID_ARG) { type = NavType.StringType },
                navArgument(MockTestNavigation.FlashOverview.TYPE_ARG) { type = NavType.StringType }
            )) { backStackEntry ->
            val listId =
                backStackEntry.arguments?.getString(MockTestNavigation.FlashOverview.ID_ARG)
                    ?: return@composable
            val listTypeString =
                backStackEntry.arguments?.getString(MockTestNavigation.FlashOverview.TYPE_ARG)
                    ?: return@composable
            val listType = WordListType.valueOf(listTypeString)
            FlashcardOverviewScreen(
                listId = listId,
                listType = listType,
                onNavigateBack = { navController.popBackStack() },
                onStartFlashcards = {
                    navController.navigate(
                        MockTestNavigation.FlashCardDetails.createRoute(
                            listId,
                            listType,
                            LearningState.NONE
                        )
                    )
                },
                onReviewMissedWords = {
                    navController.navigate(
                        MockTestNavigation.FlashCardDetails.createRoute(
                            listId,
                            listType,
                            LearningState.FORGOT
                        )
                    )
                },
                onLearnRemaining = {
                    navController.navigate(
                        MockTestNavigation.FlashCardDetails.createRoute(
                            listId,
                            listType,
                            LearningState.NOT_LEARNT_YET
                        )
                    )
                }
            )
        }

        composable(
            route = MockTestNavigation.JLPTTestSections.route,
            arguments = listOf(
                navArgument(MockTestNavigation.JLPTTestSections.LEVEL_ARG) {
                    type = NavType.StringType
                },
                navArgument(MockTestNavigation.JLPTTestSections.ID_ARG) {
                    type = NavType.StringType
                })
        ) { backStackEntry ->
            val levelString =
                backStackEntry.arguments?.getString(MockTestNavigation.JLPTTestSections.LEVEL_ARG)
                    ?: return@composable
            val id = backStackEntry.arguments?.getString(MockTestNavigation.JLPTTestSections.ID_ARG)
                ?: return@composable
            val level = Level.fromString(levelString)
                ?: return@composable
            TestSectionsScreen(
                id,
                level,
                onNavigateBack = { navController.popBackStack() },
                onSectionClick = { _, skill, _ ->
                    MockTestNavigation.JLPTTestQuestions.createRoute(
                        level,
                        id,
                        skill
                    ).let {
                        navController.navigate(it)
                        Timber.i("Route: $it")
                    }
                }
            )
        }

        composable(
            route = MockTestNavigation.JLPTTestQuestions.route,
            arguments = listOf(
                navArgument(MockTestNavigation.JLPTTestQuestions.LEVEL_ARG) {
                    type = NavType.StringType
                },
                navArgument(MockTestNavigation.JLPTTestQuestions.ID_ARG) {
                    type = NavType.StringType
                },
                navArgument(MockTestNavigation.JLPTTestQuestions.SKILL_ARG) {
                    type = NavType.StringType
                })
        ) { backStackEntry ->
            val levelString =
                backStackEntry.arguments?.getString(MockTestNavigation.JLPTTestQuestions.LEVEL_ARG)
                    ?: return@composable
            val id =
                backStackEntry.arguments?.getString(MockTestNavigation.JLPTTestQuestions.ID_ARG)
                    ?: return@composable
            val skill =
                backStackEntry.arguments?.getString(MockTestNavigation.JLPTTestQuestions.SKILL_ARG)
                    ?: return@composable
            val level = Level.fromString(levelString)
                ?: return@composable

            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(EXAM_ROUTE)
            }
            val sharedViewModel: JLPTTestViewModel = hiltViewModel(parentEntry)

            JLPTTestScreen(
                viewModel = sharedViewModel,
                onNavigateBack = {
                    sharedViewModel.resetExam(level)
                    navController.popBackStack()
                    sharedViewModel.resetTest()
                },
                level = level,
                id = id,
                skill = skill,
                onNavigateToResult = {
                    navController.navigate(
                        MockTestNavigation.JLPTTestResult.createRoute(
                            level,
                            id,
                            skill
                        )
                    )
                }
            )
        }

        composable(MOCK_TEST_RESULT) { backStackEntry ->
            val levelString =
                backStackEntry.arguments?.getString(MockTestNavigation.JLPTTestResult.LEVEL_ARG)
                    ?: return@composable
            val id = backStackEntry.arguments?.getString(MockTestNavigation.JLPTTestResult.ID_ARG)
                ?: return@composable
            val skill =
                backStackEntry.arguments?.getString(MockTestNavigation.JLPTTestResult.SKILL_ARG)
                    ?: return@composable
            val level = Level.fromString(levelString)
                ?: return@composable
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(EXAM_ROUTE)
            }
            val sharedViewModel: JLPTTestViewModel = hiltViewModel(parentEntry)
            ResultScreen(
                viewModel = sharedViewModel,
                onNavigateBack = {
                    navController.navigate(MockTestNavigation.TestList.route) {
                        popUpTo(MockTestNavigation.TestList.route) {
                            inclusive = true // Remove everything including the old TestList to refresh
                        }
                    }
                    sharedViewModel.resetExam(level)
                },
                onRetry = {
                    // Go back to the questions screen
                    navController.navigate(
                        MockTestNavigation.JLPTTestQuestions.createRoute(level, id, skill)
                    ) {
                        // Pop up to Sections so that Result and the previous Question screen are gone
                        popUpTo(MockTestNavigation.JLPTTestSections.route) {
                            inclusive = false
                        }
                    }
                    // Reset state so the new Questions Screen starts fresh
                    sharedViewModel.resetExam(level)
                },
                onReviewAnswers = {
                    navController.navigate(REVIEW_ANSWER)
                }
            )
        }

        composable(route = REVIEW_ANSWER) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(EXAM_ROUTE)
            }
            val sharedViewModel: JLPTTestViewModel = hiltViewModel(parentEntry)
            ReviewAnswersScreen(
                viewModel = sharedViewModel,
                onNavigateBack = { navController.popBackStack() },
                onRetry = { navController.popBackStack() }
            )
        }
    }
}