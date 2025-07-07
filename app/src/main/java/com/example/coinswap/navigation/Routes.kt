package com.example.coinswap.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Routes{

    @Serializable
    data object Splash: Routes()

    @Serializable
    data object MainScreen: Routes()

}