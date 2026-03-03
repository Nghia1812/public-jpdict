package com.prj.japanlib.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.prj.japanlib.feature_settings.LoginScreen
import com.prj.japanlib.feature_settings.SettingsScreen
import com.prj.japanlib.feature_settings.UserInfoScreen
import com.prj.japanlib.navigation.DestinationRoute.DICTIONARY_ROUTE
import com.prj.japanlib.navigation.DestinationRoute.LOGIN_ROUTE
import com.prj.japanlib.navigation.DestinationRoute.SETTINGS_ROUTE
import com.prj.japanlib.navigation.DestinationRoute.USERINFO_ROUTE

fun NavGraphBuilder.profileGraph(navController: NavHostController) {
    composable(SETTINGS_ROUTE) {
        SettingsScreen(
            onProfileClick = { isLoggedIn ->
                if (isLoggedIn) {
                    navController.navigate(USERINFO_ROUTE)
                } else {
                    navController.navigate(LOGIN_ROUTE)
                }
            }
        )
    }

    composable(LOGIN_ROUTE) {
        LoginScreen(
            onLoginSuccess = { navController.navigate(DICTIONARY_ROUTE) },
            onBackClick = {navController.popBackStack()}
        )
    }

    composable(USERINFO_ROUTE) {
        UserInfoScreen(
            onSignOutClick = { navController.navigate(DICTIONARY_ROUTE) },
            onBackClick = {navController.popBackStack()}
        )
    }
}