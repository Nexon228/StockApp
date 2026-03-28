package com.example.myapplication.viewmodel

import com.example.myapplication.Stock

data class StockListUiState(
    val tickerText: String = "",
    val stockList: List<Stock> = emptyList(),
    val messageText: String = "",
    val lastUpdateTime: String = "",
    val isLoading: Boolean = false
)