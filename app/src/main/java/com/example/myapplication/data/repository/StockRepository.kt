package com.example.myapplication.data.repository

import com.example.myapplication.data.api.FinnhubApi
import com.example.myapplication.data.local.StockDao
import com.example.myapplication.data.local.StockEntity
import com.example.myapplication.model.Stock
import com.example.myapplication.model.StockDetails
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StockRepository(
    private val stockDao: StockDao
) {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://finnhub.io/api/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(FinnhubApi::class.java)

    private val apiToken = "d6na2dpr01qlnj39pj40d6na2dpr01qlnj39pj4g"

    suspend fun getSavedStocks(): List<Stock> {
        return stockDao.getAllStocks().map { it.toStock() }
    }

    suspend fun saveStock(stock: Stock) {
        stockDao.insertStock(stock.toEntity())
    }

    suspend fun saveAllStocks(stocks: List<Stock>) {
        stockDao.insertAll(stocks.map { it.toEntity() })
    }

    suspend fun deleteStock(symbol: String) {
        stockDao.deleteStock(symbol)
    }

    suspend fun fetchStock(symbol: String): Stock? {
        return try {
            val profile = api.getCompanyProfile(symbol, apiToken)
            val quote = api.getQuote(symbol, apiToken)

            if (profile.name.isNullOrBlank()) {
                null
            } else {
                Stock(
                    symbol = profile.ticker ?: symbol,
                    name = profile.name,
                    price = quote.c ?: 0.0,
                    industry = profile.finnhubIndustry ?: "Brak danych",
                    country = profile.country ?: "Brak danych",
                    changePercent = quote.dp ?: 0.0,
                    logoUrl = profile.logo ?: ""
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun refreshStock(stock: Stock): Stock? {
        return try {
            val quote = api.getQuote(stock.symbol, apiToken)

            stock.copy(
                price = quote.c ?: stock.price,
                changePercent = quote.dp ?: stock.changePercent
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchStockDetails(symbol: String): StockDetails? {
        return try {
            val profile = api.getCompanyProfile(symbol, apiToken)
            val quote = api.getQuote(symbol, apiToken)

            if (profile.name.isNullOrBlank()) {
                null
            } else {
                StockDetails(
                    symbol = profile.ticker ?: symbol,
                    name = profile.name ?: "Brak danych",
                    price = quote.c ?: 0.0,
                    changePercent = quote.dp ?: 0.0,
                    industry = profile.finnhubIndustry ?: "Brak danych",
                    country = profile.country ?: "Brak danych",
                    logoUrl = profile.logo ?: "",
                    exchange = profile.exchange ?: "Brak danych",
                    ipo = profile.ipo ?: "Brak danych",
                    marketCap = profile.marketCapitalization ?: 0.0,
                    webUrl = profile.weburl ?: "Brak danych",
                    currency = profile.currency ?: "Brak danych",
                    open = quote.o ?: 0.0,
                    high = quote.h ?: 0.0,
                    low = quote.l ?: 0.0,
                    previousClose = quote.pc ?: 0.0
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun StockEntity.toStock(): Stock {
        return Stock(
            symbol = symbol,
            name = name,
            price = price,
            industry = industry,
            country = country,
            changePercent = changePercent,
            logoUrl = logoUrl
        )
    }

    private fun Stock.toEntity(): StockEntity {
        return StockEntity(
            symbol = symbol,
            name = name,
            price = price,
            industry = industry,
            country = country,
            changePercent = changePercent,
            logoUrl = logoUrl
        )
    }
}
