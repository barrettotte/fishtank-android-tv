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
)

/** ViewModel for the camera grid screen. */
class GridViewModel(
    private val streamRepository: StreamRepository,
    private val authRepository: AuthRepository,
    displayName: String,
) : ViewModel() {

    companion object {
        private const val TAG = "Grid"
    }

    private val _uiState = MutableStateFlow(GridUiState(displayName = displayName))
    val uiState: StateFlow<GridUiState> = _uiState

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.US)

    init {
        fetchStreams()
        startClockTimer()
        startThumbnailRefresh()
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
                val error = result.exceptionOrNull()
                Logger.e(TAG, "Failed to load streams: ${error?.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error?.message ?: "Failed to load streams",
                )
            }
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
