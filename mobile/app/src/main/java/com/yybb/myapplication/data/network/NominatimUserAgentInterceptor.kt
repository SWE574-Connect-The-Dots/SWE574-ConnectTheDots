package com.yybb.myapplication.data.network

import okhttp3.Interceptor
import okhttp3.Response

class NominatimUserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("User-Agent", "ConnectTheDots/1.0 (Android App)")
            .build()
        return chain.proceed(request)
    }
}


