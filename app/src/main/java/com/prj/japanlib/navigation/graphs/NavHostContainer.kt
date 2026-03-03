package com.prj.japanlib.navigation.graphs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.prj.japanlib.navigation.DestinationRoute.DICTIONARY_ROUTE
import com.prj.japanlib.navigation.DestinationRoute.TRANSLATOR_ROUTE
import com.prj.japanlib.feature_translator.TranslatorScreen

@Composable
fun NavHostContainer(navController: NavHostController, padding: PaddingValues) {
    NavHost(navController = navController,
        // set the start destination as home
        startDestination = DICTIONARY_ROUTE,

        modifier = Modifier.padding(paddingValues = padding),

        builder = {
            dictionaryGraph(navController)

            profileGraph(navController)

            // route : Exam
            testGraph(navController)

            // route : Translator
            composable(TRANSLATOR_ROUTE) {
                TranslatorScreen()
            }
        })
}