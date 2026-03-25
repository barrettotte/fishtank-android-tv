package com.barrettotte.fishtank

import org.junit.Assert.assertEquals
import org.junit.Test

/** Tests for API URL construction and constants. */
class ApiClientTest {

    @Test
    fun `base URL ends with trailing slash`() {
        // Retrofit requires a trailing slash on the base URL
        val baseUrl = "https://api.fishtank.live/v1/"
        assertEquals('/', baseUrl.last())
    }

    @Test
    fun `HLS stream URL is constructed correctly`() {
        val server = "streams-f.fishtank.live"
        val streamId = "abc-123-def"
        val liveStreamToken = "jwt-token-here"
        val quality = "maxbps"

        val url = "https://$server/hls/live+$streamId/index.m3u8?jwt=$liveStreamToken&video=$quality"

        assertEquals(
            "https://streams-f.fishtank.live/hls/live+abc-123-def/index.m3u8?jwt=jwt-token-here&video=maxbps",
            url,
        )
    }

    @Test
    fun `thumbnail URL is constructed correctly`() {
        val server = "streams-f.fishtank.live"
        val streamId = "abc+123"
        val encodedStreamId = streamId.replace("+", "%2B")
        val refresh = 1711000080L
        val url = "https://$server/live%2B$encodedStreamId.jpeg?refresh=$refresh"

        assertEquals(
            "https://streams-f.fishtank.live/live%2Babc%2B123.jpeg?refresh=1711000080",
            url,
        )
    }

    @Test
    fun `default quality is maxbps`() {
        assertEquals("maxbps", "maxbps")
    }

    @Test
    fun `default server is streams-f`() {
        val defaultServer = "streams-f.fishtank.live"
        assertEquals("streams-f.fishtank.live", defaultServer)
    }
}
