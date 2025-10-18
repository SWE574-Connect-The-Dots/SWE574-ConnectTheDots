package com.yybb.myapplication.data.network

import com.yybb.myapplication.data.network.dto.LoginRequest
import com.yybb.myapplication.data.network.dto.LoginResponse
import com.yybb.myapplication.data.network.dto.ProfileResponse
import com.yybb.myapplication.data.network.dto.RegisterRequest
import com.yybb.myapplication.data.network.dto.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("api/register/")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>

    @POST("api/login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/profiles/me/")
    suspend fun getProfile(): Response<ProfileResponse>
    
    @GET("api/profiles/{username}/user_profile/")
    suspend fun getProfileByUsername(@Path("username") username: String): Response<ProfileResponse>

    @PUT("api/profiles/update_profile/")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ProfileResponse>
}
