package com.barrettotte.fishtank.util

/** Utility for constructing stream and thumbnail URLs. */
object StreamUrls {
    private const val THUMBNAIL_INTERVAL_SECONDS = Constants.THUMBNAIL_REFRESH_MS / 1000

    /** Build the HLS stream URL for a camera. */
    fun buildStreamUrl(
        server: String,
        streamId: String,
        liveStreamToken: String,
        quality: String = Constants.DEFAULT_QUALITY,
    ): String {
        return "https://$server/hls/live+$streamId/index.m3u8?jwt=$liveStreamToken&video=$quality"
    }

    /** Build the thumbnail URL for a camera. */
    fun buildThumbnailUrl(server: String, streamId: String): String {
        val encodedId = streamId.replace("+", "%2B")
        val refresh = roundToInterval(System.currentTimeMillis() / 1000)
        return "https://$server/live%2B$encodedId.jpeg?refresh=$refresh"
    }

    /** Round a timestamp to the nearest interval for cache busting. */
    fun roundToInterval(timestampSeconds: Long): Long {
        return timestampSeconds / THUMBNAIL_INTERVAL_SECONDS * THUMBNAIL_INTERVAL_SECONDS
    }
}
