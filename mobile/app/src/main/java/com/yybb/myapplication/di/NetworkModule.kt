package com.yybb.myapplication.di

import android.content.Context
import com.yybb.myapplication.R
import com.yybb.myapplication.data.SessionManager
import com.yybb.myapplication.data.network.ApiService
import com.yybb.myapplication.data.network.AuthInterceptor
import com.yybb.myapplication.data.network.CountriesApiService
import com.yybb.myapplication.data.network.NominatimApiService
import com.yybb.myapplication.data.network.NominatimUserAgentInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthInterceptor(sessionManager: SessionManager): AuthInterceptor {
        return AuthInterceptor(sessionManager)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .serializeNulls()
            .create()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson, @ApplicationContext context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(context.getString(R.string.base_url))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    @Named("nominatim")
    fun provideNominatimRetrofit(): Retrofit {
        val userAgentInterceptor = NominatimUserAgentInterceptor()
        val client = OkHttpClient.Builder()
            .addInterceptor(userAgentInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNominatimApiService(
        @Named("nominatim") nominatimRetrofit: Retrofit
    ): NominatimApiService {
        return nominatimRetrofit.create(NominatimApiService::class.java)
    }

    @Provides
    @Singleton
    @Named("countries")
    fun provideCountriesRetrofit(): Retrofit {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://countriesnow.space/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideCountriesApiService(
        @javax.inject.Named("countries") retrofit: Retrofit
    ): CountriesApiService {
        return retrofit.create(CountriesApiService::class.java)
    }
}
