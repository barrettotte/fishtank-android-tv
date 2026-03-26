package com.barrettotte.fishtank.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import com.barrettotte.fishtank.data.model.LiveStream
import com.barrettotte.fishtank.data.repository.AuthRepository
import com.barrettotte.fishtank.data.repository.PreferencesRepository
import com.barrettotte.fishtank.data.repository.StreamData
import com.barrettotte.fishtank.data.repository.StreamRepository
import com.barrettotte.fishtank.util.Constants
import com.barrettotte.fishtank.util.Logger
import com.barrettotte.fishtank.util.StreamUrls

/** Quality option with display label and API value. */
data class QualityOption(val label: String, val value: String)

/** Server option with display label and hostname value. */
data class ServerOption(val label: String, val value: String)

/** UI state for the video player screen. */
data class PlayerUiState(
    val streamId: String = "",
    val streamUrl: String = "",
    val cameraName: String = "",
    val quality: String = Constants.DEFAULT_QUALITY,
    val server: String = "auto",
    val showInfoOverlay: Boolean = false,
    val showCameraSwitcher: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val cameras: List<CameraListItem> = emptyList(),
    val isLoading: Boolean = true,
)

/** Camera item in the switcher list. */
data class CameraListItem(
    val stream: LiveStream,
    val isOnline: Boolean,
    val isCurrent: Boolean,
    val server: String,
)

/** ViewModel for the video player screen. */
class PlayerViewModel(
    private val streamRepository: StreamRepository,
    private val preferencesRepository: PreferencesRepository,
    private val authRepository: AuthRepository,
    initialStreamId: String,
) : ViewModel() {
    companion object {
        private const val TAG = "Player"
        private const val INFO_OVERLAY_DURATION_MS = 3000L

        val QUALITY_OPTIONS = listOf(
            QualityOption("High", "maxbps"),
            QualityOption("Medium", "2mbps"),
            QualityOption("Low", "minbps"),
        )

        val SERVER_OPTIONS = listOf(
            ServerOption("Auto", "auto"),
            ServerOption("US-East (b)", "streams-b.fishtank.live"),
            ServerOption("US-West (c)", "streams-c.fishtank.live"),
            ServerOption("US-West (e)", "streams-e.fishtank.live"),
            ServerOption("US-West (f)", "streams-f.fishtank.live"),
            ServerOption("US-West (g)", "streams-g.fishtank.live"),
            ServerOption("US-West (h)", "streams-h.fishtank.live"),
            ServerOption("US-West (i)", "streams-i.fishtank.live"),
            ServerOption("US-West (j)", "streams-j.fishtank.live"),
            ServerOption("US-West (k)", "streams-k.fishtank.live"),
        )
    }

    private val _uiState = MutableStateFlow(PlayerUiState(streamId = initialStreamId))
    val uiState: StateFlow<PlayerUiState> = _uiState

    private var streamData: StreamData? = null
    private var infoOverlayJob: Job? = null

    init {
        loadPreferencesAndStream(initialStreamId)
    }

    /** Load saved preferences, refresh token, fetch stream data, and build the URL. */
    private fun loadPreferencesAndStream(streamId: String) {
        viewModelScope.launch {
            val quality = preferencesRepository.getQuality()
            val server = preferencesRepository.getServer()
            _uiState.value = _uiState.value.copy(quality = quality, server = server)

            Logger.d(TAG, "Loading stream $streamId (quality=$quality, server=$server)")

            // Refresh the live stream token before playing (it expires every 30 minutes)
            authRepository.refreshLiveStreamToken()

            val result = streamRepository.fetchStreams()
            if (result.isSuccess) {
                streamData = result.getOrThrow()
                buildStreamUrl(streamId)
                buildCameraList(streamId)
            } else {
                Logger.e(TAG, "Failed to fetch streams: ${result.exceptionOrNull()?.message}")
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    /** Build the HLS stream URL for the given stream ID. */
    private fun buildStreamUrl(streamId: String) {
        val data = streamData ?: return
        val state = _uiState.value
        val liveStreamToken = runCatching {
            kotlinx.coroutines.runBlocking { preferencesRepository.getLiveStreamToken() }
        }.getOrDefault("")

        val loadBalancerServer = data.loadBalancerMap[streamId] ?: Constants.DEFAULT_SERVER
        val server = if (state.server == "auto") loadBalancerServer else state.server

        val stream = data.streams.find { it.id == streamId }
        val cameraName = stream?.displayName ?: stream?.name ?: streamId

        val url = StreamUrls.buildStreamUrl(server, streamId, liveStreamToken, state.quality)
        Logger.d(TAG, "Stream URL built for '$cameraName': $url")

        _uiState.value = _uiState.value.copy(
            streamId = streamId,
            streamUrl = url,
            cameraName = cameraName,
        )
    }

    /** Build the camera list for the switcher overlay. */
    private fun buildCameraList(currentStreamId: String) {
        val data = streamData ?: return
        val cameras = data.streams.map { stream ->
            CameraListItem(
                stream = stream,
                isOnline = data.statusMap[stream.id] == "online",
                isCurrent = stream.id == currentStreamId,
                server = data.loadBalancerMap[stream.id] ?: Constants.DEFAULT_SERVER,
            )
        }
        _uiState.value = _uiState.value.copy(cameras = cameras)
    }

    /** Switch to a different camera stream. */
    fun switchCamera(streamId: String) {
        Logger.d(TAG, "Switching to camera: $streamId")
        buildStreamUrl(streamId)
        buildCameraList(streamId)
        _uiState.value = _uiState.value.copy(showCameraSwitcher = false)
    }

    /** Update quality preference and rebuild stream URL. */
    fun setQuality(quality: String) {
        Logger.d(TAG, "Quality changed to: $quality")
        _uiState.value = _uiState.value.copy(quality = quality)
        viewModelScope.launch { preferencesRepository.saveQuality(quality) }
        buildStreamUrl(_uiState.value.streamId)
    }

    /** Update server preference and rebuild stream URL. */
    fun setServer(server: String) {
        Logger.d(TAG, "Server changed to: $server")
        _uiState.value = _uiState.value.copy(server = server)
        viewModelScope.launch { preferencesRepository.saveServer(server) }
        buildStreamUrl(_uiState.value.streamId)
    }

    /** Show the info overlay and auto-hide after 3 seconds. */
    fun showInfoOverlay() {
        infoOverlayJob?.cancel()
        _uiState.value = _uiState.value.copy(showInfoOverlay = true)
        infoOverlayJob = viewModelScope.launch {
            delay(INFO_OVERLAY_DURATION_MS)
            _uiState.value = _uiState.value.copy(showInfoOverlay = false)
        }
    }

    /** Toggle the camera switcher overlay. */
    fun toggleCameraSwitcher() {
        val show = !_uiState.value.showCameraSwitcher
        _uiState.value = _uiState.value.copy(
            showCameraSwitcher = show,
            showSettingsDialog = false,
        )
    }

    /** Toggle the settings dialog. */
    fun toggleSettingsDialog() {
        val show = !_uiState.value.showSettingsDialog
        _uiState.value = _uiState.value.copy(
            showSettingsDialog = show,
            showCameraSwitcher = false,
        )
    }

    /** Close all overlays. */
    fun closeOverlays() {
        _uiState.value = _uiState.value.copy(
            showCameraSwitcher = false,
            showSettingsDialog = false,
            showInfoOverlay = false,
        )
    }

    /** Get the display label for the current quality setting. */
    fun getQualityLabel(): String {
        return QUALITY_OPTIONS.find { it.value == _uiState.value.quality }?.label ?: "High"
    }

    /** Get the display label for the current server setting. */
    fun getServerLabel(): String {
        return SERVER_OPTIONS.find { it.value == _uiState.value.server }?.label ?: "Auto"
    }
}
