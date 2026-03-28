package com.barrettotte.fishtank.ui.grid

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.barrettotte.fishtank.R

import com.barrettotte.fishtank.util.Constants
import com.barrettotte.fishtank.ui.theme.Danger
import com.barrettotte.fishtank.ui.theme.Dark
import com.barrettotte.fishtank.ui.theme.Gray
import com.barrettotte.fishtank.ui.theme.PanelBorder
import com.barrettotte.fishtank.ui.theme.White
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
    var showLogoutDialog by remember { mutableStateOf(false) }

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
                    showLogoutDialog = true
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
                onLogout = { showLogoutDialog = true },
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
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = uiState.error ?: "Error",
                            color = Danger,
                            fontSize = 16.sp,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.fetchStreams() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                contentColor = Color.White,
                            ),
                        ) {
                            Text("Retry")
                        }
                    }
                }
                else -> {
                    CameraGrid(
                        cameras = uiState.cameras,
                        onCameraSelected = { streamId ->
                            val tile = uiState.cameras.find { it.stream.id == streamId }
                            if (tile != null && !tile.isOnline) {
                                val name = tile.stream.displayName ?: tile.stream.name ?: streamId
                                viewModel.showOfflineWarning(name)
                            } else {
                                onCameraSelected(streamId)
                            }
                        },
                    )
                }
            }
        }

        // Warning toast overlay
        if (uiState.warningMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 48.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Text(
                    text = uiState.warningMessage!!,
                    color = White,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .background(Danger.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                )
            }
        }

        // Logout confirmation dialog
        if (showLogoutDialog) {
            val logoutFocusRequester = remember { FocusRequester() }

            LaunchedEffect(Unit) {
                logoutFocusRequester.requestFocus()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { showLogoutDialog = false },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .width(280.dp)
                        .background(Dark, RoundedCornerShape(8.dp))
                        .border(1.dp, PanelBorder, RoundedCornerShape(8.dp))
                        .clickable(enabled = false) {}
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Log Out?",
                        color = White,
                        fontSize = 18.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        DialogButton(
                            text = "Log Out",
                            baseColor = Danger,
                            focusRequester = logoutFocusRequester,
                            onClick = {
                                showLogoutDialog = false
                                onLogout()
                            },
                        )
                        DialogButton(
                            text = "Cancel",
                            baseColor = Gray,
                            onClick = { showLogoutDialog = false },
                        )
                    }
                }
            }
        }
    }
}

/** Header bar with display name, online count, clock, and logout. */
@Composable
private fun GridHeader(
    displayName: String,
    onlineCount: Int,
    totalCount: Int,
    currentTime: String,
    onLogout: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left: logo image
        Image(
            painter = painterResource(id = R.drawable.logo_stripe),
            contentDescription = "Fishtank",
            modifier = Modifier.height(28.dp),
            contentScale = ContentScale.Fit,
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
                color = Color.White,
                fontSize = 14.sp,
            )
            HeaderSeparator()
            Text(
                text = currentTime,
                color = Color.White,
                fontSize = 14.sp,
            )
            HeaderSeparator()
            var logoutFocused by remember { mutableStateOf(false) }
            Text(
                text = "Log Out",
                color = if (logoutFocused) Color.White else Danger,
                fontSize = 14.sp,
                modifier = Modifier
                    .onFocusChanged { logoutFocused = it.hasFocus || it.isFocused }
                    .background(
                        if (logoutFocused) Danger else Color.Transparent,
                        RoundedCornerShape(4.dp),
                    )
                    .clickable { onLogout() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

/** Dot separator between header items. */
@Composable
private fun HeaderSeparator() {
    Text(
        text = "  \u00B7  ",
        color = Color.White,
        fontSize = 14.sp,
    )
}

/** Grid of camera tiles with 5 columns. */
@Composable
private fun CameraGrid(
    cameras: List<CameraTile>,
    onCameraSelected: (String) -> Unit,
) {
    val firstFocusRequester = remember { FocusRequester() }

    // Request focus on the first tile after composition
    LaunchedEffect(cameras) {
        if (cameras.isNotEmpty()) {
            firstFocusRequester.requestFocus()
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(Constants.GRID_COLUMNS),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        itemsIndexed(cameras, key = { _, tile -> tile.stream.id }) { index, tile ->
            CameraGridItem(
                tile = tile,
                onSelect = { onCameraSelected(tile.stream.id) },
                focusRequester = if (index == 0) firstFocusRequester else null,
            )
        }
    }
}

/** A button that shows an orange highlight when focused via D-pad. */
@Composable
private fun DialogButton(
    text: String,
    baseColor: Color,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    val bgColor = if (isFocused) Primary else baseColor
    val focusMod = if (focusRequester != null) {
        Modifier.focusRequester(focusRequester)
    } else {
        Modifier
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = White,
        ),
        modifier = Modifier
            .onFocusChanged { isFocused = it.hasFocus || it.isFocused }
            .then(focusMod),
    ) {
        Text(text)
    }
}
