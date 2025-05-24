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
    // Updated WisdomList route to include categoryName
    object WisdomList : Screen("wisdom_list?initialTab={initialTab}&categoryName={categoryName}") {
        fun createRoute(initialTabName: String? = null, categoryName: String? = null): String {
            var route = "wisdom_list"
            val params = mutableListOf<String>()
            initialTabName?.let { params.add("initialTab=$it") }
            categoryName?.let { params.add("categoryName=$it") }
            if (params.isNotEmpty()) {
                route += "?" + params.joinToString("&")
            }
            return route
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
                onNavigateToWisdomList = { tabName -> navController.navigate(Screen.WisdomList.createRoute(initialTabName = tabName)) },
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
            arguments = listOf(
                navArgument("initialTab") { type = NavType.StringType; nullable = true },
                navArgument("categoryName") { type = NavType.StringType; nullable = true } // Added categoryName argument
            )
        ) { backStackEntry ->
            val initialTabName = backStackEntry.arguments?.getString("initialTab")
            val categoryNameFilter = backStackEntry.arguments?.getString("categoryName") // Get categoryName

            val initialTabIndex = when (initialTabName?.lowercase()) {
                "all" -> 0
                "active" -> 1
                "queued" -> 2
                "completed" -> 3
                else -> 0
            }
            WisdomListScreen(
                onBackClick = { navController.popBackStack() },
                onWisdomClick = { wisdomId -> navController.navigate(Screen.WisdomDetail.createRoute(wisdomId)) },
                viewModel = mainViewModel,
                onManageCategoriesClick = { navController.navigate(Screen.ManageCategories.route) },
                initialSelectedTabIndex = initialTabIndex,
                categoryFilterName = categoryNameFilter // Pass categoryName to WisdomListScreen
            )
        }

        composable(
            route = Screen.WisdomDetail.route,
            arguments = listOf(
                navArgument("wisdomId") { type = NavType.LongType } // This is the argument name from the route
            )
        ) { backStackEntry ->
            val wisdomIdArg = backStackEntry.arguments?.getLong("wisdomId") ?: -1L // Extracted argument
            WisdomDetailScreen(
                navController = navController, // Passed navController
                initialWisdomId = wisdomIdArg,  // Corrected parameter name
                viewModel = mainViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.ManageCategories.route) {
            ManageCategoriesScreen(
                navController = navController, // Pass NavController
                viewModel = mainViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
