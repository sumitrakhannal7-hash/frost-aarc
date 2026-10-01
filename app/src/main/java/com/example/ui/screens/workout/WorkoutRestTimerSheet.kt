package com.example.ui.screens.workout

import androidx.compose.foundation.background
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
import com.example.ui.components.FrostProgressRing
import com.example.ui.theme.AuroraIndigo
import com.example.ui.theme.FrostAccent
import com.example.ui.theme.IceHighlight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutRestTimerSheet(
    remainingSeconds: Int,
    targetSeconds: Int,
    isRunning: Boolean,
    onTogglePlayPause: () -> Unit,
    onAdjustTime: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val pct = if (targetSeconds > 0) ((remainingSeconds.toFloat() / targetSeconds.toFloat()) * 100).toInt().coerceIn(0, 100) else 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("workout_rest_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "REST INTERVAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuroraIndigo,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Breathe & Recharge for Next Set",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            FrostProgressRing(
                progressPercent = pct,
                size = 180.dp,
                strokeWidth = 14.dp,
                accentColors = listOf(AuroraIndigo, FrostAccent)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${remainingSeconds}s",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isRunning) "RESTING" else "PAUSED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuroraIndigo,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Quick Adjust Buttons (+15s, -15s)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { onAdjustTime(-15) }, shape = RoundedCornerShape(10.dp)) {
                    Text("-15s", fontWeight = FontWeight.Bold)
                }
                FilledTonalButton(onClick = onTogglePlayPause, shape = RoundedCornerShape(10.dp)) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle Pause"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isRunning) "Pause" else "Resume", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = { onAdjustTime(15) }, shape = RoundedCornerShape(10.dp)) {
                    Text("+15s", fontWeight = FontWeight.Bold)
                }
            }

            // Skip Rest
            TextButton(onClick = onDismiss) {
                Text("Skip Rest & Start Next Set", color = FrostAccent, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
