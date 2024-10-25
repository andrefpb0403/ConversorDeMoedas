package com.example.conversormoedas

data class CurrencyResponse(
    val rates: Map<String, Double>,
    val base: String,
    val date: String
)

