package com.barrettotte.fishtank.data.model

import com.google.gson.annotations.SerializedName

/** Request body for POST /auth/log-in. */
data class LoginRequest(
    val email: String,
    val password: String,
)

/** Response from POST /auth/log-in. */
data class LoginResponse(
    val session: SessionData?,
)

/** Session data containing user info and tokens. */
data class SessionData(
    val user: SessionUser?,
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("live_stream_token") val liveStreamToken: String?,
)

/** User info within the session. */
data class SessionUser(
    val id: String?,
    val email: String?,
)

/** Response from GET /auth (token validation). Session is a JWT string, not an object. */
data class ValidateResponse(
    val session: String?,
)
