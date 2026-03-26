package com.barrettotte.fishtank.data.api

import com.barrettotte.fishtank.data.repository.PreferencesRepository
import com.barrettotte.fishtank.util.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Singleton factory for creating the Retrofit API client. */
object ApiClient {

    /** Build and return the FishtankApi instance with auth and logging interceptors. */
    fun create(preferencesRepository: PreferencesRepository): FishtankApi {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(preferencesRepository))
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(FishtankApi::class.java)
    }
}
