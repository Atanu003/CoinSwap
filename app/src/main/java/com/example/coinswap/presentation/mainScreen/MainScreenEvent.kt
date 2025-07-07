package com.example.coinswap.presentation.mainScreen

sealed class MainScreenEvent {
    data object FromCurrencySelect: MainScreenEvent()
    data object ToCurrencySelect: MainScreenEvent()
    data object SwapIconClicked: MainScreenEvent()
    data class BottomSheetItemClicked(val value: String): MainScreenEvent()
    data class NumberButtonClicked(val value: String): MainScreenEvent()
}