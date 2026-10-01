package com.example.ui.screens.workout

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.WorkoutExercise
import com.example.ui.theme.AuroraIndigo
import com.example.ui.theme.FrostAccent
import com.example.ui.theme.FrostSuccess

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutExerciseDetailScreen(
    exercise: WorkoutExercise,
    restRemainingSec: Int,
    isRestRunning: Boolean,
    onBack: () -> Unit,
    onIncrementSet: (WorkoutExercise) -> Unit,
    onToggleDone: (WorkoutExercise) -> Unit,
    onStartRestTimer: (Int) -> Unit,
    onPauseRestTimer: () -> Unit,
    onResumeRestTimer: () -> Unit
) {
    val context = LocalContext.current
    val guide = remember(exercise.exerciseName) { WorkoutVideoHelper.getGuideForExercise(exercise.exerciseName) }

    // Active video being played in the embedded player (defaults to main guide, can be switched per set)
    var currentVideoId by remember { mutableStateOf(guide.youtubeId) }
    var currentVideoTitle by remember { mutableStateOf(guide.exerciseName) }

    // Helper to launch official YouTube app with web browser fallback
    val openYouTube = { videoId: String ->
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId"))
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId"))
        try {
            context.startActivity(appIntent)
        } catch (_: Exception) {
            try {
                context.startActivity(webIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open browser: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Intercept back gesture
    BackHandler { onBack() }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back to Workout List",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = exercise.exerciseName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${exercise.targetSets} sets · ${exercise.targetReps} · ${exercise.restSeconds}s rest",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Complete Toggle
                    IconButton(
                        onClick = { onToggleDone(exercise) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (exercise.isDone) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = "Mark Complete",
                            tint = if (exercise.isDone) FrostSuccess else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        },
        modifier = Modifier.testTag("workout_exercise_detail_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. YouTube Video Player Embed Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)),
                    border = BorderStroke(0.8.dp, AuroraIndigo.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Responsive Video Player with modern Chrome user agent
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                .background(Color.Black)
                        ) {
                            key(currentVideoId) {
                                AndroidView(
                                    factory = { ctx ->
                                        WebView(ctx).apply {
                                            settings.apply {
                                                javaScriptEnabled = true
                                                domStorageEnabled = true
                                                databaseEnabled = true
                                                mediaPlaybackRequiresUserGesture = false
                                                userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                                            }
                                            webChromeClient = WebChromeClient()
                                            webViewClient = WebViewClient()
                                            loadDataWithBaseURL(
                                                "https://www.youtube.com",
                                                """
                                                <!DOCTYPE html>
                                                <html>
                                                <head>
                                                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                                    <style>
                                                        * { margin:0; padding:0; box-sizing:border-box; }
                                                        body { background-color:#070B14; display:flex; justify-content:center; align-items:center; height:100vh; overflow:hidden; }
                                                        iframe { width:100%; height:100%; border:none; }
                                                    </style>
                                                </head>
                                                <body>
                                                    <iframe src="https://www.youtube.com/embed/${currentVideoId}?autoplay=1&playsinline=1&rel=0&enablejsapi=1&origin=https://www.youtube.com" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
                                                </body>
                                                </html>
                                                """.trimIndent(),
                                                "text/html",
                                                "utf-8",
                                                null
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Direct YouTube Launch Actions & Fallback
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentVideoTitle,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = guide.targetMuscles,
                                        fontSize = 11.sp,
                                        color = AuroraIndigo
                                    )
                                }

                                // 1-Tap Watch on YouTube App Button
                                Button(
                                    onClick = { openYouTube(currentVideoId) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFF0000),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.SmartDisplay,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open in YouTube", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Notice with 1-tap copy
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "If video playback is restricted by YouTube, tap 'Open in YouTube' above",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("YouTube Video Link", "https://www.youtube.com/watch?v=$currentVideoId")
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "YouTube video link copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentCopy,
                                        contentDescription = "Copy Link",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Rest Interval Countdown Bar
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isRestRunning) AuroraIndigo.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        0.8.dp,
                        if (isRestRunning) AuroraIndigo else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.HourglassTop,
                                contentDescription = "Rest Timer",
                                tint = if (isRestRunning) AuroraIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = if (isRestRunning) "REST INTERVAL ACTIVE" else "SET REST TIMER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRestRunning) AuroraIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = if (isRestRunning) "$restRemainingSec seconds remaining" else "${exercise.restSeconds}s recommended rest",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (isRestRunning) {
                                OutlinedButton(
                                    onClick = onPauseRestTimer,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Pause", fontSize = 11.sp)
                                }
                            } else {
                                Button(
                                    onClick = { onStartRestTimer(exercise.restSeconds) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AuroraIndigo),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Start Rest", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Set-by-Set Video Links & Tracking Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Workout Sets & Video Guides",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${exercise.completedSets} of ${exercise.targetSets} Sets Done",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuroraIndigo
                    )
                }
            }

            // Individual Set Cards with Specific Video Links
            items(guide.setTutorials.take(exercise.targetSets.coerceAtLeast(1))) { setTut ->
                val setNumber = setTut.setNumber
                val isCompleted = setNumber <= exercise.completedSets
                val isCurrentlyPlayingThisSet = (currentVideoId == setTut.youtubeId)

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isCompleted) FrostSuccess.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        0.8.dp,
                        if (isCurrentlyPlayingThisSet) AuroraIndigo
                        else if (isCompleted) FrostSuccess.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isCompleted) FrostSuccess.copy(alpha = 0.2f) else AuroraIndigo.copy(alpha = 0.2f),
                                    border = BorderStroke(
                                        0.6.dp,
                                        if (isCompleted) FrostSuccess.copy(alpha = 0.4f) else AuroraIndigo.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Text(
                                        text = "SET 0$setNumber",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCompleted) FrostSuccess else AuroraIndigo,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = setTut.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Interactive Check Set Button
                            Button(
                                onClick = {
                                    onIncrementSet(exercise)
                                    onStartRestTimer(exercise.restSeconds)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = if (isCompleted) {
                                    ButtonDefaults.buttonColors(
                                        containerColor = FrostSuccess.copy(alpha = 0.25f),
                                        contentColor = FrostSuccess
                                    )
                                } else {
                                    ButtonDefaults.buttonColors(
                                        containerColor = AuroraIndigo,
                                        contentColor = Color.White
                                    )
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCompleted) Icons.Outlined.Check else Icons.Outlined.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isCompleted) "Completed" else "Log Set",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Coaching focus tip
                        Text(
                            text = setTut.focusTip,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )

                        // Dual Action: Play In-App Player OR Open in YouTube App
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Switch In-App Player
                            OutlinedButton(
                                onClick = {
                                    currentVideoId = setTut.youtubeId
                                    currentVideoTitle = setTut.title
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCurrentlyPlayingThisSet) Icons.Outlined.PlayArrow else Icons.Outlined.SmartDisplay,
                                    contentDescription = null,
                                    tint = AuroraIndigo,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isCurrentlyPlayingThisSet) "Playing Above" else "Play In Player",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Open Directly in YouTube App
                            Button(
                                onClick = { openYouTube(setTut.youtubeId) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF0000).copy(alpha = 0.15f),
                                    contentColor = Color(0xFFFF4E4E)
                                ),
                                border = BorderStroke(0.6.dp, Color(0xFFFF0000).copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open on YouTube", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 4. Form Cues & Common Mistakes (Clean, slim cards)
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Verified,
                                contentDescription = null,
                                tint = FrostSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Proper Form & Execution Cues",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = FrostSuccess
                            )
                        }

                        guide.formCues.forEach { cue ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(text = "•", fontSize = 13.sp, color = FrostSuccess, fontWeight = FontWeight.Bold)
                                Text(
                                    text = cue,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.WarningAmber,
                                contentDescription = null,
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Mistakes to Avoid",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF87171)
                            )
                        }

                        guide.commonMistakes.forEach { mistake ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(text = "✕", fontSize = 11.sp, color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                                Text(
                                    text = mistake,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
