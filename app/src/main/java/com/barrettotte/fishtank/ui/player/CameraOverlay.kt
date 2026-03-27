package com.barrettotte.fishtank.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp

import com.barrettotte.fishtank.ui.theme.Accent
import com.barrettotte.fishtank.ui.theme.Danger
import com.barrettotte.fishtank.ui.theme.Dark
import com.barrettotte.fishtank.ui.theme.PanelBorder
import com.barrettotte.fishtank.ui.theme.Primary
import com.barrettotte.fishtank.ui.theme.Secondary
import com.barrettotte.fishtank.ui.theme.White

/** Number of times to repeat the camera list for infinite scroll illusion. */
private const val REPEAT_COUNT = 100

/** Right-side camera switcher overlay panel with infinite scroll. */
@Composable
fun CameraOverlay(
    cameras: List<CameraListItem>,
    onCameraSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    if (cameras.isEmpty()) return

    val cameraCount = cameras.size
    // Each "page" has cameras + 1 divider
    val pageSize = cameraCount + 1
    val totalItems = pageSize * REPEAT_COUNT

    // Start in the middle so user can scroll both directions
    val middlePage = REPEAT_COUNT / 2
    val currentIdx = cameras.indexOfFirst { it.isCurrent }.takeIf { it >= 0 }
        ?: cameras.indexOfFirst { it.isOnline }.takeIf { it >= 0 }
        ?: 0
    val startIndex = middlePage * pageSize + currentIdx

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)

    // One focus requester per visible item slot (reused across repeats)
    val focusRequesters = remember(cameraCount) {
        List(cameraCount) { FocusRequester() }
    }

    LaunchedEffect(Unit) {
        focusRequesters[currentIdx].requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
    ) {
        Column(
            modifier = Modifier
                .width(350.dp)
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .background(Dark)
                .clickable(enabled = false) {}
                .padding(16.dp),
        ) {
            Text(
                text = "Switch Camera",
                style = MaterialTheme.typography.titleLarge,
                color = White,
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = PanelBorder)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(state = listState) {
                items(totalItems) { index ->
                    val posInPage = index % pageSize
                    if (posInPage == cameraCount) {
                        // Divider between repeats
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = PanelBorder)
                        Spacer(modifier = Modifier.height(6.dp))
                    } else {
                        val camera = cameras[posInPage]
                        CameraListRow(
                            camera = camera,
                            focusRequester = focusRequesters[posInPage],
                            onClick = {
                                if (camera.isOnline && !camera.isCurrent) {
                                    onCameraSelected(camera.stream.id)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

/** A single row in the camera switcher list. */
@Composable
private fun CameraListRow(
    camera: CameraListItem,
    focusRequester: FocusRequester,
    onClick: () -> Unit,
) {
    val statusColor = if (camera.isOnline) Secondary else Danger
    val cameraName = camera.stream.displayName ?: camera.stream.name ?: camera.stream.id
    var isFocused by remember { mutableStateOf(false) }

    val bgColor = if (isFocused) Primary else Color.Transparent
    val textColor = when {
        isFocused -> White
        camera.isCurrent -> Accent
        !camera.isOnline -> Color.Gray
        else -> White
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.hasFocus || it.isFocused }
            .focusRequester(focusRequester)
            .background(bgColor)
            .clickable(enabled = camera.isOnline, onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(statusColor),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = cameraName,
            color = textColor,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
