package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.ui.theme.FrostAccent
import com.example.ui.theme.IceHighlight

@Composable
fun MiniTimerBar(
    subject: Subject?,
    targetMinutes: Int,
    elapsedSeconds: Int,
    isRunning: Boolean,
    onTogglePlayPause: () -> Unit,
    onComplete: () -> Unit,
    onClickBar: () -> Unit
) {
    val totalSeconds = targetMinutes * 60
    val remainingSec = (totalSeconds - elapsedSeconds).coerceAtLeast(0)
    val minutes = remainingSec / 60
    val seconds = remainingSec % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, FrostAccent.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .clickable { onClickBar() },
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Active Timer",
                    tint = FrostAccent,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = subject?.name ?: "Focus Session",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formattedTime,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = FrostAccent
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Resume",
                        tint = IceHighlight
                    )
                }
                FilledIconButton(
                    onClick = onComplete,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = FrostAccent,
                        contentColor = Color(0xFF070B14)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Complete Session",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
