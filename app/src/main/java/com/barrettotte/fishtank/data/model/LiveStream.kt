package com.barrettotte.fishtank.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response from GET /live-streams.
 * Handles both flat format and nested "data" wrapper format.
 */
data class LiveStreamsResponse(
    val data: LiveStreamsData?,
    val liveStreams: List<LiveStream>?,
    val liveStreamStatus: Map<String, String>?,
    val loadBalancer: Map<String, String>?,
    val region: String?,
)

/** Nested data wrapper (some API responses wrap in "data"). */
data class LiveStreamsData(
    val liveStreams: List<LiveStream>?,
    val liveStreamStatus: Map<String, String>?,
    val loadBalancer: Map<String, String>?,
    val region: String?,
)

/** A single camera live stream. */
data class LiveStream(
    val id: String,
    val name: String?,
    @SerializedName("display_name") val displayName: String?,
    val description: String?,
)
