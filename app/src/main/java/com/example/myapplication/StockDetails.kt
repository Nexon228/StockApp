package com.example.myapplication

data class StockDetails(
    val symbol: String,
    val name: String,
    val price: Double,
    val changePercent: Double,
    val industry: String,
    val country: String,
    val logoUrl: String,
    val exchange: String,
    val ipo: String,
    val marketCap: Double,
    val webUrl: String,
    val currency: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val previousClose: Double
)