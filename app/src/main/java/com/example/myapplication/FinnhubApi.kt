package com.example.myapplication

import retrofit2.http.GET
import retrofit2.http.Query

interface FinnhubApi {

    @GET("stock/profile2")
    suspend fun getCompanyProfile(
        @Query("symbol") symbol: String,
        @Query("token") token: String
    ): CompanyProfileDto

    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
        @Query("token") token: String
    ): QuoteDto
}