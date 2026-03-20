package com.example.myapplication

data class Stock(
    val symbol: String,
    val name: String,
    val price: Double,
    val industry: String = "Brak danych",
    val country: String = "Brak danych",
    val changePercent: Double = 0.0,
    val logoUrl: String = ""
)