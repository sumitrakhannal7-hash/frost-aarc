package com.example.ui.screens.routine

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RoutineItem
import com.example.ui.theme.FrostAccent
import com.example.ui.theme.FrostDanger
import com.example.ui.theme.FrostSuccess
import com.example.ui.theme.FrostWarning

@Composable
fun RoutineScreen(
    routineItems: List<RoutineItem>,
    onToggleRoutine: (RoutineItem) -> Unit,
    onCompleteAllTasks: () -> Unit,
    onDeleteRoutine: (RoutineItem) -> Unit,
    onOpenAddDialog: () -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    val categories = listOf("All", "Study", "Health", "Meal", "Sleep", "Personal")

    val filteredItems = if (selectedCategoryFilter == "All") {
        routineItems
    } else {
        routineItems.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
    }

    val completedCount = routineItems.count { it.isCompleted }
    val totalCount = routineItems.size
    val totalMins = routineItems.sumOf { it.durationMinutes }
    val completedMins = routineItems.filter { it.isCompleted }.sumOf { it.durationMinutes }
    val completionPct = if (totalCount > 0) ((completedCount.toFloat() / totalCount.toFloat()) * 100).toInt() else 100

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddDialog,
                containerColor = FrostAccent,
                contentColor = Color(0xFF070B14),
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("add_routine_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Routine Task")
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("routine_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header Overview Card
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "MASTER DAILY ROUTINE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FrostSuccess,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "$completedCount of $totalCount Tasks Finished",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = FrostSuccess.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "$completionPct%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FrostSuccess,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 1f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = FrostSuccess,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Text(
                            text = "$completedMins of $totalMins scheduled minutes accomplished today · 25% of Arc Score",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = onCompleteAllTasks,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FrostSuccess,
                                contentColor = Color(0xFF070B14)
                            )
                        ) {
                            Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Complete All Tasks (100% Day Victory)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. Category Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FrostAccent,
                                selectedLabelColor = Color(0xFF070B14)
                            )
                        )
                    }
                }
            }

            // 3. Routine To-Do List Items
            items(filteredItems, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = if (item.isCritical && !item.isCompleted) {
                        BorderStroke(1.dp, FrostWarning.copy(alpha = 0.6f))
                    } else CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = item.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (item.isCritical) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = FrostWarning.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "Critical",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FrostWarning,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = item.startTime,
                                        fontSize = 12.sp,
                                        color = FrostAccent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "· ${item.durationMinutes}m",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "· ${item.category}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { onDeleteRoutine(item) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Task",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
