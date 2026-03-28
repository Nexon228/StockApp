package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.myapplication.data.StockDatabase
import com.example.myapplication.data.StockRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StockListViewModel(application: Application) : AndroidViewModel(application) {

    private val database = Room.databaseBuilder(
        application,
        StockDatabase::class.java,
        "stock_db"
    ).build()

    private val repository = StockRepository(database.stockDao())

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
        val cleanedTicker = _uiState.value.tickerText.trim().uppercase()

        if (cleanedTicker.isEmpty()) {
            _uiState.value = _uiState.value.copy(messageText = "Wpisz ticker spółki")
            return
        }

        if (_uiState.value.stockList.any { it.symbol == cleanedTicker }) {
            _uiState.value = _uiState.value.copy(messageText = "Ta spółka jest już na liście")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val stock = repository.fetchStock(cleanedTicker)

            _uiState.value =
                if (stock != null) {
                    repository.saveStock(stock)

                    _uiState.value.copy(
                        stockList = _uiState.value.stockList + stock,
                        tickerText = "",
                        messageText = "Dodano spółkę ${stock.symbol}",
                        lastUpdateTime = getCurrentTimeString(),
                        isLoading = false
                    )
                } else {
                    _uiState.value.copy(
                        messageText = "Nie znaleziono spółki o tickerze $cleanedTicker",
                        isLoading = false
                    )
                }
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
            val updatedList = _uiState.value.stockList.map { stock ->
                repository.refreshStock(stock) ?: stock
            }

            repository.saveAllStocks(updatedList)

            _uiState.value = _uiState.value.copy(
                stockList = updatedList,
                messageText = "Ceny zaktualizowane",
                lastUpdateTime = getCurrentTimeString()
            )
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
                delay(10000)

                if (_uiState.value.stockList.isNotEmpty()) {
                    val updatedList = _uiState.value.stockList.map { stock ->
                        repository.refreshStock(stock) ?: stock
                    }

                    repository.saveAllStocks(updatedList)

                    _uiState.value = _uiState.value.copy(
                        stockList = updatedList,
                        messageText = "Ceny zaktualizowane",
                        lastUpdateTime = getCurrentTimeString()
                    )
                }
            }
        }
    }

    private fun getCurrentTimeString(): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }
}