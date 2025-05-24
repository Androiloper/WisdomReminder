package com.example.wisdomreminder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wisdomreminder.ui.main.MainScreen
import com.example.wisdomreminder.ui.main.MainViewModel
import com.example.wisdomreminder.ui.settings.SettingsScreen
import com.example.wisdomreminder.ui.settings.SettingsViewModel
import com.example.wisdomreminder.ui.wisdom.WisdomDetailScreen
import com.example.wisdomreminder.ui.wisdom.WisdomListScreen
import com.example.wisdomreminder.ui.categories.ManageCategoriesScreen

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Settings : Screen("settings")
    object WisdomList : Screen("wisdom_list?initialTab={initialTab}") {
        fun createRoute(initialTabName: String? = null): String {
            return initialTabName?.let { "wisdom_list?initialTab=$it" } ?: "wisdom_list"
        }
    }
    object WisdomDetail : Screen("wisdom_detail/{wisdomId}") {
        fun createRoute(wisdomId: Long): String = "wisdom_detail/$wisdomId"
    }
    object ManageCategories : Screen("manage_categories")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                viewModel = mainViewModel,
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onNavigateToManageCategories = { navController.navigate(Screen.ManageCategories.route) },
                onNavigateToWisdomList = { tabName -> navController.navigate(Screen.WisdomList.createRoute(tabName)) },
                onWisdomClick = { wisdomId -> navController.navigate(Screen.WisdomDetail.createRoute(wisdomId)) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = settingsViewModel
            )
        }

        composable(
            route = Screen.WisdomList.route,
            arguments = listOf(navArgument("initialTab") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val initialTabName = backStackEntry.arguments?.getString("initialTab")
            val initialTabIndex = when (initialTabName?.lowercase()) {
                "all" -> 0
                "active" -> 1
                "queued" -> 2
                "completed" -> 3
                else -> 0 // Default to "ALL" if no or unknown tab is specified
            }
            WisdomListScreen(
                onBackClick = { navController.popBackStack() },
                onWisdomClick = { wisdomId -> navController.navigate(Screen.WisdomDetail.createRoute(wisdomId)) },
                viewModel = mainViewModel,
                onManageCategoriesClick = { navController.navigate(Screen.ManageCategories.route) },
                initialSelectedTabIndex = initialTabIndex
            )
        }

        composable(
            route = Screen.WisdomDetail.route,
            arguments = listOf(
                navArgument("wisdomId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val wisdomId = backStackEntry.arguments?.getLong("wisdomId") ?: -1L
            WisdomDetailScreen(
                onBackClick = { navController.popBackStack() },
                wisdomId = wisdomId,
                viewModel = mainViewModel
            )
        }

        composable(Screen.ManageCategories.route) {
            ManageCategoriesScreen(
                viewModel = mainViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}