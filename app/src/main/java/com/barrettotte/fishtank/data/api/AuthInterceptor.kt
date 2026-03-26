package com.barrettotte.fishtank.data.api

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

import com.barrettotte.fishtank.data.repository.PreferencesRepository

/** OkHttp interceptor that attaches the Bearer token to authenticated requests. */
class AuthInterceptor(private val preferencesRepository: PreferencesRepository) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth header for login endpoint
        if (originalRequest.url.encodedPath.endsWith("auth/log-in")) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking { 
            preferencesRepository.getAccessToken() 
        }
        val authenticatedRequest = if (token.isNotEmpty()) {
            originalRequest.newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            originalRequest
        }

        return chain.proceed(authenticatedRequest)
    }
}
