package com.barrettotte.fishtank

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

import com.barrettotte.fishtank.util.Constants
import com.barrettotte.fishtank.util.StreamUrls

/** Tests for stream and thumbnail URL construction. */
class StreamUrlsTest {

    @Test
    fun `buildStreamUrl constructs correct HLS URL`() {
        val url = StreamUrls.buildStreamUrl(
            server = "streams-f.fishtank.live",
            streamId = "abc-123",
            liveStreamToken = "jwt-token",
            quality = "maxbps",
        )
        assertEquals(
            "https://streams-f.fishtank.live/hls/live+abc-123/index.m3u8?jwt=jwt-token&video=maxbps",
            url,
        )
    }

    @Test
    fun `buildStreamUrl uses default quality`() {
        val url = StreamUrls.buildStreamUrl(
            server = "streams-b.fishtank.live",
            streamId = "xyz",
            liveStreamToken = "token",
        )
        assertTrue(url.contains("video=maxbps"))
    }

    @Test
    fun `buildThumbnailUrl encodes plus signs`() {
        val url = StreamUrls.buildThumbnailUrl("streams-f.fishtank.live", "abc+def")
        assertTrue(url.contains("live%2Babc%2Bdef.jpeg"))
    }

    @Test
    fun `buildThumbnailUrl includes refresh parameter`() {
        val url = StreamUrls.buildThumbnailUrl("streams-f.fishtank.live", "stream-1")
        assertTrue(url.contains("?refresh="))
    }

    @Test
    fun `roundToInterval rounds down to 90 second boundary`() {
        assertEquals(0L, StreamUrls.roundToInterval(89L))
        assertEquals(90L, StreamUrls.roundToInterval(90L))
        assertEquals(90L, StreamUrls.roundToInterval(179L))
        assertEquals(180L, StreamUrls.roundToInterval(180L))
    }

    @Test
    fun `default server is streams-f`() {
        assertEquals("streams-f.fishtank.live", Constants.DEFAULT_SERVER)
    }

    @Test
    fun `default quality is maxbps`() {
        assertEquals("maxbps", Constants.DEFAULT_QUALITY)
    }
}
