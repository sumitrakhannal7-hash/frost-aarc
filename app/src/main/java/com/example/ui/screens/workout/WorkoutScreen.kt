package com.example.ui.screens.workout

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.WorkoutExercise
import com.example.ui.theme.AuroraIndigo
import com.example.ui.theme.FrostAccent
import com.example.ui.theme.FrostSuccess
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    exercises: List<WorkoutExercise>,
    workoutRestRemainingSec: Int,
    workoutRestTargetSec: Int,
    isWorkoutRestRunning: Boolean,
    onStartRestTimer: (Int) -> Unit,
    onPauseRestTimer: () -> Unit,
    onResumeRestTimer: () -> Unit,
    onResetRestTimer: () -> Unit,
    onAdjustRestTime: (Int) -> Unit,
    onIncrementSet: (WorkoutExercise) -> Unit,
    onToggleDone: (WorkoutExercise) -> Unit,
    onToggleRestDay: (Boolean) -> Unit,
    onDeleteExercise: (WorkoutExercise) -> Unit,
    onAddExercise: (String, Int, String, Int) -> Unit
) {
    val context = LocalContext.current
    val isRestDay = exercises.any { it.isRestDay }
    var showAddExerciseDialog by remember { mutableStateOf(false) }

    // When an exercise is tapped, navigate to its dedicated YouTube video & set tutorial section
    var selectedExerciseId by remember { mutableStateOf<Long?>(null) }
    val currentSelectedExercise = exercises.find { it.id == selectedExerciseId }

    // Workout Music Synthesizer Engine
    val musicManager = remember { WorkoutMusicManager(context) }
    DisposableEffect(Unit) {
        onDispose { musicManager.release() }
    }

    val isMusicOn by musicManager.isMusicOn.collectAsStateWithLifecycle()
    val isMusicPlaying by musicManager.isPlaying.collectAsStateWithLifecycle()
    val currentTrackIdx by musicManager.currentTrackIndex.collectAsStateWithLifecycle()
    val currentTrack = musicManager.tracks[currentTrackIdx]
    val visualizerAmps by musicManager.visualizerAmplitudes.collectAsStateWithLifecycle()

    // Active Exercise Set Timer state
    var setTimerTargetSec by remember { mutableIntStateOf(45) }
    var setTimerRemainingSec by remember { mutableIntStateOf(45) }
    var isSetTimerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isSetTimerRunning, setTimerRemainingSec) {
        if (isSetTimerRunning && setTimerRemainingSec > 0) {
            delay(1000L)
            setTimerRemainingSec--
            if (setTimerRemainingSec == 0) {
                isSetTimerRunning = false
                try {
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    vibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                } catch (_: Exception) {}
            }
        }
    }

    // IF A WORKOUT IS TAPPED: SHOW THE DEDICATED SEPARATE YOUTUBE VIDEO SECTION
    if (currentSelectedExercise != null) {
        WorkoutExerciseDetailScreen(
            exercise = currentSelectedExercise,
            restRemainingSec = workoutRestRemainingSec,
            isRestRunning = isWorkoutRestRunning,
            onBack = { selectedExerciseId = null },
            onIncrementSet = onIncrementSet,
            onToggleDone = onToggleDone,
            onStartRestTimer = onStartRestTimer,
            onPauseRestTimer = onPauseRestTimer,
            onResumeRestTimer = onResumeRestTimer
        )
        return
    }

    // MAIN SLEEK & SLIM WORKOUT OVERVIEW
    val totalSets = exercises.sumOf { it.targetSets }
    val doneSets = exercises.sumOf { it.completedSets }
    val progressPct = if (isRestDay) 100 else if (totalSets > 0) ((doneSets.toFloat() / totalSets.toFloat()) * 100).toInt().coerceIn(0, 100) else 100

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("workout_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Slim Header Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(AuroraIndigo.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = AuroraIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "WORKOUT SPLIT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AuroraIndigo,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = if (isRestDay) "Active Recovery" else "Push & Core Conditioning",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isRestDay) FrostSuccess.copy(alpha = 0.15f) else AuroraIndigo.copy(alpha = 0.15f),
                            border = BorderStroke(
                                0.6.dp,
                                if (isRestDay) FrostSuccess.copy(alpha = 0.4f) else AuroraIndigo.copy(alpha = 0.4f)
                            )
                        ) {
                            Text(
                                text = if (isRestDay) "REST" else "$progressPct% Complete",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRestDay) FrostSuccess else AuroraIndigo,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Slim Rest Day Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Bed,
                                contentDescription = "Rest",
                                tint = AuroraIndigo,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Rest Day Protection",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Switch(
                            checked = isRestDay,
                            onCheckedChange = { onToggleRestDay(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AuroraIndigo,
                                checkedTrackColor = AuroraIndigo.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }
        }

        // 2. Slim Workout Music Capsule (Music ON / OFF Feature)
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isMusicOn) AuroraIndigo.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    0.7.dp,
                    if (isMusicOn) AuroraIndigo.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = { musicManager.toggleMusic() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isMusicOn) Icons.Outlined.MusicNote else Icons.Outlined.MusicOff,
                                contentDescription = "Toggle Music",
                                tint = if (isMusicOn) AuroraIndigo else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isMusicOn) currentTrack.title else "Workout Music Off",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isMusicOn) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${currentTrack.bpm} BPM",
                                        fontSize = 10.sp,
                                        color = AuroraIndigo,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Text(
                                text = if (isMusicOn) "Gym beat synthesizer active" else "Tap switch to start high-energy rhythm",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isMusicOn) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Pulsing visualizer bars
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier
                                    .height(18.dp)
                                    .padding(end = 6.dp)
                            ) {
                                visualizerAmps.take(4).forEach { amp ->
                                    val barH = (amp * 16).dp.coerceAtLeast(3.dp)
                                    Box(
                                        modifier = Modifier
                                            .width(2.5.dp)
                                            .height(barH)
                                            .clip(RoundedCornerShape(1.dp))
                                            .background(AuroraIndigo)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { musicManager.togglePlayPause() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMusicPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = AuroraIndigo,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { musicManager.nextTrack() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SkipNext,
                                    contentDescription = "Next Track",
                                    tint = AuroraIndigo,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Music Switch
                    Switch(
                        checked = isMusicOn,
                        onCheckedChange = { musicManager.toggleMusic() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AuroraIndigo,
                            checkedTrackColor = AuroraIndigo.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }

        // 3. Slim Set / Rest Countdown Capsule
        if (!isRestDay) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = null,
                                tint = FrostAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Set Timer: ${setTimerRemainingSec}s",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(30, 45, 60).forEach { sec ->
                                Surface(
                                    modifier = Modifier.clickable {
                                        setTimerTargetSec = sec
                                        setTimerRemainingSec = sec
                                        isSetTimerRunning = false
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (setTimerTargetSec == sec) FrostAccent else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(
                                        0.5.dp,
                                        if (setTimerTargetSec == sec) FrostAccent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Text(
                                        text = "${sec}s",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (setTimerTargetSec == sec) Color(0xFF070B14) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { isSetTimerRunning = !isSetTimerRunning },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSetTimerRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                    contentDescription = "Toggle Timer",
                                    tint = FrostAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Workout Circuit List Header
        if (!isRestDay) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Exercises & Circuits",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap any exercise to open dedicated YouTube video & set tutorial",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { showAddExerciseDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 5. Slim, Elegant Workout Exercise Cards
            items(exercises) { ex ->
                val guide = remember(ex.exerciseName) { WorkoutVideoHelper.getGuideForExercise(ex.exerciseName) }
                val isAllSetsDone = ex.completedSets >= ex.targetSets

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // TAP OPENS THE SEPARATE DEDICATED YOUTUBE VIDEO SECTION
                            selectedExerciseId = ex.id
                        },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        0.7.dp,
                        if (ex.isDone) FrostSuccess.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
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
                            // Slim status indicator circle
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (ex.isDone) FrostSuccess.copy(alpha = 0.15f)
                                        else AuroraIndigo.copy(alpha = 0.12f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (ex.isDone) Icons.Outlined.CheckCircle else Icons.Outlined.SmartDisplay,
                                    contentDescription = null,
                                    tint = if (ex.isDone) FrostSuccess else AuroraIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = ex.exerciseName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFFF0000).copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "YouTube Video",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF4E4E),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "${ex.targetSets} sets · ${ex.targetReps} · ${ex.restSeconds}s rest · ${guide.targetMuscles}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                // Slim completion text
                                Text(
                                    text = if (isAllSetsDone) "✓ All sets finished" else "${ex.completedSets}/${ex.targetSets} sets completed",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isAllSetsDone) FrostSuccess else AuroraIndigo
                                )
                            }
                        }

                        // Right action area
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { onToggleDone(ex) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (ex.isDone) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                    contentDescription = "Done",
                                    tint = if (ex.isDone) FrostSuccess else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = "View YouTube Video & Details",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog: Add Exercise
    if (showAddExerciseDialog) {
        var name by remember { mutableStateOf("") }
        var sets by remember { mutableStateOf("3") }
        var reps by remember { mutableStateOf("10-12") }
        var rest by remember { mutableStateOf("60") }

        AlertDialog(
            onDismissRequest = { showAddExerciseDialog = false },
            title = { Text("Add Workout Exercise", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Exercise Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sets,
                            onValueChange = { sets = it.filter { c -> c.isDigit() } },
                            label = { Text("Sets") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = reps,
                            onValueChange = { reps = it },
                            label = { Text("Reps") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = rest,
                            onValueChange = { rest = it.filter { c -> c.isDigit() } },
                            label = { Text("Rest (s)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val setsNum = sets.toIntOrNull() ?: 3
                            val restNum = rest.toIntOrNull() ?: 60
                            onAddExercise(name.trim(), setsNum, reps.trim(), restNum)
                            showAddExerciseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AuroraIndigo)
                ) {
                    Text("Add", fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExerciseDialog = false }) {
                    Text("Cancel", fontSize = 12.sp)
                }
            }
        )
    }
}
