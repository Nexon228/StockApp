package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.app.StockApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StockDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as StockApplication).repository

    private val _uiState = MutableStateFlow(StockDetailsUiState())
    val uiState: StateFlow<StockDetailsUiState> = _uiState.asStateFlow()

    fun loadStock(symbol: String) {
        viewModelScope.launch {
            _uiState.value = StockDetailsUiState(isLoading = true)

            val stock = repository.fetchStockDetails(symbol)

            _uiState.value = if (stock != null) {
                StockDetailsUiState(stock = stock, isLoading = false)
            } else {
                StockDetailsUiState(
                    isLoading = false,
                    errorText = "Nie udało się pobrać szczegółów spółki"
                )
            }
        }
    }
}
