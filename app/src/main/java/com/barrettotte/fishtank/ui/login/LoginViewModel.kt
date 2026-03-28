package com.barrettotte.fishtank.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import com.barrettotte.fishtank.data.repository.AuthRepository

/** UI state for the login screen. */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isCheckingToken: Boolean = true,
    val error: String? = null,
)

/** ViewModel for login screen. Handles login, token validation, and auto-login. */
class LoginViewModel(
    private val authRepository: AuthRepository,
    private val autoLoginEmail: String = "",
    private val autoLoginPassword: String = "",
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    /** Check for cached token on launch. Always re-logins to get fresh live_stream_token. */
    fun checkCachedToken(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingToken = true)

            // Always do a full login to get a fresh live_stream_token (expires every 30 min).
            // Try .env credentials first, then stored credentials from previous login.
            val email = autoLoginEmail.ifEmpty { authRepository.getStoredEmail() }
            val password = autoLoginPassword.ifEmpty { authRepository.getStoredPassword() }

            if (email.isNotEmpty() && password.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    email = email,
                    password = password,
                    isCheckingToken = false,
                )
                login(onSuccess)
                return@launch
            }

            // No credentials anywhere - try cached token (will have stale live_stream_token)
            val isValid = authRepository.validateToken()
            if (isValid) {
                onSuccess()
                return@launch
            }

            _uiState.value = _uiState.value.copy(isCheckingToken = false)
        }
    }

    /** Update the email field. */
    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    /** Update the password field. */
    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    /** Attempt to log in with the current email and password. */
    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Email and password are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = authRepository.login(state.email, state.password)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } else {
                val exception = result.exceptionOrNull()
                val message = when {
                    exception is java.net.UnknownHostException -> "No internet connection"
                    exception is java.net.SocketTimeoutException -> "Connection timed out"
                    exception?.message?.contains("401") == true -> "Invalid email or password"
                    exception?.message?.contains("403") == true -> "Access denied"
                    else -> "Login failed. Please try again."
                }
                _uiState.value = _uiState.value.copy(isLoading = false, error = message)
            }
        }
    }
}
