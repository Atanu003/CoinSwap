package com.example.coinswap.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.coinswap.presentation.mainScreen.MainScreen
import com.example.coinswap.presentation.mainScreen.MainScreenViewModel
import com.example.coinswap.presentation.splashScreen.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.Splash) {
        composable<Routes.Splash> {
            SplashScreen(navController)
        }

        composable<Routes.MainScreen> {
            val viewModel: MainScreenViewModel = hiltViewModel()
            MainScreen(
                state = viewModel.state,
                onEvent = viewModel::onEvent
            )
        }
    }
}
