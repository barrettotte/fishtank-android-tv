package com.barrettotte.fishtank

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

import com.barrettotte.fishtank.ui.player.PlayerUiState
import com.barrettotte.fishtank.ui.player.PlayerViewModel
import com.barrettotte.fishtank.util.Constants
import com.barrettotte.fishtank.util.StreamUrls

/** Tests for player state and configuration. */
class PlayerViewModelTest {

    @Test
    fun `default UI state has correct defaults`() {
        val state = PlayerUiState()

        assertEquals(Constants.DEFAULT_QUALITY, state.quality)
        assertEquals("auto", state.server)
        assertFalse(state.showInfoOverlay)
        assertFalse(state.showCameraSwitcher)
        assertFalse(state.showSettingsDialog)
        assertTrue(state.isLoading)
    }

    @Test
    fun `quality options have correct values`() {
        val options = PlayerViewModel.QUALITY_OPTIONS
        assertEquals(3, options.size)
        assertEquals("maxbps", options[0].value)
        assertEquals("High", options[0].label)
        assertEquals("2mbps", options[1].value)
        assertEquals("Medium", options[1].label)
        assertEquals("minbps", options[2].value)
        assertEquals("Low", options[2].label)
    }

    @Test
    fun `server options include auto and all servers`() {
        val options = PlayerViewModel.SERVER_OPTIONS
        assertEquals("auto", options[0].value)
        assertEquals("Auto", options[0].label)
        assertTrue(options.size >= 10)
        assertTrue(options.any { it.value == "streams-b.fishtank.live" })
        assertTrue(options.any { it.value == "streams-k.fishtank.live" })
    }

    @Test
    fun `stream URL changes with quality`() {
        val urlHigh = StreamUrls.buildStreamUrl("streams-f.fishtank.live", "cam-1", "token", "maxbps")
        val urlLow = StreamUrls.buildStreamUrl("streams-f.fishtank.live", "cam-1", "token", "minbps")

        assertTrue(urlHigh.contains("video=maxbps"))
        assertTrue(urlLow.contains("video=minbps"))
        assertTrue(urlHigh != urlLow)
    }

    @Test
    fun `stream URL changes with server`() {
        val urlF = StreamUrls.buildStreamUrl("streams-f.fishtank.live", "cam-1", "token", "maxbps")
        val urlB = StreamUrls.buildStreamUrl("streams-b.fishtank.live", "cam-1", "token", "maxbps")

        assertTrue(urlF.contains("streams-f.fishtank.live"))
        assertTrue(urlB.contains("streams-b.fishtank.live"))
        assertTrue(urlF != urlB)
    }
}
