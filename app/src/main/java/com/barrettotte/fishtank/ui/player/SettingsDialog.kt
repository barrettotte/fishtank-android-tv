package com.barrettotte.fishtank.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import com.barrettotte.fishtank.ui.theme.Dark
import com.barrettotte.fishtank.ui.theme.Gray
import com.barrettotte.fishtank.ui.theme.PanelBorder
import com.barrettotte.fishtank.ui.theme.Primary
import com.barrettotte.fishtank.ui.theme.Secondary
import com.barrettotte.fishtank.ui.theme.White

/** Settings dialog for quality and server selection. */
@Composable
fun SettingsDialog(
    currentQuality: String,
    currentServer: String,
    onQualitySelected: (String) -> Unit,
    onServerSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .width(340.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Dark)
                .border(1.dp, PanelBorder, RoundedCornerShape(8.dp))
                .clickable(enabled = false) {} // Prevent click-through
                .padding(16.dp),
        ) {
            // Quality section
            Text(
                text = "Quality",
                style = MaterialTheme.typography.labelSmall,
                color = Gray,
            )
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = PanelBorder)
            Spacer(modifier = Modifier.height(4.dp))

            PlayerViewModel.QUALITY_OPTIONS.forEach { option ->
                SettingsRow(
                    label = option.label,
                    isSelected = option.value == currentQuality,
                    onClick = { onQualitySelected(option.value) },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Server section
            Text(
                text = "Server",
                style = MaterialTheme.typography.labelSmall,
                color = Gray,
            )
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = PanelBorder)
            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(modifier = Modifier.height(300.dp)) {
                items(PlayerViewModel.SERVER_OPTIONS) { option ->
                    SettingsRow(
                        label = option.label,
                        isSelected = option.value == currentServer,
                        onClick = { onServerSelected(option.value) },
                    )
                }
            }
        }
    }
}

/** A single selectable row in the settings dialog. */
@Composable
private fun SettingsRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (isSelected) Secondary.copy(alpha = 0.2f) else Color.Transparent
    val textColor = if (isSelected) Secondary else White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
