package com.prj.japanlib.navigation.graphs

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.prj.japanlib.feature_dictionary.CustomVocabularyScreen
import com.prj.japanlib.navigation.DestinationRoute.DICTIONARY_DETAIL
import com.prj.japanlib.navigation.DestinationRoute.DICTIONARY_ROUTE
import com.prj.japanlib.navigation.DestinationRoute.DICTIONARY_SEARCH
import com.prj.japanlib.navigation.DestinationRoute.HOME_ROUTE
import com.prj.japanlib.feature_dictionary.DictionaryScreen
import com.prj.japanlib.feature_dictionary.SearchScreen
import com.prj.japanlib.feature_dictionary.JlptVocabularyScreen
import com.prj.japanlib.feature_dictionary.SearchResultScreen
import com.prj.japanlib.feature_dictionary.ThemeVocabularyScreen
import com.prj.japanlib.feature_dictionary.WordDetailScreen
import com.prj.japanlib.navigation.DestinationRoute.DICTIONARY_CUSTOM_LIST
import com.prj.japanlib.navigation.DestinationRoute.DICTIONARY_JLPT_LIST
import com.prj.japanlib.navigation.DestinationRoute.DICTIONARY_THEME_LIST
import com.prj.japanlib.navigation.DestinationRoute.SEARCH_RESULT
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class DictionaryNavigation(val route: String) {


    data object Home : DictionaryNavigation(HOME_ROUTE)
    data object Search : DictionaryNavigation(DICTIONARY_SEARCH)
    data object JlptList : DictionaryNavigation(DICTIONARY_JLPT_LIST) {
        fun createRoute(level: String) = "jlptList/$level"
        const val LEVEL_ARG = "level"
    }

    data object CustomList : DictionaryNavigation(DICTIONARY_CUSTOM_LIST) {
        fun createRoute(name: String, listId: String) = "customList/$name/$listId"
        const val NAME_ARG = "name"
        const val ID_ARG = "listId"
    }

    data object ThemeList : DictionaryNavigation(DICTIONARY_THEME_LIST) {
        fun createRoute(theme: Int, themeName: String) = "themeList/$theme/$themeName"
        const val THEME_ARG = "theme"
        const val THEME_NAME_ARG = "themeName"
    }

    data object WordDetail : DictionaryNavigation(DICTIONARY_DETAIL) {
        fun createRoute(wordId: Int) = "wordDetail/$wordId"
        const val WORD_ID_ARG = "wordId"
    }

    data object SearchResult : DictionaryNavigation(SEARCH_RESULT) {
        fun createRoute(searchQuery: String) = "searchResult/$searchQuery"
        const val SEARCH_ARG = "searchQuery"
    }
}

fun NavGraphBuilder.dictionaryGraph(navController: NavHostController) {
    navigation(
        startDestination = DictionaryNavigation.Home.route,
        route = DICTIONARY_ROUTE
    ) { // Nested navigation
        val onWordClick: (Int) -> Unit = { wordId ->
            navController.navigate(DictionaryNavigation.WordDetail.createRoute(wordId))
        }

        // route : Home
        composable(DictionaryNavigation.Home.route) {
            DictionaryScreen(
                onSearchClick = { navController.navigate(DictionaryNavigation.Search.route) },
                onJlptWordListClick = { level ->
                    navController.navigate(DictionaryNavigation.JlptList.createRoute(level))
                },
                onCustomWordListClick = { listName, listId ->
                    navController.navigate(DictionaryNavigation.CustomList.createRoute(listName, listId))
                },
                onThemeListClick = { themeId, themeName ->
                    navController.navigate(DictionaryNavigation.ThemeList.createRoute(themeId, themeName))
                }
            )

        }

        composable(DictionaryNavigation.Search.route) {
            SearchScreen(
                onDetectedKanjiClick = { kanji ->
                    navController.navigate(DictionaryNavigation.SearchResult.createRoute(kanji))
                },
                onSearchClick = { query ->
                    val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
                    navController.navigate(DictionaryNavigation.SearchResult.createRoute(encoded))
                }
            )
        }

        composable(
            route = DictionaryNavigation.SearchResult.route,
            arguments = listOf(navArgument(DictionaryNavigation.SearchResult.SEARCH_ARG) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val encodedArg =
                backStackEntry.arguments?.getString(DictionaryNavigation.SearchResult.SEARCH_ARG)
                    ?: return@composable
            val searchQuery = URLDecoder.decode(encodedArg, StandardCharsets.UTF_8.toString())
            SearchResultScreen(
                searchQuery = searchQuery,
                onWordClick = onWordClick,
                onBackClick = {navController.popBackStack()}
            )
        }

        vocabularyScreen(
            route = DictionaryNavigation.JlptList.route,
            argumentName = DictionaryNavigation.JlptList.LEVEL_ARG
        ) { level ->
            JlptVocabularyScreen(
                level = level,
                onWordClick = onWordClick,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = DictionaryNavigation.CustomList.route,
            arguments = listOf( navArgument(DictionaryNavigation.CustomList.NAME_ARG){
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val listName = backStackEntry.arguments?.getString(DictionaryNavigation.CustomList.NAME_ARG)
            val listId = backStackEntry.arguments?.getString(DictionaryNavigation.CustomList.ID_ARG)

            CustomVocabularyScreen(
                listId = listId,
                listName = listName ?: "",
                onWordClick = onWordClick,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = DictionaryNavigation.ThemeList.route,
            arguments = listOf(
                navArgument(DictionaryNavigation.ThemeList.THEME_ARG) {
                    type = NavType.IntType
                },
                navArgument(DictionaryNavigation.ThemeList.THEME_NAME_ARG) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val themeId =
                backStackEntry.arguments?.getInt(DictionaryNavigation.ThemeList.THEME_ARG)
                    ?: return@composable
            val themeName =
                backStackEntry.arguments?.getString(DictionaryNavigation.ThemeList.THEME_NAME_ARG)
                    ?: return@composable
            ThemeVocabularyScreen(
                themeName = themeName,
                themeId = themeId,
                onWordClick = onWordClick,
                onBackClick = { navController.popBackStack() }
            )
        }

        // route: Detail Screen
        composable(
            route = DictionaryNavigation.WordDetail.route,
            arguments = listOf(navArgument(DictionaryNavigation.WordDetail.WORD_ID_ARG) {
                type = NavType.IntType
            }),
            deepLinks = listOf(navDeepLink { uriPattern = "myapp://${DictionaryNavigation.WordDetail.route}" })
        ) { backStackEntry ->
            val wordId =
                backStackEntry.arguments?.getInt(DictionaryNavigation.WordDetail.WORD_ID_ARG)
                    ?: return@composable
            WordDetailScreen(
                wordId = wordId,
                onBack = { navController.popBackStack() },
                onRelatedWordClick = onWordClick
            )
        }
    }
}

private fun NavGraphBuilder.vocabularyScreen(
    route: String,
    argumentName: String,
    content: @Composable (argument: String) -> Unit
) {
    composable(
        route = route,
        arguments = listOf(navArgument(argumentName) {
            type = NavType.StringType
        })
    ) { backStackEntry ->
        val argument = backStackEntry.arguments?.getString(argumentName)
            ?: return@composable
        content(argument)
    }
}
