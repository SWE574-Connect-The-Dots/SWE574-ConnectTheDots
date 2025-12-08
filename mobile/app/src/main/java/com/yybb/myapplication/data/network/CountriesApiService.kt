package com.yybb.myapplication.data.network

import com.yybb.myapplication.data.network.dto.CitiesResponse
import com.yybb.myapplication.data.network.dto.CountriesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CountriesApiService {
    @GET("api/v0.1/countries/positions")
    suspend fun getCountries(): Response<CountriesResponse>

    @GET("api/v0.1/countries/cities/q")
    suspend fun getCities(@Query("country") country: String): Response<CitiesResponse>
}

