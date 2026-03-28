package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.myapplication.data.StockDatabase
import com.example.myapplication.data.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StockDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = Room.databaseBuilder(
        application,
        StockDatabase::class.java,
        "stock_db"
    ).build()

    private val repository = StockRepository(database.stockDao())

    private val _uiState = MutableStateFlow(StockDetailsUiState())
    val uiState: StateFlow<StockDetailsUiState> = _uiState.asStateFlow()

    fun loadStock(symbol: String) {
        viewModelScope.launch {
            _uiState.value = StockDetailsUiState(isLoading = true)

            val result = repository.fetchStockDetails(symbol)

            _uiState.value =
                if (result != null) {
                    StockDetailsUiState(
                        isLoading = false,
                        stock = result
                    )
                } else {
                    StockDetailsUiState(
                        isLoading = false,
                        errorText = "Nie udało się pobrać szczegółów spółki"
                    )
                }
        }
    }
}