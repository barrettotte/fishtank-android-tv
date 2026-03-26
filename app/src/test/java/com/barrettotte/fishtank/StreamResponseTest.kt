package com.barrettotte.fishtank

import com.barrettotte.fishtank.data.model.LiveStreamsResponse
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/** Tests for live streams response parsing. */
class StreamResponseTest {

    private val gson = Gson()

    @Test
    fun `parses flat format with streams and status`() {
        val json = """
        {
            "liveStreams": [
                { "id": "cam-1", "name": "Kitchen" },
                { "id": "cam-2", "name": "Living Room" }
            ],
            "liveStreamStatus": { "cam-1": "online", "cam-2": "offline" },
            "loadBalancer": { "cam-1": "streams-f.fishtank.live", "cam-2": "streams-b.fishtank.live" },
            "region": "us-east"
        }
        """.trimIndent()
        val response = gson.fromJson(json, LiveStreamsResponse::class.java)

        assertNotNull(response.liveStreams)
        assertEquals(2, response.liveStreams?.size)
        assertEquals("online", response.liveStreamStatus?.get("cam-1"))
        assertEquals("offline", response.liveStreamStatus?.get("cam-2"))
        assertEquals("streams-f.fishtank.live", response.loadBalancer?.get("cam-1"))
    }

    @Test
    fun `parses nested data format`() {
        val json = """
        {
            "data": {
                "liveStreams": [{ "id": "cam-1", "name": "Kitchen" }],
                "liveStreamStatus": { "cam-1": "online" },
                "loadBalancer": { "cam-1": "streams-g.fishtank.live" },
                "region": "us-west"
            }
        }
        """.trimIndent()
        val response = gson.fromJson(json, LiveStreamsResponse::class.java)

        assertNotNull(response.data)
        assertEquals(1, response.data?.liveStreams?.size)
        assertEquals("online", response.data?.liveStreamStatus?.get("cam-1"))
    }

    @Test
    fun `parses stream with display_name`() {
        val json = """
        {
            "liveStreams": [{ "id": "cam-1", "name": "cam1", "display_name": "Kitchen Cam" }],
            "liveStreamStatus": {},
            "loadBalancer": {}
        }
        """.trimIndent()
        val response = gson.fromJson(json, LiveStreamsResponse::class.java)

        assertEquals("Kitchen Cam", response.liveStreams?.get(0)?.displayName)
    }

    @Test
    fun `handles empty streams list`() {
        val json = """{ "liveStreams": [], "liveStreamStatus": {}, "loadBalancer": {} }"""
        val response = gson.fromJson(json, LiveStreamsResponse::class.java)

        assertNotNull(response.liveStreams)
        assertEquals(0, response.liveStreams?.size)
    }
}
