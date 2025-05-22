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
// Import for ManageCategoriesScreen was added in the previous conceptual step
import com.example.wisdomreminder.ui.categories.ManageCategoriesScreen // Ensure this path is correct

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Settings : Screen("settings")
    object WisdomList : Screen("wisdom_list")
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
        startDestination = Screen.Main.route // "main"
    ) {
        composable(Screen.Main.route) { // route = "main"
            MainScreen(
                viewModel = mainViewModel,
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onManageCategoriesClick = { navController.navigate(Screen.ManageCategories.route) }, // Add this
                onWisdomListClick = { navController.navigate(Screen.WisdomList.route) },
                onWisdomClick = { wisdomId -> navController.navigate(Screen.WisdomDetail.createRoute(wisdomId)) }
            )
        }

        composable(Screen.Settings.route) { // route = "settings"
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = settingsViewModel
            )
        }

        composable(Screen.WisdomList.route) { // route = "wisdom_list"
            WisdomListScreen(
                onBackClick = { navController.popBackStack() },
                onWisdomClick = { wisdomId -> navController.navigate(Screen.WisdomDetail.createRoute(wisdomId)) },
                viewModel = mainViewModel,
                onManageCategoriesClick = { navController.navigate(Screen.ManageCategories.route) }
            )
        }

        composable(
            route = Screen.WisdomDetail.route, // route = "wisdom_detail/{wisdomId}"
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

        composable(Screen.ManageCategories.route) { // route = "manage_categories"
            ManageCategoriesScreen(
                viewModel = mainViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}