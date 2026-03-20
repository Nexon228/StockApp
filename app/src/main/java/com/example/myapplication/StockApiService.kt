package com.example.myapplication

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private val retrofit = Retrofit.Builder()
    .baseUrl("https://finnhub.io/api/v1/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

private val finnhubApi = retrofit.create(FinnhubApi::class.java)

suspend fun fetchStockFromApi(symbol: String): Stock? {
    val token = "d6na2dpr01qlnj39pj40d6na2dpr01qlnj39pj4g"

    return try {
        val profile = finnhubApi.getCompanyProfile(
            symbol = symbol,
            token = token
        )

        val quote = finnhubApi.getQuote(
            symbol = symbol,
            token = token
        )

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

suspend fun refreshStockPrice(stock: Stock): Stock? {
    val token = "d6na2dpr01qlnj39pj40d6na2dpr01qlnj39pj4g"

    return try {
        val quote = finnhubApi.getQuote(
            symbol = stock.symbol,
            token = token
        )

        stock.copy(
            price = quote.c ?: stock.price,
            changePercent = quote.dp ?: stock.changePercent
        )
    } catch (e: Exception) {
        null
    }
}