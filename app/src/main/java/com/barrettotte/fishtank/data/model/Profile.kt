package com.barrettotte.fishtank.data.model

/** Response from GET /profile/{userId}. */
data class ProfileResponse(
    val data: ProfileData?,
)

/** Profile data wrapper. */
data class ProfileData(
    val profile: Profile?,
)

/** User profile with display name. */
data class Profile(
    val displayName: String?,
)
