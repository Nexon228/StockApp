package com.example.myapplication.app

import android.app.Application
import androidx.room.Room
import com.example.myapplication.data.local.StockDatabase
import com.example.myapplication.data.repository.StockRepository

class StockApplication : Application() {

    private val database: StockDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            StockDatabase::class.java,
            "stock_db"
        ).build()
    }

    val repository: StockRepository by lazy {
        StockRepository(database.stockDao())
    }
}
