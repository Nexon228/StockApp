package com.example.myapplication.viewmodel

import com.example.myapplication.StockDetails

data class StockDetailsUiState(
    val isLoading: Boolean = true,
    val stock: StockDetails? = null,
    val errorText: String = ""
)