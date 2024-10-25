package com.example.conversormoedas

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApiService {
    @GET("http://data.fixer.io/api/latest?access_key=712d0ed55ba215a620001575d466b348")
    fun getExchangeRates(
        @Query("access_key") apiKey: String,
        @Query("base") baseCurrency: String
    ): Call<CurrencyResponse>
}

