package com.barrettotte.fishtank.ui.grid

import android.view.KeyEvent as AndroidKeyEvent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

import coil.compose.AsyncImage

import com.barrettotte.fishtank.ui.theme.Danger
import com.barrettotte.fishtank.ui.theme.Dark
import com.barrettotte.fishtank.ui.theme.Gray
import com.barrettotte.fishtank.ui.theme.PanelBorder
import com.barrettotte.fishtank.ui.theme.Primary
import com.barrettotte.fishtank.ui.theme.Secondary

/** A single camera tile in the grid showing thumbnail, name, and online status. */
@Composable
fun CameraGridItem(
    tile: CameraTile,
    onSelect: () -> Unit,
    focusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier,
) {
    val statusColor = if (tile.isOnline) Secondary else Danger
    val cameraName = tile.stream.displayName ?: tile.stream.name ?: tile.stream.id
    var isFocused by remember { mutableStateOf(false) }

    val borderColor = if (isFocused) Primary else PanelBorder
    val borderWidth = if (isFocused) 3.dp else 1.dp

    val focusModifier = if (focusRequester != null) {
        Modifier.focusRequester(focusRequester)
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Dark)
            .onFocusChanged { isFocused = it.hasFocus || it.isFocused }
            .then(focusModifier)
            .border(borderWidth, borderColor, RoundedCornerShape(4.dp))
            .clickable { onSelect() }
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(PanelBorder),
            contentAlignment = Alignment.Center,
        ) {
            if (tile.isOnline && tile.thumbnailUrl.isNotEmpty()) {
                AsyncImage(
                    model = tile.thumbnailUrl,
                    contentDescription = cameraName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Text(
                    text = "Offline",
                    color = Gray,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        // Name + status dot
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(statusColor),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = cameraName,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
            )
        }
    }
}
