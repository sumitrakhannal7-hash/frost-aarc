package com.example.ui.screens.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.ui.components.FrostProgressRing
import com.example.ui.theme.FrostAccent
import com.example.ui.theme.FrostDanger
import com.example.ui.theme.FrostSuccess
import com.example.ui.theme.IceHighlight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerSheet(
    subject: Subject?,
    targetMinutes: Int,
    elapsedSeconds: Int,
    isRunning: Boolean,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onCompleteSession: () -> Unit,
    onChangeDuration: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val totalSeconds = targetMinutes * 60
    val remainingSeconds = (totalSeconds - elapsedSeconds).coerceAtLeast(0)
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)
    val progressPct = if (totalSeconds > 0) ((elapsedSeconds.toFloat() / totalSeconds.toFloat()) * 100).toInt().coerceIn(0, 100) else 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("focus_timer_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Subject Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = subject?.name ?: "Deep Focus",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "TARGET: $targetMinutes MINUTES · EARN 1 XP / MIN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FrostAccent,
                    letterSpacing = 1.sp
                )
            }

            // Big Timer Ring
            FrostProgressRing(
                progressPercent = progressPct,
                size = 200.dp,
                strokeWidth = 16.dp,
                accentColors = listOf(FrostAccent, IceHighlight)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedTime,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isRunning) "IN FOCUS" else if (elapsedSeconds > 0) "PAUSED" else "READY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRunning) FrostSuccess else FrostAccent,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Duration Presets (Adjustable by user)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Adjust Duration:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(15, 25, 30, 45, 60).forEach { mins ->
                        val selected = targetMinutes == mins
                        FilterChip(
                            selected = selected,
                            onClick = { onChangeDuration(mins) },
                            label = { Text("${mins}m", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FrostAccent,
                                selectedLabelColor = Color(0xFF070B14)
                            )
                        )
                    }
                }
            }

            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                IconButton(
                    onClick = onResetTimer,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", tint = MaterialTheme.colorScheme.onSurface)
                }

                // Main Play/Pause Button
                Button(
                    onClick = { if (isRunning) onPauseTimer() else onStartTimer() },
                    modifier = Modifier
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) MaterialTheme.colorScheme.surfaceVariant else FrostAccent,
                        contentColor = if (isRunning) MaterialTheme.colorScheme.onSurface else Color(0xFF070B14)
                    )
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "PAUSE" else "START FOCUS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Finish / Complete Button
                IconButton(
                    onClick = onCompleteSession,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(FrostSuccess.copy(alpha = 0.2f))
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Complete", tint = FrostSuccess)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
