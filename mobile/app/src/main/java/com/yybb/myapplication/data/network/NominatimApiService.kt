package com.yybb.myapplication.data.network

import com.yybb.myapplication.data.network.dto.NominatimResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimApiService {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String,
        @Query("addressdetails") addressdetails: Int,
        @Query("limit") limit: Int,
        @Query("accept-language") acceptLanguage: String
    ): Response<List<NominatimResponse>>
}

