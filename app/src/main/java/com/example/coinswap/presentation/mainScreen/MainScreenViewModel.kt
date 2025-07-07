package com.example.coinswap.presentation.mainScreen

import android.icu.text.DecimalFormat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coinswap.domain.model.Resource
import com.example.coinswap.domain.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
        private val repository: CurrencyRepository
): ViewModel(){
        var state by mutableStateOf(MainScreenState())

    init {
        getCurrencyRateList()
    }

    fun onEvent(event: MainScreenEvent){
        when(event){
            MainScreenEvent.FromCurrencySelect -> {
                state = state.copy(selection = SelectionState.FROM)
            }

            MainScreenEvent.ToCurrencySelect -> {
                state = state.copy(selection = SelectionState.TO)
            }

            MainScreenEvent.SwapIconClicked -> {
                state = state.copy(
                    fromCurrencyCode = state.toCurrencyCode,
                    fromCurrencyValue = state.toCurrencyValue,
                    toCurrencyCode = state.fromCurrencyCode,
                    toCurrencyValue = state.fromCurrencyValue
                )
            }

            is MainScreenEvent.BottomSheetItemClicked -> {
                updateCurrencyValue(value = event.value)
            }
            is MainScreenEvent.NumberButtonClicked -> {
                updateCurrencyValue(event.value)
            }

        }
    }

    private fun getCurrencyRateList(){
        viewModelScope.launch {
            repository
                .getCurrencyRateList()
                .collectLatest { result ->
                    state = when(result){
                        is Resource.Success -> {
                            state.copy(
                                currencyRates = result.data?.associateBy { it.code } ?: emptyMap(),
                                error = null
                            )
                        }

                        is Resource.Failure -> {
                            state.copy(
                                currencyRates = result.data?.associateBy { it.code } ?: emptyMap(),
                                error = result.message
                            )
                        }
                    }
                }
        }
    }

    private fun updateCurrencyValue(value: String) {
        val currentValue = when (state.selection) {
            SelectionState.FROM -> state.fromCurrencyValue
            SelectionState.TO -> state.toCurrencyValue
        }

        val updatedValue = when (value) {
            "C" -> "0.00"
            "." -> if (currentValue.contains(".")) currentValue else currentValue + "."
            else -> if (currentValue == "0.00") value else currentValue + value
        }

        val fromRate = state.currencyRates[state.fromCurrencyCode]?.rate ?: 1.0
        val toRate = state.currencyRates[state.toCurrencyCode]?.rate ?: 1.0
        val formatted = DecimalFormat("#.##").format(
            if (state.selection == SelectionState.FROM)
                (updatedValue.toDoubleOrNull() ?: 0.0) / fromRate * toRate
            else
                (updatedValue.toDoubleOrNull() ?: 0.0) / toRate * fromRate
        )

        state = when (state.selection) {
            SelectionState.FROM -> state.copy(
                fromCurrencyValue = updatedValue,
                toCurrencyValue = formatted
            )
            SelectionState.TO -> state.copy(
                toCurrencyValue = updatedValue,
                fromCurrencyValue = formatted
            )
        }
    }

}
