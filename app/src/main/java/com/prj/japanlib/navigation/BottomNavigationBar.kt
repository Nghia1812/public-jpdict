package com.prj.japanlib.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.prj.japanlib.R
import com.prj.japanlib.navigation.DestinationRoute.DICTIONARY_ROUTE
import com.prj.japanlib.navigation.DestinationRoute.EXAM_ROUTE
import com.prj.japanlib.navigation.DestinationRoute.SETTINGS_ROUTE
import com.prj.japanlib.navigation.DestinationRoute.TRANSLATOR_ROUTE
import com.prj.japanlib.ui.theme.outlineVariantLight
import com.prj.japanlib.ui.theme.primaryLight

@Composable
fun bottomNavItems(): List<BottomNavItem> = listOf(
    BottomNavItem(
        label = stringResource(R.string.dictionary_item),
        icon = ImageVector.vectorResource(id = R.drawable.dictionary_ic),
        route = DICTIONARY_ROUTE
    ),
    BottomNavItem(
        label = stringResource(R.string.learning_item),
        icon = ImageVector.vectorResource(id = R.drawable.learning_ic),
        route = EXAM_ROUTE
    ),
    BottomNavItem(
        label = stringResource(R.string.translator_item),
        icon = ImageVector.vectorResource(id = R.drawable.translate_ic),
        route = TRANSLATOR_ROUTE
    ),
    BottomNavItem(
        label = stringResource(R.string.settings_item),
        icon = ImageVector.vectorResource(id = R.drawable.setting_ic),
        route = SETTINGS_ROUTE
    ),
)

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar(
        modifier = Modifier.border(
            BorderStroke(width = 1.dp, color = outlineVariantLight.copy(alpha = 0.3f))
        ),
        containerColor = Color(0xFF1B2434),
        tonalElevation = 0.dp,
    ) {
        // Observe backstack
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val selectedColor = primaryLight.copy(alpha = 0.75f)
        val unselectedColor = Color.White.copy(alpha = 0.55f)

        bottomNavItems().forEach { navItem ->
            val isSelected =
                currentDestination?.hierarchy?.any { it.route == navItem.route } == true
            val itemColor = if (isSelected) selectedColor else unselectedColor
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(navItem.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = navItem.icon,
                        contentDescription = navItem.label,
                        tint = itemColor
                    )
                },
                label = {
                    Text(
                        text = navItem.label,
                        color = itemColor,
                        fontSize = 12.sp
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                )
            )
        }
    }
}