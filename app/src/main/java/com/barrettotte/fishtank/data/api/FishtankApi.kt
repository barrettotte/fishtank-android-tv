package com.barrettotte.fishtank.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

import com.barrettotte.fishtank.data.model.LiveStreamsResponse
import com.barrettotte.fishtank.data.model.LoginRequest
import com.barrettotte.fishtank.data.model.LoginResponse
import com.barrettotte.fishtank.data.model.ProfileResponse
import com.barrettotte.fishtank.data.model.ValidateResponse

/** Retrofit interface for all Fishtank.live API endpoints. */
interface FishtankApi {

    /** Log in with email and password. Returns session with tokens. */
    @POST("auth/log-in")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    /** Validate the current access token. 200 means valid. */
    @GET("auth")
    suspend fun validateToken(): Response<ValidateResponse>

    /** Fetch all live camera streams with status and load balancer info. */
    @GET("live-streams")
    suspend fun getLiveStreams(): Response<LiveStreamsResponse>

    /** Fetch user profile by ID to get display name. */
    @GET("profile/{userId}")
    suspend fun getProfile(@Path("userId") userId: String): Response<ProfileResponse>
}
