package com.barrettotte.fishtank.ui.player

import android.view.KeyEvent as AndroidKeyEvent
import android.view.SurfaceView

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

import com.barrettotte.fishtank.MainActivity
import com.barrettotte.fishtank.util.Logger
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

    // Create ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // Update media source when stream URL changes
    LaunchedEffect(uiState.streamUrl) {
        if (uiState.streamUrl.isNotEmpty()) {
            val mediaItem = MediaItem.fromUri(uiState.streamUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
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
        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            // Bare SurfaceView - no focus issues unlike PlayerView
            AndroidView(
                factory = { ctx ->
                    SurfaceView(ctx).apply {
                        isFocusable = false
                        isFocusableInTouchMode = false
                    }
                },
                update = { surfaceView ->
                    exoPlayer.setVideoSurfaceView(surfaceView)
                },
                modifier = Modifier.fillMaxSize(),
            )
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
            if (!uiState.showSettingsDialog) {
                viewModel.toggleCameraSwitcher()
                true
            } else {
                false
            }
        }
        AndroidKeyEvent.KEYCODE_DPAD_DOWN -> {
            if (!uiState.showCameraSwitcher) {
                viewModel.toggleSettingsDialog()
                true
            } else {
                false
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
