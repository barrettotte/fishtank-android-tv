package com.barrettotte.fishtank.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import com.barrettotte.fishtank.ui.theme.Accent
import com.barrettotte.fishtank.ui.theme.Danger
import com.barrettotte.fishtank.ui.theme.Dark
import com.barrettotte.fishtank.ui.theme.PanelBorder
import com.barrettotte.fishtank.ui.theme.Secondary
import com.barrettotte.fishtank.ui.theme.White

/** Right-side camera switcher overlay panel. */
@Composable
fun CameraOverlay(
    cameras: List<CameraListItem>,
    onCameraSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
    ) {
        // Right-side panel
        Column(
            modifier = Modifier
                .width(350.dp)
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .background(Dark)
                .clickable(enabled = false) {} // Prevent click-through
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

            LazyColumn {
                items(cameras, key = { it.stream.id }) { camera ->
                    CameraListRow(
                        camera = camera,
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

/** A single row in the camera switcher list. */
@Composable
private fun CameraListRow(
    camera: CameraListItem,
    onClick: () -> Unit,
) {
    val statusColor = if (camera.isOnline) Secondary else Danger
    val textColor = when {
        camera.isCurrent -> Accent
        !camera.isOnline -> Color.Gray
        else -> White
    }
    val cameraName = camera.stream.displayName ?: camera.stream.name ?: camera.stream.id

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = camera.isOnline && !camera.isCurrent, onClick = onClick)
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
