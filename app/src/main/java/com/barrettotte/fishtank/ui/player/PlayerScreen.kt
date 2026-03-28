package com.barrettotte.fishtank.ui.player

import android.view.KeyEvent as AndroidKeyEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer

import com.barrettotte.fishtank.MainActivity
import com.barrettotte.fishtank.util.Logger
import com.barrettotte.fishtank.ui.theme.Danger
import com.barrettotte.fishtank.ui.theme.Dark
import com.barrettotte.fishtank.ui.theme.Gray
import com.barrettotte.fishtank.ui.theme.Primary
import com.barrettotte.fishtank.ui.theme.White

/** Full-screen video player with ExoPlayer HLS playback. */
@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? MainActivity
    val uiState by viewModel.uiState.collectAsState()
    var isBuffering by remember { mutableStateOf(false) }
    var surfaceReady by remember { mutableStateOf(false) }

    // Create ExoPlayer with live-stream-friendly buffer settings
    val exoPlayer = remember {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                15_000, // minBufferMs
                30_000, // maxBufferMs
                1_500,  // bufferForPlaybackMs (faster start)
                3_000,  // bufferForPlaybackAfterRebufferMs
            )
            .build()

        ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .build()
            .apply {
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        isBuffering = state == Player.STATE_BUFFERING
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        val message = when (error.errorCode) {
                            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                                "Network error. Check your connection."
                            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                                "Stream unavailable. Try a different server."
                            PlaybackException.ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED,
                            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ->
                                "Stream not found."
                            else -> "Playback error. Try again."
                        }
                        viewModel.setPlaybackError(message)
                    }
                })
            }
    }

    // Only load media after surface is ready AND we have a URL
    LaunchedEffect(uiState.streamUrl, surfaceReady) {
        if (uiState.streamUrl.isNotEmpty() && surfaceReady) {
            Logger.d("Player", "Surface ready, loading: ${uiState.streamUrl.substringBefore("?")}")
            exoPlayer.stop()
            val mediaItem = MediaItem.fromUri(uiState.streamUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    // Register Activity-level key interceptor for ALL key events
    DisposableEffect(uiState.showCameraSwitcher, uiState.showSettingsDialog) {
        activity?.keyEventInterceptor = { event ->
            if (event.action == AndroidKeyEvent.ACTION_DOWN) {
                handleKeyEvent(event.keyCode, viewModel, uiState, onBack)
            } else {
                false
            }
        }
        onDispose {
            activity?.keyEventInterceptor = null
        }
    }

    // Clean up player on dispose
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            activity?.keyEventInterceptor = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Dark),
    ) {
        // Video surface - SurfaceHolder callback ensures surface is ready before playback
        AndroidView(
            factory = { ctx ->
                SurfaceView(ctx).apply {
                    isFocusable = false
                    isFocusableInTouchMode = false
                    holder.addCallback(object : SurfaceHolder.Callback {
                        override fun surfaceCreated(holder: SurfaceHolder) {
                            Logger.d("Player", "Surface created")
                            surfaceReady = true
                        }
                        override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {}
                        override fun surfaceDestroyed(holder: SurfaceHolder) {
                            surfaceReady = false
                        }
                    })
                    exoPlayer.setVideoSurfaceView(this)
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Loading/buffering indicator on top of video
        if (uiState.isLoading || isBuffering) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Primary)
            }
        }

        // Playback error overlay
        if (uiState.playbackError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Dark.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.playbackError!!,
                        color = Danger,
                        fontSize = 16.sp,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { viewModel.retry() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                contentColor = White,
                            ),
                        ) {
                            Text("Retry")
                        }
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Gray,
                                contentColor = White,
                            ),
                        ) {
                            Text("Back")
                        }
                    }
                }
            }
        }

        // Info overlay (auto-hides after 3s)
        if (uiState.showInfoOverlay) {
            InfoOverlay(
                cameraName = uiState.cameraName,
                qualityLabel = viewModel.getQualityLabel(),
                serverLabel = viewModel.getServerLabel(),
            )
        }

        // Camera switcher overlay
        if (uiState.showCameraSwitcher) {
            CameraOverlay(
                cameras = uiState.cameras,
                onCameraSelected = { viewModel.switchCamera(it) },
                onDismiss = { viewModel.closeOverlays() },
            )
        }

        // Settings dialog
        if (uiState.showSettingsDialog) {
            SettingsDialog(
                currentQuality = uiState.quality,
                currentServer = uiState.server,
                onQualitySelected = { viewModel.setQuality(it) },
                onServerSelected = { viewModel.setServer(it) },
                onDismiss = { viewModel.closeOverlays() },
            )
        }
    }
}

/** Handle key events from the Activity dispatcher. Returns true if consumed. */
private fun handleKeyEvent(
    keyCode: Int,
    viewModel: PlayerViewModel,
    uiState: PlayerUiState,
    onBack: () -> Unit,
): Boolean {
    Logger.d("Player", "Key: keyCode=$keyCode")

    return when (keyCode) {
        AndroidKeyEvent.KEYCODE_BACK -> {
            if (uiState.showCameraSwitcher || uiState.showSettingsDialog) {
                viewModel.closeOverlays()
            } else {
                onBack()
            }
            true
        }
        AndroidKeyEvent.KEYCODE_DPAD_UP -> {
            if (!uiState.showCameraSwitcher && !uiState.showSettingsDialog) {
                viewModel.toggleCameraSwitcher()
                true
            } else {
                false // Let overlays handle their own D-pad navigation
            }
        }
        AndroidKeyEvent.KEYCODE_DPAD_DOWN -> {
            if (!uiState.showCameraSwitcher && !uiState.showSettingsDialog) {
                viewModel.toggleSettingsDialog()
                true
            } else {
                false // Let overlays handle their own D-pad navigation
            }
        }
        AndroidKeyEvent.KEYCODE_DPAD_CENTER, AndroidKeyEvent.KEYCODE_ENTER -> {
            if (!uiState.showCameraSwitcher && !uiState.showSettingsDialog) {
                viewModel.showInfoOverlay()
                true
            } else {
                false
            }
        }
        else -> false
    }
}

/** Bottom info bar showing camera name, quality, and server. */
@Composable
private fun InfoOverlay(
    cameraName: String,
    qualityLabel: String,
    serverLabel: String,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: camera info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = cameraName, color = White, fontSize = 14.sp)
                Text(text = "  \u00B7  ", color = Gray, fontSize = 14.sp)
                Text(text = qualityLabel, color = White, fontSize = 14.sp)
                Text(text = "  \u00B7  ", color = Gray, fontSize = 14.sp)
                Text(text = serverLabel, color = White, fontSize = 14.sp)
            }

            // Right: control hints
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Up: cameras", color = Gray, fontSize = 12.sp)
                Text(text = "  \u00B7  ", color = Gray, fontSize = 12.sp)
                Text(text = "Down: settings", color = Gray, fontSize = 12.sp)
            }
        }
    }
}
