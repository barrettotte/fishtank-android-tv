package com.barrettotte.fishtank.data.model

/** Response from GET /profile/{userId}. Handles both nested and flat formats. */
data class ProfileResponse(
    val data: ProfileData?,
    val profile: Profile?,
)

/** Profile data wrapper for nested format. */
data class ProfileData(
    val profile: Profile?,
)

/** User profile with display name. */
data class Profile(
    val displayName: String?,
)
