package com.yybb.myapplication.data.network

import com.yybb.myapplication.data.SessionManager
import com.yybb.myapplication.util.NavigationEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            sessionManager.authToken.first()
        }
        val request = chain.request().newBuilder()
        if (token != null) {
            request.addHeader("Authorization", "Bearer $token")
        }

        val response = chain.proceed(request.build())

        if (response.code == 401 || response.code == 403) {
            runBlocking {
                sessionManager.clearAuthToken()
                NavigationEvent.emit(NavigationEvent.Event.NavigateToLogin)
            }
        }
        return response
    }
}
