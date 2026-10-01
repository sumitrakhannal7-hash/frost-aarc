package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.FrostAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    profile: UserProfile?,
    onToggleDarkMode: () -> Unit,
    onUpdateWeights: (study: Int, routine: Int, workout: Int, task: Int) -> Unit,
    onResetOnboarding: () -> Unit,
    onDismiss: () -> Unit
) {
    var studyWeight by remember { mutableStateOf((profile?.studyWeight ?: 40).toString()) }
    var routineWeight by remember { mutableStateOf((profile?.routineWeight ?: 25).toString()) }
    var workoutWeight by remember { mutableStateOf((profile?.workoutWeight ?: 25).toString()) }
    var taskWeight by remember { mutableStateOf((profile?.taskWeight ?: 10).toString()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("settings_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "FROSTARC SETTINGS",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = FrostAccent,
                letterSpacing = 1.sp
            )

            // Dark Mode Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (profile?.isDarkMode == true) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "Theme",
                            tint = FrostAccent
                        )
                        Column {
                            Text(text = "Theme Appearance", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (profile?.isDarkMode == true) "Frost Dark Mode Active" else "Frost Light Mode Active",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = profile?.isDarkMode ?: true,
                        onCheckedChange = { onToggleDarkMode() },
                        colors = SwitchDefaults.colors(checkedThumbColor = FrostAccent)
                    )
                }
            }

            // Completion Weights Configuration
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Score Weight Redistribution (Total = 100%)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = studyWeight,
                            onValueChange = { studyWeight = it.filter { c -> c.isDigit() } },
                            label = { Text("Study %") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = routineWeight,
                            onValueChange = { routineWeight = it.filter { c -> c.isDigit() } },
                            label = { Text("Routine %") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = workoutWeight,
                            onValueChange = { workoutWeight = it.filter { c -> c.isDigit() } },
                            label = { Text("Workout %") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = taskWeight,
                            onValueChange = { taskWeight = it.filter { c -> c.isDigit() } },
                            label = { Text("Task %") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Button(
                        onClick = {
                            val s = studyWeight.toIntOrNull() ?: 40
                            val r = routineWeight.toIntOrNull() ?: 25
                            val w = workoutWeight.toIntOrNull() ?: 25
                            val t = taskWeight.toIntOrNull() ?: 10
                            onUpdateWeights(s, r, w, t)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FrostAccent,
                            contentColor = Color(0xFF070B14)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Weights", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // App & Developer Info
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Current Stream: ${profile?.stream ?: "Science"} · ${profile?.grade ?: "Class 12"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FrostAccent
                    )

                    OutlinedButton(
                        onClick = {
                            onResetOnboarding()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reconfigure Stream & Subjects", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "FrostArc Discipline Engine v2.0",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Developer:Subodh",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = FrostAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
