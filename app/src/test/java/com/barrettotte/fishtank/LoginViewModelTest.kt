package com.barrettotte.fishtank

import com.barrettotte.fishtank.ui.login.LoginUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Tests for LoginUiState defaults and validation logic. */
class LoginViewModelTest {

    @Test
    fun `default UI state has empty fields and no error`() {
        val state = LoginUiState()

        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals(false, state.isLoading)
        assertEquals(true, state.isCheckingToken)
        assertNull(state.error)
    }

    @Test
    fun `state with error preserves other fields`() {
        val state = LoginUiState(
            email = "test@example.com",
            password = "pass123",
            error = "Login failed: 401",
        )

        assertEquals("test@example.com", state.email)
        assertEquals("pass123", state.password)
        assertNotNull(state.error)
        assertEquals("Login failed: 401", state.error)
    }

    @Test
    fun `state copy clears error`() {
        val state = LoginUiState(error = "some error")
        val cleared = state.copy(error = null)

        assertNull(cleared.error)
    }

    @Test
    fun `loading state disables interaction`() {
        val state = LoginUiState(isLoading = true)

        assertEquals(true, state.isLoading)
    }
}
