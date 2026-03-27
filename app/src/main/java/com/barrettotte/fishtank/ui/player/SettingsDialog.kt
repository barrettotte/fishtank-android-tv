package com.barrettotte.fishtank.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    val firstFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        firstFocusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Dark)
                .border(1.dp, PanelBorder, RoundedCornerShape(4.dp))
                .clickable(enabled = false) {}
                .padding(top = 12.dp, bottom = 4.dp),
        ) {
            // Header
            Text(
                text = "Stream Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = PanelBorder)
            SectionLabel("Quality")

            LazyColumn(modifier = Modifier.height(450.dp)) {
                items(PlayerViewModel.QUALITY_OPTIONS.size) { index ->
                    val option = PlayerViewModel.QUALITY_OPTIONS[index]
                    SettingsRow(
                        label = option.label,
                        isSelected = option.value == currentQuality,
                        focusRequester = if (index == 0) firstFocusRequester else null,
                        onClick = { onQualitySelected(option.value) },
                    )
                }

                // Server section
                item {
                    SectionLabel("Server")
                }
                items(PlayerViewModel.SERVER_OPTIONS.size) { index ->
                    val option = PlayerViewModel.SERVER_OPTIONS[index]
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

/** Gray section label (e.g. "Quality", "Server"). */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = Gray,
        modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 4.dp),
    )
    HorizontalDivider(color = PanelBorder)
}

/** A single selectable row in the settings dialog. */
@Composable
private fun SettingsRow(
    label: String,
    isSelected: Boolean,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }

    val focusModifier = if (focusRequester != null) {
        Modifier.focusRequester(focusRequester)
    } else {
        Modifier
    }
    val bgColor = when {
        isFocused -> Primary
        isSelected -> Secondary
        else -> Color.Transparent
    }
    val textColor = when {
        isFocused || isSelected -> Dark
        else -> White
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.hasFocus || it.isFocused }
            .then(focusModifier)
            .background(bgColor)
            .border(0.5.dp, PanelBorder)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = if (isFocused || isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
