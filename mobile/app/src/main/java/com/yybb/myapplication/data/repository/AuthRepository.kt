package com.yybb.myapplication.data.repository

import com.yybb.myapplication.data.SessionManager
import com.yybb.myapplication.data.network.ApiService
import com.yybb.myapplication.data.network.dto.LoginRequest
import com.yybb.myapplication.data.network.dto.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun login(loginRequest: LoginRequest): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(loginRequest)
                if (response.isSuccessful) {
                    response.body()?.token?.let {
                        sessionManager.saveAuthToken(it)
                        Result.success(Unit)
                    } ?: Result.failure(Exception("Login failed: Token not found"))
                } else {
                    Result.failure(Exception("Login failed: ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun register(registerRequest: RegisterRequest): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(registerRequest)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Registration failed: ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
