package com.example.wisdomreminder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wisdomreminder.ui.main.MainScreen
import com.example.wisdomreminder.ui.main.MainViewModel
import com.example.wisdomreminder.ui.settings.SettingsScreen
import com.example.wisdomreminder.ui.wisdom.WisdomDetailScreen
import com.example.wisdomreminder.ui.wisdom.WisdomListScreen

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Settings : Screen("settings")
    object WisdomList : Screen("wisdom_list")
    object WisdomDetail : Screen("wisdom_detail/{wisdomId}") {
        fun createRoute(wisdomId: Long): String = "wisdom_detail/$wisdomId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                viewModel = mainViewModel,
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onWisdomListClick = { navController.navigate(Screen.WisdomList.route) },
                onWisdomClick = { wisdomId -> navController.navigate(Screen.WisdomDetail.createRoute(wisdomId)) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = mainViewModel
            )
        }

        composable(Screen.WisdomList.route) {
            WisdomListScreen(
                onBackClick = { navController.popBackStack() },
                onWisdomClick = { wisdomId -> navController.navigate(Screen.WisdomDetail.createRoute(wisdomId)) },
                viewModel = mainViewModel
            )
        }

        composable(
            route = Screen.WisdomDetail.route,
            arguments = listOf(
                navArgument("wisdomId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val wisdomId = backStackEntry.arguments?.getLong("wisdomId") ?: -1L
            WisdomDetailScreen(
                onBackClick = { navController.popBackStack() },
                wisdomId = wisdomId,
                viewModel = mainViewModel
            )
        }
    }
}