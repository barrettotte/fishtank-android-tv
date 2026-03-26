package com.barrettotte.fishtank.ui.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.barrettotte.fishtank.util.Constants
import com.barrettotte.fishtank.ui.theme.Dark
import com.barrettotte.fishtank.ui.theme.Gray
import com.barrettotte.fishtank.ui.theme.Primary
import com.barrettotte.fishtank.ui.theme.Secondary

/** Camera grid screen with header, camera tiles, and D-pad navigation. */
@Composable
fun GridScreen(
    viewModel: GridViewModel,
    onCameraSelected: (String) -> Unit,
    onLogout: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle token expiry by redirecting to login
    LaunchedEffect(uiState.error) {
        if (viewModel.isTokenExpired()) {
            viewModel.logout()
            onLogout()
        }
    }

    // Handle menu/options key for logout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Dark)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.key == Key.Menu) {
                    onLogout()
                    true
                } else {
                    false
                }
            }
            .focusable(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            GridHeader(
                displayName = uiState.displayName,
                onlineCount = uiState.onlineCount,
                totalCount = uiState.totalCount,
                currentTime = uiState.currentTime,
            )

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = uiState.error ?: "Error",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                else -> {
                    CameraGrid(
                        cameras = uiState.cameras,
                        onCameraSelected = onCameraSelected,
                    )
                }
            }
        }
    }
}

/** Header bar with display name, online count, and clock. */
@Composable
private fun GridHeader(
    displayName: String,
    onlineCount: Int,
    totalCount: Int,
    currentTime: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left: logo text
        Text(
            text = "Fishtank",
            style = MaterialTheme.typography.headlineMedium,
            color = Primary,
        )

        // Right: display name, online count, clock
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = displayName,
                color = Color.White,
                fontSize = 14.sp,
            )
            HeaderSeparator()
            Text(
                text = "$onlineCount/$totalCount online",
                color = Secondary,
                fontSize = 14.sp,
            )
            HeaderSeparator()
            Text(
                text = currentTime,
                color = Gray,
                fontSize = 14.sp,
            )
        }
    }
}

/** Dot separator between header items. */
@Composable
private fun HeaderSeparator() {
    Text(
        text = "  \u00B7  ",
        color = Gray,
        fontSize = 14.sp,
    )
}

/** Grid of camera tiles with 5 columns. */
@Composable
private fun CameraGrid(
    cameras: List<CameraTile>,
    onCameraSelected: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(Constants.GRID_COLUMNS),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(cameras, key = { it.stream.id }) { tile ->
            CameraGridItem(
                tile = tile,
                modifier = Modifier
                    .clickable(enabled = tile.isOnline) {
                        onCameraSelected(tile.stream.id)
                    },
            )
        }
    }
}
