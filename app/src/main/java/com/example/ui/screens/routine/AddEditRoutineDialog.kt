package com.example.ui.screens.routine

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FrostAccent

@Composable
fun AddEditRoutineDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, startTime: String, durationMinutes: Int, isCritical: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Study") }
    var startTime by remember { mutableStateOf("08:00 AM") }
    var durationMinutesText by remember { mutableStateOf("30") }
    var isCritical by remember { mutableStateOf(false) }

    val categories = listOf("Study", "Health", "Meal", "Sleep", "Personal")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Routine Task",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    placeholder = { Text("e.g. Physics Formula Review") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Selector
                Text(
                    text = "Category:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FrostAccent,
                                selectedLabelColor = Color(0xFF070B14)
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = durationMinutesText,
                        onValueChange = { durationMinutesText = it.filter { c -> c.isDigit() } },
                        label = { Text("Duration (min)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Mark as Critical / Priority",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = isCritical,
                        onCheckedChange = { isCritical = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = FrostAccent)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val duration = durationMinutesText.toIntOrNull() ?: 30
                        onSave(title, selectedCategory, startTime, duration, isCritical)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = FrostAccent,
                    contentColor = Color(0xFF070B14)
                ),
                modifier = Modifier.testTag("save_routine_button")
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
