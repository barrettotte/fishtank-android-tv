package com.barrettotte.fishtank.data.repository

import com.barrettotte.fishtank.data.api.FishtankApi
import com.barrettotte.fishtank.util.Logger

/** Repository for fetching user profile data (display name). */
class ProfileRepository(private val api: FishtankApi) {
    companion object {
        private const val TAG = "Profile"
    }

    /** Fetch the display name for a user by their ID. Returns empty string on failure. */
    suspend fun getDisplayName(userId: String): String {
        return try {
            Logger.d(TAG, "Fetching profile for user: $userId")
            val response = api.getProfile(userId)

            if (!response.isSuccessful) {
                Logger.e(TAG, "Profile fetch failed with status: ${response.code()}")
                return ""
            }

            val body = response.body()
            if (body == null) {
                Logger.e(TAG, "Profile response body is null")
                return ""
            }

            // Handle both nested and flat formats
            val profile = body.data?.profile ?: body.profile
            val displayName = profile?.displayName ?: ""

            Logger.d(TAG, "Resolved display name: '$displayName'")
            displayName
        } catch (e: Exception) {
            Logger.e(TAG, "Profile fetch exception: ${e.message}", e)
            ""
        }
    }
}
