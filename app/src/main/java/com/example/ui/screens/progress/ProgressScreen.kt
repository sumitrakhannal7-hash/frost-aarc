package com.example.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.engine.XpEngine
import com.example.ui.components.FrostProgressRing
import com.example.ui.theme.*

@Composable
fun ProgressScreen(
    profile: UserProfile?,
    arcDays: List<ArcDayRecord>,
    subjects: List<Subject>,
    achievements: List<AchievementItem>,
    studySessions: List<StudySessionLog>,
    onOpenArcGridFull: () -> Unit,
    onOpenAchievementsFull: () -> Unit
) {
    val levelInfo = XpEngine.getLevelInfo(profile?.totalXp ?: 0)
    val unlockedCount = achievements.count { it.isUnlocked }
    val totalAchievements = achievements.size

    val completedDaysCount = arcDays.count { it.status == "Completed" }
    val currentDayNum = profile?.arcCurrentDay ?: 1

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("progress_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. 60-Day Arc Progression Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "60-DAY ARC MOMENTUM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FrostAccent,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Day $currentDayNum of 60",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = FrostAccent.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "$completedDaysCount / 60 Won",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = FrostAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Mini Arc Preview (first 14 days or around current)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        arcDays.take(10).forEach { day ->
                            val isCurrent = day.dayNumber == currentDayNum
                            val isCompleted = day.completionPercent >= 80

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when {
                                            isCurrent -> FrostAccent
                                            isCompleted -> FrostSuccess
                                            day.completionPercent in 1..79 -> FrostWarning
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                    .border(
                                        width = if (isCurrent) 2.dp else 0.dp,
                                        color = if (isCurrent) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${day.dayNumber}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent || isCompleted) Color(0xFF070B14) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onOpenArcGridFull,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = FrostAccent
                        )
                    ) {
                        Icon(imageVector = Icons.Default.GridOn, contentDescription = "Arc Grid", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View Complete 60-Day Arc Grid & Status", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. XP & Frost Rank Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "LEVEL & XP PROGRESSION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FrostSuccess,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Level ${levelInfo.level} · ${levelInfo.title}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Text(
                            text = "${profile?.totalXp ?: 0} Total XP",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = FrostAccent
                        )
                    }

                    LinearProgressIndicator(
                        progress = { levelInfo.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = FrostAccent,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${levelInfo.currentLevelXp} XP into level",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${levelInfo.nextLevelTargetXp} XP to Level ${levelInfo.level + 1}",
                            fontSize = 11.sp,
                            color = FrostAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 3. Subject Study Analytics (Instant Breakdown)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "STUDY ANALYTICS BY SUBJECT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FrostAccent,
                        letterSpacing = 1.sp
                    )

                    subjects.forEach { subject ->
                        val pct = if (subject.dailyTargetMinutes > 0) {
                            ((subject.completedMinutesToday.toFloat() / subject.dailyTargetMinutes.toFloat()) * 100).toInt().coerceIn(0, 100)
                        } else 100

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = subject.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "${subject.completedMinutesToday}m / ${subject.dailyTargetMinutes}m ($pct%)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FrostAccent
                                )
                            }
                            LinearProgressIndicator(
                                progress = { pct / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(android.graphics.Color.parseColor(subject.colorHex)),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 4. Achievements Showcase
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "ACHIEVEMENTS GALLERY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FrostWarning,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "$unlockedCount of $totalAchievements Unlocked",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        TextButton(onClick = onOpenAchievementsFull) {
                            Text("View All", color = FrostAccent, fontSize = 12.sp)
                        }
                    }

                    // Display top 4 achievements
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        achievements.take(4).forEach { item ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.width(68.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (item.isUnlocked) FrostWarning.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (item.isUnlocked) FrostWarning else MaterialTheme.colorScheme.outline,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (item.isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                                        contentDescription = item.title,
                                        tint = if (item.isUnlocked) FrostWarning else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = item.title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (item.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
