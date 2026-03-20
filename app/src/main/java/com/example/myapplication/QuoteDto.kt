package com.example.myapplication

data class QuoteDto(
    val c: Double?,   // current price
    val d: Double?,   // change
    val dp: Double?,  // percent change
    val h: Double?,   // high
    val l: Double?,   // low
    val o: Double?,   // open
    val pc: Double?,  // previous close
    val t: Long?      // timestamp
)