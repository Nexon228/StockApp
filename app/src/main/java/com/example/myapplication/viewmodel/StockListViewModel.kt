package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.app.StockApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StockListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as StockApplication).repository

    private val _uiState = MutableStateFlow(StockListUiState())
    val uiState: StateFlow<StockListUiState> = _uiState.asStateFlow()

    init {
        loadSavedStocks()
        startAutoRefresh()
    }

    fun onTickerTextChange(value: String) {
        _uiState.value = _uiState.value.copy(tickerText = value)
    }

    fun addStock() {
        val symbol = _uiState.value.tickerText.trim().uppercase()

        if (symbol.isEmpty()) {
            showMessage("Wpisz ticker spółki")
            return
        }

        if (_uiState.value.stockList.any { it.symbol == symbol }) {
            showMessage("Ta spółka jest już na liście")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                messageText = "Pobieram dane..."
            )

            val stock = repository.fetchStock(symbol)

            if (stock == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    messageText = "Nie znaleziono spółki o tickerze $symbol"
                )
                return@launch
            }

            repository.saveStock(stock)

            _uiState.value = _uiState.value.copy(
                stockList = _uiState.value.stockList + stock,
                tickerText = "",
                isLoading = false,
                messageText = "Dodano spółkę ${stock.symbol}",
                lastUpdateTime = currentTime()
            )
        }
    }

    fun deleteStock(symbol: String) {
        viewModelScope.launch {
            repository.deleteStock(symbol)

            _uiState.value = _uiState.value.copy(
                stockList = _uiState.value.stockList.filter { it.symbol != symbol },
                messageText = "Usunięto spółkę $symbol"
            )
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            refreshStocks(showLoading = true)
        }
    }

    private fun loadSavedStocks() {
        viewModelScope.launch {
            val savedStocks = repository.getSavedStocks()
            _uiState.value = _uiState.value.copy(stockList = savedStocks)
        }
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(10_000)
                refreshStocks(showLoading = false)
            }
        }
    }

    private suspend fun refreshStocks(showLoading: Boolean) {
        val currentStocks = _uiState.value.stockList

        if (currentStocks.isEmpty()) {
            if (showLoading) {
                showMessage("Lista jest pusta")
            }
            return
        }

        if (showLoading) {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                messageText = "Odświeżam ceny..."
            )
        }

        val updatedStocks = currentStocks.map { stock ->
            repository.refreshStock(stock) ?: stock
        }

        repository.saveAllStocks(updatedStocks)

        _uiState.value = _uiState.value.copy(
            stockList = updatedStocks,
            isLoading = false,
            messageText = "Ceny zaktualizowane",
            lastUpdateTime = currentTime()
        )
    }

    private fun showMessage(message: String) {
        _uiState.value = _uiState.value.copy(messageText = message)
    }

    private fun currentTime(): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }
}
