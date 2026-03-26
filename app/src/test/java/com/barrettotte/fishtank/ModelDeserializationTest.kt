package com.barrettotte.fishtank

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

import com.barrettotte.fishtank.data.model.LiveStreamsResponse
import com.barrettotte.fishtank.data.model.LoginResponse
import com.barrettotte.fishtank.data.model.ProfileResponse
import com.barrettotte.fishtank.data.model.ValidateResponse

/** Tests that API response models deserialize correctly from known JSON formats. */
class ModelDeserializationTest {

    private val gson = Gson()

    @Test
    fun `LoginResponse parses session with tokens`() {
        val json = """
        {
            "session": {
                "user": { "id": "uuid-123", "email": "test@example.com" },
                "access_token": "abc.def.ghi",
                "live_stream_token": "xyz.123.456"
            }
        }
        """.trimIndent()
        val response = gson.fromJson(json, LoginResponse::class.java)

        assertNotNull(response.session)
        assertEquals("uuid-123", response.session?.user?.id)
        assertEquals("test@example.com", response.session?.user?.email)
        assertEquals("abc.def.ghi", response.session?.accessToken)
        assertEquals("xyz.123.456", response.session?.liveStreamToken)
    }

    @Test
    fun `ValidateResponse parses session as JWT string`() {
        val json = """{"session": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjMifQ.sig"}"""
        val response = gson.fromJson(json, ValidateResponse::class.java)

        assertNotNull(response.session)
        assertEquals("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjMifQ.sig", response.session)
    }

    @Test
    fun `LiveStreamsResponse parses flat format`() {
        val json = """
        {
            "liveStreams": [
                { "id": "stream-1", "name": "Camera 1" },
                { "id": "stream-2", "name": "Camera 2" }
            ],
            "liveStreamStatus": { "stream-1": "online", "stream-2": "offline" },
            "loadBalancer": { "stream-1": "streams-f.fishtank.live" },
            "region": "us-east"
        }
        """.trimIndent()
        val response = gson.fromJson(json, LiveStreamsResponse::class.java)

        assertNotNull(response.liveStreams)
        assertEquals(2, response.liveStreams?.size)
        assertEquals("stream-1", response.liveStreams?.get(0)?.id)
        assertEquals("Camera 1", response.liveStreams?.get(0)?.name)
        assertEquals("online", response.liveStreamStatus?.get("stream-1"))
        assertEquals("offline", response.liveStreamStatus?.get("stream-2"))
        assertEquals("streams-f.fishtank.live", response.loadBalancer?.get("stream-1"))
        assertEquals("us-east", response.region)
    }

    @Test
    fun `LiveStreamsResponse parses nested data format`() {
        val json = """
        {
            "data": {
                "liveStreams": [
                    { "id": "stream-1", "name": "Camera 1" }
                ],
                "liveStreamStatus": { "stream-1": "online" },
                "loadBalancer": { "stream-1": "streams-b.fishtank.live" },
                "region": "us-west"
            }
        }
        """.trimIndent()
        val response = gson.fromJson(json, LiveStreamsResponse::class.java)

        assertNotNull(response.data)
        assertEquals(1, response.data?.liveStreams?.size)
        assertEquals("stream-1", response.data?.liveStreams?.get(0)?.id)
        assertEquals("online", response.data?.liveStreamStatus?.get("stream-1"))
        assertEquals("us-west", response.data?.region)
    }

    @Test
    fun `ProfileResponse parses display name`() {
        val json = """
        {
            "data": {
                "profile": {
                    "displayName": "fishwatcher99"
                }
            }
        }
        """.trimIndent()
        val response = gson.fromJson(json, ProfileResponse::class.java)

        assertNotNull(response.data?.profile)
        assertEquals("fishwatcher99", response.data?.profile?.displayName)
    }
}
