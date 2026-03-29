package com.barrettotte.fishtank.ui.grid

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import com.barrettotte.fishtank.data.model.LiveStream
import com.barrettotte.fishtank.data.repository.AuthRepository
import com.barrettotte.fishtank.data.repository.PreferencesRepository
import com.barrettotte.fishtank.data.repository.StreamRepository
import com.barrettotte.fishtank.data.repository.TokenExpiredException
import com.barrettotte.fishtank.util.Constants
import com.barrettotte.fishtank.util.Logger
import com.barrettotte.fishtank.util.StreamUrls

/** A camera tile with all data needed for display. */
data class CameraTile(
    val stream: LiveStream,
    val isOnline: Boolean,
    val server: String,
    val thumbnailUrl: String,
)

/** UI state for the camera grid screen. */
data class GridUiState(
    val cameras: List<CameraTile> = emptyList(),
    val displayName: String = "",
    val onlineCount: Int = 0,
    val totalCount: Int = 0,
    val currentTime: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val warningMessage: String? = null,
)

/** ViewModel for the camera grid screen. */
class GridViewModel(
    private val streamRepository: StreamRepository,
    private val authRepository: AuthRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "Grid"
    }

    private val _uiState = MutableStateFlow(GridUiState())
    val uiState: StateFlow<GridUiState> = _uiState

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.US)

    init {
        loadDisplayName()
        fetchStreams()
        startClockTimer()
        startThumbnailRefresh()
    }

    /** Load display name from preferences asynchronously. */
    private fun loadDisplayName() {
        viewModelScope.launch {
            val name = preferencesRepository.getDisplayName()
            _uiState.value = _uiState.value.copy(displayName = name)
        }
    }

    /** Fetch camera streams from the API. */
    fun fetchStreams() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = streamRepository.fetchStreams()
            if (result.isSuccess) {
                val data = result.getOrThrow()
                val cameras = data.streams.map { stream ->
                    val isOnline = data.statusMap[stream.id] == "online"
                    val server = data.loadBalancerMap[stream.id] ?: Constants.DEFAULT_SERVER

                    val thumbnailUrl = if (isOnline) {
                        StreamUrls.buildThumbnailUrl(server, stream.id)
                    } else {
                        ""
                    }
                    CameraTile(stream, isOnline, server, thumbnailUrl)
                }
                val onlineCount = cameras.count { it.isOnline }

                Logger.d(TAG, "Grid loaded: ${cameras.size} cameras, $onlineCount online")

                _uiState.value = _uiState.value.copy(
                    cameras = cameras,
                    onlineCount = onlineCount,
                    totalCount = cameras.size,
                    isLoading = false,
                )
            } else {
                val exception = result.exceptionOrNull()
                Logger.e(TAG, "Failed to load streams: ${exception?.message}")

                val message = when (exception) {
                    is TokenExpiredException -> "Session expired. Please log in again."
                    is java.net.UnknownHostException -> "No internet connection"
                    is java.net.SocketTimeoutException -> "Connection timed out"
                    else -> "Failed to load cameras. Please try again."
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = message,
                )
            }
        }
    }

    /** Manually refresh auth tokens. */
    suspend fun refreshToken() {
        Logger.d(TAG, "Manual token refresh requested")
        authRepository.refreshLiveStreamToken()
        Logger.d(TAG, "Token refreshed")
    }

    /** Show a warning when an offline camera is selected. Auto-dismisses after 3 seconds. */
    fun showOfflineWarning(cameraName: String) {
        _uiState.value = _uiState.value.copy(warningMessage = "$cameraName is offline")
        viewModelScope.launch {
            delay(3000)
            _uiState.value = _uiState.value.copy(warningMessage = null)
        }
    }

    /** Check if the stream fetch failed due to token expiry. */
    fun isTokenExpired(): Boolean {
        val result = _uiState.value.error
        return result?.contains("Token expired") == true
    }

    /** Logout and clear session. */
    suspend fun logout() {
        authRepository.logout()
    }

    /** Update the clock display every second. */
    private fun startClockTimer() {
        viewModelScope.launch {
            while (true) {
                _uiState.value = _uiState.value.copy(currentTime = dateFormat.format(Date()))
                delay(Constants.CLOCK_REFRESH_MS)
            }
        }
    }

    /** Refresh thumbnail URLs every 90 seconds. */
    private fun startThumbnailRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(Constants.THUMBNAIL_REFRESH_MS)
                Logger.d(TAG, "Refreshing thumbnails")

                val updated = _uiState.value.cameras.map { tile ->
                    if (tile.isOnline) {
                        tile.copy(thumbnailUrl = StreamUrls.buildThumbnailUrl(tile.server, tile.stream.id))
                    } else {
                        tile
                    }
                }
                _uiState.value = _uiState.value.copy(cameras = updated)
            }
        }
    }
}
