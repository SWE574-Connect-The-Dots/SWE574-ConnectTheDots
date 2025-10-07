package com.yybb.myapplication.data.api

import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("example")
    suspend fun getExample(): Response<Unit>
}
