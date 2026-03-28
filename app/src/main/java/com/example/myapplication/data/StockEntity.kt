package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stocks")
data class StockEntity(
    @PrimaryKey
    val symbol: String,
    val name: String,
    val price: Double,
    val industry: String,
    val country: String,
    val changePercent: Double,
    val logoUrl: String
)