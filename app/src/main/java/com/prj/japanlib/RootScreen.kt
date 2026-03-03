package com.prj.japanlib

import android.app.Activity
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import com.prj.domain.model.settingsscreen.AppLanguage
import com.prj.japanlib.feature_settings.viewmodel.implementation.SettingsScreenViewModel
import com.prj.japanlib.navigation.BottomNavigationBar
import com.prj.japanlib.navigation.graphs.NavHostContainer
import com.prj.japanlib.ui.theme.JapanlibTheme
import timber.log.Timber

@Composable
fun RootScreen(
    navController: NavHostController,
    settingsViewModel: SettingsScreenViewModel = hiltViewModel()
){
    val fontScale by settingsViewModel.fontScale.collectAsStateWithLifecycle()

    JapanlibTheme {
        CompositionLocalProvider(
            LocalDensity provides Density(
                density = LocalDensity.current.density,
                fontScale = fontScale.scale
            )
        ) {
            Scaffold(bottomBar = { BottomNavigationBar(navController) },
                content = { padding ->
                    NavHostContainer(navController = navController, padding = padding)
                }
            )
        }
    }
}