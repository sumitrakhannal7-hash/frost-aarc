package com.example.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.ui.theme.FrostAccent
import com.example.ui.theme.FrostSuccess
import com.example.ui.theme.FrostWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BacklogSheet(
    subjects: List<Subject>,
    onUpdateBacklog: (subjectId: Long, isPending: Boolean, pendingChapters: Int, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val upToDateCount = subjects.count { !it.isPending }
    val totalCount = subjects.size

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("backlog_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "NOTES & BACKLOG AUDIT MATRIX",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = FrostAccent,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$upToDateCount of $totalCount Subjects Up-to-Date (+30 XP when cleared)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(subjects) { subject ->
                    var isPending by remember(subject.isPending) { mutableStateOf(subject.isPending) }
                    var chaptersText by remember(subject.pendingChapters) { mutableStateOf(subject.pendingChapters.toString()) }
                    var notesText by remember(subject.notes) { mutableStateOf(subject.notes) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = if (isPending) BorderStroke(1.dp, FrostWarning.copy(alpha = 0.6f)) else BorderStroke(1.dp, FrostSuccess.copy(alpha = 0.4f))
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
                                Text(
                                    text = subject.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Button(
                                    onClick = {
                                        val newPending = !isPending
                                        isPending = newPending
                                        val chap = chaptersText.toIntOrNull() ?: 0
                                        onUpdateBacklog(subject.id, newPending, chap, notesText)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isPending) FrostWarning else FrostSuccess,
                                        contentColor = Color(0xFF070B14)
                                    )
                                ) {
                                    Text(
                                        text = if (isPending) "Pending" else "Up-to-Date ✓",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (isPending) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = chaptersText,
                                        onValueChange = {
                                            chaptersText = it.filter { c -> c.isDigit() }
                                            val chap = chaptersText.toIntOrNull() ?: 0
                                            onUpdateBacklog(subject.id, isPending, chap, notesText)
                                        },
                                        label = { Text("Pending Chapters", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = notesText,
                                        onValueChange = {
                                            notesText = it
                                            val chap = chaptersText.toIntOrNull() ?: 0
                                            onUpdateBacklog(subject.id, isPending, chap, notesText)
                                        },
                                        label = { Text("Chapter Topics", fontSize = 11.sp) },
                                        modifier = Modifier.weight(2f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
