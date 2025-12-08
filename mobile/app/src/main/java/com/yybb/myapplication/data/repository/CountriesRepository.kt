package com.yybb.myapplication.data.repository

import com.yybb.myapplication.data.network.CountriesApiService
import com.yybb.myapplication.data.network.dto.CountryPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CountriesRepository @Inject constructor(
    private val countriesApiService: CountriesApiService
) {
    suspend fun getCountries(): Result<List<CountryPosition>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = countriesApiService.getCountries()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.data)
                } else {
                    Result.failure(Exception("Failed to fetch countries: ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getCities(country: String): Result<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = countriesApiService.getCities(country)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.data)
                } else {
                    Result.failure(Exception("Failed to fetch cities: ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

