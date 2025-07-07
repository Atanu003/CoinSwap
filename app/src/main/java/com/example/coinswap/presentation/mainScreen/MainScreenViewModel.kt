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
                updateSelectedCurrency(event.value)
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
            "." -> if (currentValue.contains(".")) currentValue else "$currentValue."
            else -> {
                if (currentValue == "0.00" || currentValue == "0" || currentValue == "0.") {
                    // Reset to new digit (starting fresh)
                    value
                } else if (currentValue.startsWith("0") && !currentValue.startsWith("0.")) {
                    // Remove leading zero if user tries to type 08 or 012
                    value
                } else {
                    currentValue + value
                }
            }
        }


        // Safely parse to double
        val inputAmount = updatedValue.toDoubleOrNull() ?: 0.0

        // Get both rates from the map (relative to API base currency)
        val fromRate = state.currencyRates[state.fromCurrencyCode]?.rate
        val toRate = state.currencyRates[state.toCurrencyCode]?.rate

        if (fromRate == null || toRate == null) {
            state = state.copy(error = "Invalid currency selection")
            return
        }

        // Universal conversion formula:
        val convertedAmount = inputAmount / fromRate * toRate

        // Format
        val formatted = DecimalFormat("#.##").format(convertedAmount)

        // Update state
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

    private fun updateSelectedCurrency(code: String) {
        if (state.selection == SelectionState.FROM) {
            state = state.copy(fromCurrencyCode = code)
        } else if (state.selection == SelectionState.TO) {
            state = state.copy(toCurrencyCode = code)
        }

        recalculateConversion()
    }

    private fun recalculateConversion() {
        val fromAmount = state.fromCurrencyValue.toDoubleOrNull() ?: 0.0
        val fromRate = state.currencyRates[state.fromCurrencyCode]?.rate
        val toRate = state.currencyRates[state.toCurrencyCode]?.rate

        if (fromRate == null || toRate == null) {
            state = state.copy(error = "Invalid currency selection")
            return
        }

        val convertedAmount = fromAmount / fromRate * toRate
        val formatted = DecimalFormat("#.##").format(convertedAmount)

        state = state.copy(toCurrencyValue = formatted)
    }




}
