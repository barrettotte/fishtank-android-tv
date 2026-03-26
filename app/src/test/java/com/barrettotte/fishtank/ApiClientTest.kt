package com.barrettotte.fishtank

import org.junit.Assert.assertEquals
import org.junit.Test

import com.barrettotte.fishtank.util.Constants

/** Tests for app constants. */
class ApiClientTest {

    @Test
    fun `base URL ends with trailing slash`() {
        assertEquals('/', Constants.API_BASE_URL.last())
    }

    @Test
    fun `base URL points to fishtank API v1`() {
        assertEquals("https://api.fishtank.live/v1/", Constants.API_BASE_URL)
    }

    @Test
    fun `thumbnail refresh interval is 90 seconds`() {
        assertEquals(90_000L, Constants.THUMBNAIL_REFRESH_MS)
    }

    @Test
    fun `grid has 5 columns`() {
        assertEquals(5, Constants.GRID_COLUMNS)
    }
}
