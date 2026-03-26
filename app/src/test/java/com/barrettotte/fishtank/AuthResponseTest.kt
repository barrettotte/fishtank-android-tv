package com.barrettotte.fishtank

import com.barrettotte.fishtank.data.model.LoginResponse
import com.barrettotte.fishtank.data.model.ValidateResponse
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Tests for auth-related API response parsing edge cases. */
class AuthResponseTest {

    private val gson = Gson()

    @Test
    fun `LoginResponse handles null session`() {
        val json = """{}"""
        val response = gson.fromJson(json, LoginResponse::class.java)

        assertNull(response.session)
    }

    @Test
    fun `LoginResponse handles session with null tokens`() {
        val json = """
        {
            "session": {
                "user": { "id": "123" }
            }
        }
        """.trimIndent()
        val response = gson.fromJson(json, LoginResponse::class.java)

        assertNotNull(response.session)
        assertNull(response.session?.accessToken)
        assertNull(response.session?.liveStreamToken)
    }

    @Test
    fun `ValidateResponse handles null session`() {
        val json = """{}"""
        val response = gson.fromJson(json, ValidateResponse::class.java)

        assertNull(response.session)
    }

    @Test
    fun `LoginResponse parses complete session`() {
        val json = """
        {
            "session": {
                "user": { "id": "user-abc", "email": "test@test.com" },
                "access_token": "token-123",
                "live_stream_token": "stream-456"
            }
        }
        """.trimIndent()
        val response = gson.fromJson(json, LoginResponse::class.java)

        assertEquals("user-abc", response.session?.user?.id)
        assertEquals("token-123", response.session?.accessToken)
        assertEquals("stream-456", response.session?.liveStreamToken)
    }
}
