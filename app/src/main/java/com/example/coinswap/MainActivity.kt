package com.example.coinswap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coinswap.navigation.AppNavigation
import com.example.coinswap.presentation.mainScreen.MainScreenViewModel
import com.example.coinswap.ui.theme.CoinSwapTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CoinSwapTheme {

                    AppNavigation()

            }
        }
    }
}
