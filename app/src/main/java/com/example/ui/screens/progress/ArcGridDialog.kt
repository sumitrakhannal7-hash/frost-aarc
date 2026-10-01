package com.example.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.ArcDayRecord
import com.example.ui.theme.FrostAccent
import com.example.ui.theme.FrostDanger
import com.example.ui.theme.FrostSuccess
import com.example.ui.theme.FrostWarning

@Composable
fun ArcGridDialog(
    arcDays: List<ArcDayRecord>,
    currentDayNumber: Int,
    onDismiss: () -> Unit
) {
    var selectedDay by remember { mutableStateOf<ArcDayRecord?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .padding(8.dp)
                .testTag("arc_grid_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "60-DAY ARC MATRIX",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = FrostAccent
                        )
                        Text(
                            text = "Day $currentDayNumber in progress",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LegendItem("Done (80%+)", FrostSuccess)
                    LegendItem("Current", FrostAccent)
                    LegendItem("Incomplete", FrostWarning)
                    LegendItem("Future", MaterialTheme.colorScheme.surfaceVariant)
                }

                // 60 Grid Tiles
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(arcDays, key = { it.dayNumber }) { day ->
                        val isCurrent = day.dayNumber == currentDayNumber
                        val isDone = day.completionPercent >= 80

                        val tileColor = when {
                            isCurrent -> FrostAccent
                            isDone -> FrostSuccess
                            day.completionPercent in 1..79 -> FrostWarning
                            day.status == "Missed" -> FrostDanger
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(tileColor)
                                .border(
                                    width = if (isCurrent) 2.dp else 0.dp,
                                    color = if (isCurrent) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedDay = day },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${day.dayNumber}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent || isDone) Color(0xFF070B14) else MaterialTheme.colorScheme.onSurface
                                )
                                if (day.completionPercent > 0) {
                                    Text(
                                        text = "${day.completionPercent}%",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isCurrent || isDone) Color(0xFF070B14) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Selected Day Detail Box
                if (selectedDay != null) {
                    val d = selectedDay!!
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Day ${d.dayNumber} · ${d.dateKey}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = FrostAccent
                            )
                            Text(
                                text = "Status: ${d.status} (${d.completionPercent}%) · Study: ${d.studyMinutes}m · Tasks: ${d.routineCompletedCount}/${d.routineTotalCount}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
