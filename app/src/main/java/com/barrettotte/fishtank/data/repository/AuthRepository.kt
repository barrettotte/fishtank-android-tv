package com.barrettotte.fishtank.data.repository

import com.barrettotte.fishtank.data.api.FishtankApi
import com.barrettotte.fishtank.data.model.LoginRequest
import com.barrettotte.fishtank.util.JwtDecoder
import com.barrettotte.fishtank.util.Logger

/** Repository for authentication operations (login, validate, logout). */
class AuthRepository(
    private val api: FishtankApi,
    private val preferencesRepository: PreferencesRepository,
    private val profileRepository: ProfileRepository,
) {
    companion object {
        private const val TAG = "Auth"
    }

    /** Log in with email and password. Stores tokens and fetches display name on success. */
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            Logger.d(TAG, "Logging in with email: $email")
            val response = api.login(LoginRequest(email, password))

            if (!response.isSuccessful) {
                Logger.e(TAG, "Login failed with status: ${response.code()}")
                return Result.failure(Exception("Login failed: ${response.code()}"))
            }

            val session = response.body()?.session
            if (session == null) {
                Logger.e(TAG, "Login response has null session")
                return Result.failure(Exception("Login failed: empty session"))
            }

            val accessToken = session.accessToken
            if (accessToken == null) {
                Logger.e(TAG, "Session missing access_token")
                return Result.failure(Exception("Login failed: missing access token"))
            }

            val liveStreamToken = session.liveStreamToken
            if (liveStreamToken == null) {
                Logger.e(TAG, "Session missing live_stream_token")
                return Result.failure(Exception("Login failed: missing live stream token"))
            }

            Logger.d(TAG, "Login successful, fetching display name...")

            // Fetch display name from profile using user ID from JWT
            val displayName = fetchDisplayName(accessToken)
            Logger.d(TAG, "Display name: '$displayName'")

            preferencesRepository.saveSession(accessToken, liveStreamToken, displayName)
            preferencesRepository.saveCredentials(email, password)
            Logger.d(TAG, "Session saved to DataStore")

            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e(TAG, "Login exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    /** Validate the cached access token. Refreshes tokens and display name if needed. */
    suspend fun validateToken(): Boolean {
        return try {
            val token = preferencesRepository.getAccessToken()
            if (token.isEmpty()) {
                Logger.d(TAG, "No cached token found")
                return false
            }
            Logger.d(TAG, "Validating cached token...")
            val response = api.validateToken()
            Logger.d(TAG, "Token validation result: ${response.code()}")

            if (response.isSuccessful) {
                // GET /auth may return refreshed tokens - save them if present
                val session = response.body()?.session
                if (session?.accessToken != null && session.liveStreamToken != null) {
                    Logger.d(TAG, "Refreshed tokens from GET /auth")

                    val displayName = preferencesRepository.getDisplayName().ifEmpty {
                        fetchDisplayName(session.accessToken)
                    }
                    preferencesRepository.saveSession(
                        session.accessToken,
                        session.liveStreamToken,
                        displayName,
                    )
                } else {
                    // No refreshed tokens - just fetch display name if missing
                    val cachedName = preferencesRepository.getDisplayName()
                    if (cachedName.isEmpty()) {
                        Logger.d(TAG, "Display name missing, fetching from profile...")

                        val displayName = fetchDisplayName(token)
                        if (displayName.isNotEmpty()) {
                            val liveStreamToken = preferencesRepository.getLiveStreamToken()
                            preferencesRepository.saveSession(token, liveStreamToken, displayName)
                        }
                    }
                }
            }

            response.isSuccessful
        } catch (e: Exception) {
            Logger.e(TAG, "Token validation exception: ${e.message}", e)
            false
        }
    }

    /** Refresh the live stream token by re-logging in. Call before playing a stream. */
    suspend fun refreshLiveStreamToken(): String {
        Logger.d(TAG, "Refreshing live stream token...")
        val email = preferencesRepository.getEmail()
        val password = preferencesRepository.getPassword()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            Logger.d(TAG, "Re-logging in to get fresh token...")
            login(email, password)
        } else {
            Logger.d(TAG, "No stored credentials, validating existing token...")
            validateToken()
        }
        return preferencesRepository.getLiveStreamToken()
    }

    /** Get stored email for auto-login. */
    suspend fun getStoredEmail(): String = preferencesRepository.getEmail()

    /** Get stored password for auto-login. */
    suspend fun getStoredPassword(): String = preferencesRepository.getPassword()

    /** Clear stored auth data. */
    suspend fun logout() {
        Logger.d(TAG, "Logging out, clearing session")
        preferencesRepository.clearSession()
    }

    /** Extract user ID from JWT and fetch display name from profile API. */
    private suspend fun fetchDisplayName(accessToken: String): String {
        val userId = JwtDecoder.getUserId(accessToken)
        if (userId == null) {
            Logger.e(TAG, "Failed to extract user ID from JWT")
            return ""
        }
        Logger.d(TAG, "Extracted user ID: $userId")
        return profileRepository.getDisplayName(userId)
    }
}
