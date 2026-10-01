package com.example.ui.screens.focus

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
import androidx.compose.runtime.*
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
import com.example.ui.theme.FrostSuccess
import com.example.ui.theme.FrostWarning

@Composable
fun FocusScreen(
    subjects: List<Subject>,
    onStartFocusTimer: (Subject, Int) -> Unit,
    onAdjustSubjectMinutes: (Long, Int) -> Unit,
    onUpdateDailyTarget: (Long, Int) -> Unit,
    onOpenBacklogMatrix: () -> Unit,
    onAddNewSubject: (String, Int, String) -> Unit
) {
    var subjectToEditTarget by remember { mutableStateOf<Subject?>(null) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }

    val totalTarget = subjects.sumOf { it.dailyTargetMinutes }
    val totalDone = subjects.sumOf { it.completedMinutesToday }
    val overallPercent = if (totalTarget > 0) ((totalDone.toFloat() / totalTarget.toFloat()) * 100).toInt().coerceIn(0, 100) else 100

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("focus_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hub Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SUBJECT FOCUS HUB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FrostAccent,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalDone / $totalTarget min",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Daily study targets adjustable per subject",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FrostProgressRing(
                        progressPercent = overallPercent,
                        size = 72.dp,
                        strokeWidth = 8.dp
                    )
                }
            }
        }

        // 2. Action Shortcuts: Backlog Matrix & Add Subject
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenBacklogMatrix,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Checklist, contentDescription = "Backlog", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Backlog Matrix", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { showAddSubjectDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FrostAccent,
                        contentColor = Color(0xFF070B14)
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Subject", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Add Subject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 3. Subject List with Adjustable Time Controls
        items(subjects) { subject ->
            val pct = if (subject.dailyTargetMinutes > 0) {
                ((subject.completedMinutesToday.toFloat() / subject.dailyTargetMinutes.toFloat()) * 100).toInt().coerceIn(0, 100)
            } else 100

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Row
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
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(subject.colorHex)))
                            )
                            Text(
                                text = subject.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Backlog status badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (subject.isPending) FrostWarning.copy(alpha = 0.2f) else FrostSuccess.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (subject.isPending) "Pending" else "Up-to-Date",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (subject.isPending) FrostWarning else FrostSuccess,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Progress info & Adjust Target trigger
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${subject.completedMinutesToday} / ${subject.dailyTargetMinutes} minutes today ($pct%)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Total logged: ${subject.totalAccumulatedMinutes}m · ${subject.sessionsCount} sessions",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TextButton(
                            onClick = { subjectToEditTarget = subject },
                            modifier = Modifier.testTag("edit_target_${subject.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Target", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Adjust Target", fontSize = 11.sp, color = FrostAccent)
                        }
                    }

                    // Linear progress bar
                    LinearProgressIndicator(
                        progress = { pct / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = FrostAccent,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )

                    // Quick Session Launch Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onStartFocusTimer(subject, 25) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FrostAccent,
                                contentColor = Color(0xFF070B14)
                            )
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "25m", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "25m Timer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = { onStartFocusTimer(subject, 45) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "45m Deep", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { onAdjustSubjectMinutes(subject.id, 15) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "+15m Log", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    // Dialog: Adjust Target Time
    if (subjectToEditTarget != null) {
        val subject = subjectToEditTarget!!
        var targetInput by remember { mutableStateOf(subject.dailyTargetMinutes.toString()) }

        AlertDialog(
            onDismissRequest = { subjectToEditTarget = null },
            title = {
                Text(
                    text = "Adjust Daily Study Target",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Set daily target minutes for ${subject.name}:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = targetInput,
                        onValueChange = { targetInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Target Minutes") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick preset chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(30, 45, 60, 90).forEach { preset ->
                            SuggestionChip(
                                onClick = { targetInput = preset.toString() },
                                label = { Text("${preset}m") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val minutes = targetInput.toIntOrNull() ?: subject.dailyTargetMinutes
                        onUpdateDailyTarget(subject.id, minutes)
                        subjectToEditTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FrostAccent,
                        contentColor = Color(0xFF070B14)
                    )
                ) {
                    Text("Save Target")
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectToEditTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Add Subject
    if (showAddSubjectDialog) {
        var newName by remember { mutableStateOf("") }
        var newTarget by remember { mutableStateOf("60") }

        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = { Text("Add New Subject", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Subject Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTarget,
                        onValueChange = { newTarget = it.filter { c -> c.isDigit() } },
                        label = { Text("Daily Target (Minutes)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            val mins = newTarget.toIntOrNull() ?: 60
                            onAddNewSubject(newName, mins, "#38BDF8")
                            showAddSubjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FrostAccent,
                        contentColor = Color(0xFF070B14)
                    )
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
