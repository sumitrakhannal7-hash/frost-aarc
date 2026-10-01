package com.example.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.RecoveryTask
import com.example.data.model.RoutineItem
import com.example.data.model.Subject
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutExercise
import com.example.engine.XpEngine
import com.example.ui.FrostTab
import com.example.ui.components.FrostProgressRing
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    profile: UserProfile?,
    subjects: List<Subject>,
    routineItems: List<RoutineItem>,
    workoutExercises: List<WorkoutExercise>,
    recoveryTasks: List<RecoveryTask>,
    onNavigateTab: (FrostTab) -> Unit,
    onToggleRoutine: (RoutineItem) -> Unit,
    onCompleteAllTasks: () -> Unit,
    onToggleRecovery: (RecoveryTask) -> Unit,
    onOpenAiGuide: () -> Unit,
    onOpenArcGrid: () -> Unit,
    onQuickStartFocus: (Subject) -> Unit
) {
    val levelInfo = XpEngine.getLevelInfo(profile?.totalXp ?: 0)
    val todayPercent = profile?.todayCompletionPercent ?: 0
    val streak = profile?.streak ?: 0
    val shields = profile?.shields ?: 3
    val arcDay = profile?.arcCurrentDay ?: 1
    val displayName = profile?.name?.ifBlank { "Subodh" } ?: "Subodh"
    val displayGrade = profile?.grade ?: "Class 12"
    val displayStream = profile?.stream ?: "Science"

    // Category sums
    val totalStudyTarget = subjects.sumOf { it.dailyTargetMinutes }
    val totalStudyDone = subjects.sumOf { it.completedMinutesToday }
    val studyPct = if (totalStudyTarget > 0) ((totalStudyDone.toFloat() / totalStudyTarget.toFloat()) * 100).toInt().coerceIn(0, 100) else 100

    val routineDone = routineItems.count { it.isCompleted }
    val routinePct = if (routineItems.isNotEmpty()) ((routineDone.toFloat() / routineItems.size.toFloat()) * 100).toInt() else 100

    val isRestDay = workoutExercises.any { it.isRestDay }
    val workoutDoneSets = workoutExercises.sumOf { it.completedSets }
    val workoutTotalSets = workoutExercises.sumOf { it.targetSets }
    val workoutPct = if (isRestDay) 100 else if (workoutTotalSets > 0) ((workoutDoneSets.toFloat() / workoutTotalSets.toFloat()) * 100).toInt().coerceIn(0, 100) else 100

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. User Header & Developer Credit
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Welcome, $displayName",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FrostAccent.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, FrostAccent.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Developer",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = FrostAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Day $arcDay of 60 Arc · $displayStream ($displayGrade) · Developer: Subodh",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FrostAccent
                    )
                }

                // Level Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(FrostSuccess)
                        )
                        Text(
                            text = "Lvl ${levelInfo.level} · ${levelInfo.title}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 2. Hero Card: Today's Completion Ring & Streak
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Streak & Shield Status Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Streak Flame
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = FrostWarning,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "$streak Days Streak",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = FrostWarning
                            )
                        }

                        // Streak Shields
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(3) { index ->
                                val hasShield = index < shields
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Shield ${index + 1}",
                                    tint = if (hasShield) FrostAccent else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$shields/3",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = FrostAccent
                            )
                        }
                    }

                    // Main Progress Ring
                    FrostProgressRing(
                        progressPercent = todayPercent,
                        size = 170.dp,
                        strokeWidth = 16.dp,
                        accentColors = listOf(FrostAccent, DeepGlacier, IceHighlight)
                    )

                    // Arc Day Button
                    OutlinedButton(
                        onClick = onOpenArcGrid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_arc_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Arc Grid",
                            modifier = Modifier.size(16.dp),
                            tint = FrostAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View 60-Day Arc Grid & Milestones",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FrostAccent
                        )
                    }
                }
            }
        }

        // 3. Mini Rings: Study, Routine, Workout
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Study Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab(FrostTab.FOCUS) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "STUDY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FrostAccent)
                        Text(text = "$studyPct%", fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text(text = "${totalStudyDone}m / ${totalStudyTarget}m", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Routine Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab(FrostTab.ROUTINE) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "ROUTINE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FrostSuccess)
                        Text(text = "$routinePct%", fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text(text = "$routineDone/${routineItems.size} tasks", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Workout Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab(FrostTab.WORKOUT) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "WORKOUT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AuroraIndigo)
                        Text(text = if (isRestDay) "REST" else "$workoutPct%", fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text(text = if (isRestDay) "Protected" else "$workoutDoneSets/$workoutTotalSets sets", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 3.5 Prominent Workout & Video Form Quick Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTab(FrostTab.WORKOUT) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(0.8.dp, AuroraIndigo.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AuroraIndigo.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = "Workout",
                                tint = AuroraIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Daily Workout & Form Guides",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AuroraIndigo.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "Music ON/OFF",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AuroraIndigo,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Stream internet video guides · Set & rest timers · Gym beats",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { onNavigateTab(FrostTab.WORKOUT) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AuroraIndigo, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Recovery Alert (if any pending)
        if (recoveryTasks.any { !it.isCompleted }) {
            val pendingTask = recoveryTasks.first { !it.isCompleted }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FrostDanger.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, FrostDanger.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = "Recovery", tint = FrostDanger)
                            Column {
                                Text(text = "Recovery Task Required", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FrostDanger)
                                Text(text = pendingTask.title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        IconButton(
                            onClick = { onToggleRecovery(pendingTask) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Complete Recovery", tint = FrostSuccess)
                        }
                    }
                }
            }
        }

        // 5. Quick Action Recommended Focus
        val recommendedSubject = subjects.firstOrNull { it.completedMinutesToday < it.dailyTargetMinutes } ?: subjects.firstOrNull()
        if (recommendedSubject != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "RECOMMENDED STUDY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = FrostAccent,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = recommendedSubject.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${recommendedSubject.completedMinutesToday}/${recommendedSubject.dailyTargetMinutes} min done today",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { onQuickStartFocus(recommendedSubject) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FrostAccent,
                                contentColor = Color(0xFF070B14)
                            )
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start Focus", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Start Focus", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 6. Today's Routine To-Do List (All Items Visible + Complete All 100% Button)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Today's Routine To-Do",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "$routineDone of ${routineItems.size} tasks completed",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onCompleteAllTasks,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FrostSuccess,
                            contentColor = Color(0xFF070B14)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Complete All (100%)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    TextButton(onClick = { onNavigateTab(FrostTab.ROUTINE) }) {
                        Text(text = "Manage", fontSize = 12.sp, color = FrostAccent)
                    }
                }
            }
        }

        // All routine items shown so none are truncated
        items(routineItems, key = { it.id }) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = if (item.isCompleted) BorderStroke(1.dp, FrostSuccess.copy(alpha = 0.5f)) else CardDefaults.outlinedCardBorder()
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Checkbox(
                            checked = item.isCompleted,
                            onCheckedChange = { onToggleRoutine(item) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = FrostSuccess,
                                checkmarkColor = Color(0xFF070B14)
                            )
                        )
                        Column {
                            Text(
                                text = item.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = item.startTime,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "· ${item.durationMinutes} min",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "· ${item.category}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FrostAccent
                                )
                            }
                        }
                    }

                    if (item.isCompleted) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Done", tint = FrostSuccess, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // 7. Gemini AI Assistant Callout
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenAiGuide() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Gemini AI",
                        tint = FrostAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gemini AI Tutor Ready",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ask any question by your choice—physics, math, chemistry, coding, or daily planning.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 8. Prominent Developer Signature Card on Home Screen as requested
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("developer_signature_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, FrostAccent.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified Developer",
                        tint = FrostAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Developer: Subodh",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = FrostAccent,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
