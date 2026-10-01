package com.example.ui.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AuroraIndigo
import com.example.ui.theme.FrostAccent
import com.example.ui.theme.FrostSuccess
import com.example.ui.theme.IceHighlight

data class SubjectConfig(
    val name: String,
    var isSelected: Boolean = true,
    var targetMinutes: Int = 60,
    val isCustom: Boolean = false
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onCompleteOnboarding: (name: String, grade: String, stream: String, subjects: List<Pair<String, Int>>) -> Unit
) {
    var studentName by remember { mutableStateOf("Subodh") }
    var selectedClass by remember { mutableStateOf("Class 12") }
    var selectedStream by remember { mutableStateOf("Science") } // Science or Management

    val classesList = listOf("Class 11", "Class 12", "Class 10", "College")

    // Stream-based subject presets
    val scienceSubjects = remember {
        mutableStateListOf(
            SubjectConfig("Physics", isSelected = true, targetMinutes = 60),
            SubjectConfig("Chemistry", isSelected = true, targetMinutes = 60),
            SubjectConfig("Mathematics", isSelected = true, targetMinutes = 90),
            SubjectConfig("English", isSelected = true, targetMinutes = 30),
            SubjectConfig("Nepali", isSelected = true, targetMinutes = 30),
            SubjectConfig("Computer Science", isSelected = true, targetMinutes = 45),
            SubjectConfig("Biology", isSelected = false, targetMinutes = 60)
        )
    }

    val managementSubjects = remember {
        mutableStateListOf(
            SubjectConfig("Accountancy", isSelected = true, targetMinutes = 60),
            SubjectConfig("Economics", isSelected = true, targetMinutes = 60),
            SubjectConfig("Business Studies", isSelected = true, targetMinutes = 45),
            SubjectConfig("English", isSelected = true, targetMinutes = 30),
            SubjectConfig("Nepali", isSelected = true, targetMinutes = 30),
            SubjectConfig("Business Mathematics", isSelected = true, targetMinutes = 60),
            SubjectConfig("Computer Science", isSelected = false, targetMinutes = 45),
            SubjectConfig("Hotel Management", isSelected = false, targetMinutes = 45)
        )
    }

    val additionalSubjects = remember { mutableStateListOf<SubjectConfig>() }
    var showAddCustomDialog by remember { mutableStateOf(false) }

    val activeList = if (selectedStream == "Science") scienceSubjects else managementSubjects

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("onboarding_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App Header & Branding
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.5.dp, FrostAccent, RoundedCornerShape(16.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.frost_arc_logo),
                        contentDescription = "FrostArc Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = "FROSTARC SETUP",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = FrostAccent,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Configure your profile, stream & subjects to calibrate your 60-Day Arc engine.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Section 1: Name and Class
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "1. STUDENT PROFILE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FrostAccent,
                        letterSpacing = 1.sp
                    )

                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("Your Name / Scholar Handle") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Select Class / Grade:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            classesList.forEach { c ->
                                FilterChip(
                                    selected = selectedClass == c,
                                    onClick = { selectedClass = c },
                                    label = { Text(c, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FrostAccent,
                                        selectedLabelColor = Color(0xFF070B14)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Choose Stream (Science vs Management)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "2. CHOOSE ACADEMIC STREAM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FrostAccent,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Science Card
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedStream = "Science" },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selectedStream == "Science") FrostAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (selectedStream == "Science") BorderStroke(2.dp, FrostAccent) else BorderStroke(1.dp, Color.Transparent)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = "Science",
                                    tint = if (selectedStream == "Science") FrostAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "Science",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedStream == "Science") FrostAccent else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "PCM / PCB / Comp",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Management Card
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedStream = "Management" },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selectedStream == "Management") AuroraIndigo.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (selectedStream == "Management") BorderStroke(2.dp, AuroraIndigo) else BorderStroke(1.dp, Color.Transparent)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QueryStats,
                                    contentDescription = "Management",
                                    tint = if (selectedStream == "Management") AuroraIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "Management",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedStream == "Management") AuroraIndigo else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Account / Econ / Bus",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Select & Adjust Stream Subjects
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "3. CONFIGURE $selectedStream SUBJECTS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FrostAccent,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Check the subjects you have; uncheck any you don't take.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Subjects list with checkbox and target minutes
                    activeList.forEachIndexed { index, sub ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Checkbox(
                                        checked = sub.isSelected,
                                        onCheckedChange = { checked ->
                                            activeList[index] = sub.copy(isSelected = checked)
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = FrostAccent, checkmarkColor = Color(0xFF070B14))
                                    )
                                    Text(
                                        text = sub.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (sub.isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (sub.isSelected) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (sub.targetMinutes > 15) {
                                                    activeList[index] = sub.copy(targetMinutes = sub.targetMinutes - 15)
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                        }
                                        Text(
                                            text = "${sub.targetMinutes}m",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FrostAccent
                                        )
                                        IconButton(
                                            onClick = {
                                                if (sub.targetMinutes < 180) {
                                                    activeList[index] = sub.copy(targetMinutes = sub.targetMinutes + 15)
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Additional Custom Subjects
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "4. ADDITIONAL SUBJECTS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FrostSuccess,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Add optional or custom subjects (e.g. Opt Math, Marketing).",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showAddCustomDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FrostSuccess,
                                contentColor = Color(0xFF070B14)
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Subject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (additionalSubjects.isNotEmpty()) {
                        additionalSubjects.forEachIndexed { idx, customSub ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${customSub.name} (${customSub.targetMinutes}m/day)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FrostSuccess
                                    )

                                    IconButton(
                                        onClick = { additionalSubjects.removeAt(idx) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No additional subjects added yet.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Section 5: Launch Button
        item {
            Button(
                onClick = {
                    val chosen = mutableListOf<Pair<String, Int>>()
                    activeList.filter { it.isSelected }.forEach {
                        chosen.add(Pair(it.name, it.targetMinutes))
                    }
                    additionalSubjects.forEach {
                        chosen.add(Pair(it.name, it.targetMinutes))
                    }

                    if (chosen.isEmpty()) {
                        // Keep at least 1 subject
                        chosen.add(Pair(if (selectedStream == "Science") "Physics" else "Accountancy", 60))
                    }

                    onCompleteOnboarding(
                        studentName.ifBlank { "Subodh" },
                        selectedClass,
                        selectedStream,
                        chosen
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("launch_arc_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FrostAccent,
                    contentColor = Color(0xFF070B14)
                )
            ) {
                Icon(imageVector = Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LAUNCH 60-DAY FROST ARC",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }

    // Dialog: Add Custom Additional Subject
    if (showAddCustomDialog) {
        var customName by remember { mutableStateOf("") }
        var customTarget by remember { mutableStateOf("45") }

        AlertDialog(
            onDismissRequest = { showAddCustomDialog = false },
            title = { Text("Add Additional Subject", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Subject Name") },
                        placeholder = { Text("e.g. Marketing, Biology, Opt Math") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customTarget,
                        onValueChange = { customTarget = it.filter { c -> c.isDigit() } },
                        label = { Text("Daily Study Target (Minutes)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customName.isNotBlank()) {
                            val mins = customTarget.toIntOrNull() ?: 45
                            additionalSubjects.add(SubjectConfig(name = customName.trim(), targetMinutes = mins, isCustom = true))
                            showAddCustomDialog = false
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
                TextButton(onClick = { showAddCustomDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
