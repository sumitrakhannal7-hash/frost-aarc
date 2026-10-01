package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.FrostAccent
import com.example.ui.theme.FrostSuccess
import com.example.ui.theme.FrostWarning
import com.example.ui.theme.IceHighlight

@Composable
fun CongratulationsDialog(
    streakCount: Int,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(2.dp, FrostAccent, RoundedCornerShape(24.dp))
                .testTag("congratulations_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Glowing Trophy Badge
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .scale(scaleAnim)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(FrostAccent.copy(alpha = 0.4f), Color.Transparent)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(FrostAccent, FrostSuccess)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Trophy",
                            tint = Color(0xFF070B14),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Title
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CONGRATULATIONS!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = FrostAccent,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "100% DAILY TARGET CONQUERED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FrostSuccess,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "You executed every single task with absolute precision. Your streak has immediately increased!",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                // Streak Banner
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = FrostWarning,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "$streakCount DAYS",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = FrostWarning
                                )
                                Text(
                                    text = "Current Streak (+1)",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Bonus XP",
                                tint = IceHighlight,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "+150 XP",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = FrostAccent
                                )
                                Text(
                                    text = "Perfect Bonus",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Keep Streak Alive Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("dismiss_congratulations_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FrostAccent,
                        contentColor = Color(0xFF070B14)
                    )
                ) {
                    Text(
                        text = "KEEP THE STREAK ALIVE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
