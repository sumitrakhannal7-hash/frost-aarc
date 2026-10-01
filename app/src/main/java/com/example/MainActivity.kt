package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FrostTab
import com.example.ui.FrostViewModel
import com.example.ui.components.*
import com.example.ui.screens.focus.FocusScreen
import com.example.ui.screens.focus.FocusTimerSheet
import com.example.ui.screens.gemini.GeminiChatScreen
import com.example.ui.screens.guide.AiGuideSheet
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.progress.AchievementsDialog
import com.example.ui.screens.progress.ArcGridDialog
import com.example.ui.screens.progress.ProgressScreen
import com.example.ui.screens.routine.AddEditRoutineDialog
import com.example.ui.screens.routine.RoutineScreen
import com.example.ui.screens.settings.BacklogSheet
import com.example.ui.screens.settings.SettingsSheet
import com.example.ui.screens.workout.WorkoutRestTimerSheet
import com.example.ui.screens.workout.WorkoutScreen
import com.example.ui.theme.AuroraIndigo
import com.example.ui.theme.FrostAccent
import com.example.ui.theme.FrostArcTheme

class MainActivity : ComponentActivity() {

    private val viewModel: FrostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val profile by viewModel.profile.collectAsStateWithLifecycle()
            val isDarkMode = profile?.isDarkMode ?: true

            FrostArcTheme(darkTheme = isDarkMode) {
                val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
                val subjects by viewModel.subjects.collectAsStateWithLifecycle()
                val routineItems by viewModel.routineItems.collectAsStateWithLifecycle()
                val workoutExercises by viewModel.workoutExercises.collectAsStateWithLifecycle()
                val arcDays by viewModel.arcDays.collectAsStateWithLifecycle()
                val achievements by viewModel.achievements.collectAsStateWithLifecycle()
                val recoveryTasks by viewModel.recoveryTasks.collectAsStateWithLifecycle()
                val studySessions by viewModel.studySessions.collectAsStateWithLifecycle()

                // Focus Timer States
                val isFocusTimerRunning by viewModel.isFocusTimerRunning.collectAsStateWithLifecycle()
                val focusTimerSubject by viewModel.focusTimerSubject.collectAsStateWithLifecycle()
                val focusTargetMinutes by viewModel.focusTargetMinutes.collectAsStateWithLifecycle()
                val focusElapsedSeconds by viewModel.focusElapsedSeconds.collectAsStateWithLifecycle()

                // Workout Rest Timer States
                val isWorkoutTimerRunning by viewModel.isWorkoutTimerRunning.collectAsStateWithLifecycle()
                val workoutRestRemainingSec by viewModel.workoutRestRemainingSec.collectAsStateWithLifecycle()
                val workoutRestTargetSec by viewModel.workoutRestTargetSec.collectAsStateWithLifecycle()

                // Dialog and Sheet States
                val showAiGuideSheet by viewModel.showAiGuideSheet.collectAsStateWithLifecycle()
                val showSettingsSheet by viewModel.showSettingsSheet.collectAsStateWithLifecycle()
                val showBacklogSheet by viewModel.showBacklogSheet.collectAsStateWithLifecycle()
                val showArcGridDialog by viewModel.showArcGridDialog.collectAsStateWithLifecycle()
                val showAchievementsDialog by viewModel.showAchievementsDialog.collectAsStateWithLifecycle()
                val showAddRoutineDialog by viewModel.showAddRoutineDialog.collectAsStateWithLifecycle()
                val showFocusTimerSheet by viewModel.showFocusTimerSheet.collectAsStateWithLifecycle()
                val showWorkoutTimerSheet by viewModel.showWorkoutTimerSheet.collectAsStateWithLifecycle()
                val showCongratulationsPopup by viewModel.showCongratulationsPopup.collectAsStateWithLifecycle()
                val congratulationsStreak by viewModel.congratulationsStreak.collectAsStateWithLifecycle()

                val guideMessages by viewModel.guideMessages.collectAsStateWithLifecycle()
                val isGuideThinking by viewModel.isGuideThinking.collectAsStateWithLifecycle()

                if (profile == null || !profile!!.isOnboarded) {
                    OnboardingScreen(
                        onCompleteOnboarding = { name, grade, stream, subjectsList ->
                            viewModel.completeOnboarding(name, grade, stream, subjectsList)
                        }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopBarHeader(
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = { viewModel.toggleDarkMode() },
                                onOpenAiGuide = { viewModel.selectTab(FrostTab.GEMINI) },
                                onOpenSettings = { viewModel.showSettingsSheet.value = true }
                            )
                        },
                    bottomBar = {
                        Column(modifier = Modifier.navigationBarsPadding()) {
                            // Mini Timer Bar if Focus Timer is active and sheet is closed
                            if ((isFocusTimerRunning || focusElapsedSeconds > 0) && !showFocusTimerSheet) {
                                MiniTimerBar(
                                    subject = focusTimerSubject,
                                    targetMinutes = focusTargetMinutes,
                                    elapsedSeconds = focusElapsedSeconds,
                                    isRunning = isFocusTimerRunning,
                                    onTogglePlayPause = {
                                        if (isFocusTimerRunning) viewModel.pauseFocusTimer()
                                        else viewModel.startFocusTimer()
                                    },
                                    onComplete = { viewModel.completeFocusSession() },
                                    onClickBar = { viewModel.showFocusTimerSheet.value = true }
                                )
                            }

                            // Navigation Bar
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == FrostTab.HOME,
                                    onClick = { viewModel.selectTab(FrostTab.HOME) },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF070B14),
                                        selectedTextColor = FrostAccent,
                                        indicatorColor = FrostAccent
                                    ),
                                    modifier = Modifier.testTag("tab_home")
                                )
                                NavigationBarItem(
                                    selected = currentTab == FrostTab.FOCUS,
                                    onClick = { viewModel.selectTab(FrostTab.FOCUS) },
                                    icon = { Icon(Icons.Default.Timer, contentDescription = "Focus") },
                                    label = { Text("Focus", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF070B14),
                                        selectedTextColor = FrostAccent,
                                        indicatorColor = FrostAccent
                                    ),
                                    modifier = Modifier.testTag("tab_focus")
                                )
                                NavigationBarItem(
                                    selected = currentTab == FrostTab.WORKOUT,
                                    onClick = { viewModel.selectTab(FrostTab.WORKOUT) },
                                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Workout") },
                                    label = { Text("Workout", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF070B14),
                                        selectedTextColor = AuroraIndigo,
                                        indicatorColor = AuroraIndigo
                                    ),
                                    modifier = Modifier.testTag("tab_workout")
                                )
                                NavigationBarItem(
                                    selected = currentTab == FrostTab.GEMINI,
                                    onClick = { viewModel.selectTab(FrostTab.GEMINI) },
                                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Gemini AI") },
                                    label = { Text("Gemini", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF070B14),
                                        selectedTextColor = FrostAccent,
                                        indicatorColor = FrostAccent
                                    ),
                                    modifier = Modifier.testTag("tab_gemini")
                                )
                                NavigationBarItem(
                                    selected = currentTab == FrostTab.ROUTINE,
                                    onClick = { viewModel.selectTab(FrostTab.ROUTINE) },
                                    icon = { Icon(Icons.Default.Checklist, contentDescription = "Routine") },
                                    label = { Text("Routine", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF070B14),
                                        selectedTextColor = FrostAccent,
                                        indicatorColor = FrostAccent
                                    ),
                                    modifier = Modifier.testTag("tab_routine")
                                )
                                NavigationBarItem(
                                    selected = currentTab == FrostTab.PROGRESS,
                                    onClick = { viewModel.selectTab(FrostTab.PROGRESS) },
                                    icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = "Progress") },
                                    label = { Text("Progress", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF070B14),
                                        selectedTextColor = FrostAccent,
                                        indicatorColor = FrostAccent
                                    ),
                                    modifier = Modifier.testTag("tab_progress")
                                )
                            }

                            // Developer Signature Footer as requested
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Developer:Subodh",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            FrostTab.HOME -> HomeScreen(
                                profile = profile,
                                subjects = subjects,
                                routineItems = routineItems,
                                workoutExercises = workoutExercises,
                                recoveryTasks = recoveryTasks,
                                onNavigateTab = { viewModel.selectTab(it) },
                                onToggleRoutine = { viewModel.toggleRoutineItem(it) },
                                onCompleteAllTasks = { viewModel.markAllTasksCompletedToday() },
                                onToggleRecovery = { viewModel.toggleRecoveryTask(it) },
                                onOpenAiGuide = { viewModel.selectTab(FrostTab.GEMINI) },
                                onOpenArcGrid = { viewModel.showArcGridDialog.value = true },
                                onQuickStartFocus = { subject ->
                                    viewModel.openFocusTimerForSubject(subject, 25)
                                }
                            )
                            FrostTab.FOCUS -> FocusScreen(
                                subjects = subjects,
                                onStartFocusTimer = { subject, minutes ->
                                    viewModel.openFocusTimerForSubject(subject, minutes)
                                },
                                onAdjustSubjectMinutes = { id, delta ->
                                    viewModel.adjustSubjectMinutes(id, delta)
                                },
                                onUpdateDailyTarget = { id, target ->
                                    viewModel.updateSubjectDailyTarget(id, target)
                                },
                                onOpenBacklogMatrix = { viewModel.showBacklogSheet.value = true },
                                onAddNewSubject = { name, target, color ->
                                    viewModel.addSubject(name, target, color)
                                }
                            )
                            FrostTab.GEMINI -> GeminiChatScreen(
                                userName = profile?.name?.ifBlank { "Subodh" } ?: "Subodh",
                                messages = guideMessages,
                                isThinking = isGuideThinking,
                                onSendMessage = { viewModel.sendGuidePrompt(it) },
                                onApplyAction = { viewModel.applyGuideAction(it) },
                                onClearChat = { viewModel.clearGuideMessages() }
                            )
                            FrostTab.ROUTINE -> RoutineScreen(
                                routineItems = routineItems,
                                onToggleRoutine = { viewModel.toggleRoutineItem(it) },
                                onCompleteAllTasks = { viewModel.markAllTasksCompletedToday() },
                                onDeleteRoutine = { viewModel.deleteRoutineItem(it) },
                                onOpenAddDialog = { viewModel.showAddRoutineDialog.value = true }
                            )
                            FrostTab.WORKOUT -> WorkoutScreen(
                                exercises = workoutExercises,
                                workoutRestRemainingSec = workoutRestRemainingSec,
                                workoutRestTargetSec = workoutRestTargetSec,
                                isWorkoutRestRunning = isWorkoutTimerRunning,
                                onStartRestTimer = { viewModel.startWorkoutRestTimer(it) },
                                onPauseRestTimer = { viewModel.pauseWorkoutRestTimer() },
                                onResumeRestTimer = { viewModel.resumeWorkoutRestTimer() },
                                onResetRestTimer = { viewModel.startWorkoutRestTimer(workoutRestTargetSec) },
                                onAdjustRestTime = { viewModel.adjustWorkoutRest(it) },
                                onIncrementSet = { viewModel.incrementWorkoutSet(it) },
                                onToggleDone = { viewModel.toggleWorkoutExerciseDone(it) },
                                onToggleRestDay = { viewModel.setRestDay(it) },
                                onDeleteExercise = { viewModel.deleteWorkoutExercise(it) },
                                onAddExercise = { name, sets, reps, rest ->
                                    viewModel.addWorkoutExercise(name, sets, reps, rest)
                                }
                            )
                            FrostTab.PROGRESS -> ProgressScreen(
                                profile = profile,
                                arcDays = arcDays,
                                subjects = subjects,
                                achievements = achievements,
                                studySessions = studySessions,
                                onOpenArcGridFull = { viewModel.showArcGridDialog.value = true },
                                onOpenAchievementsFull = { viewModel.showAchievementsDialog.value = true }
                            )
                        }
                    }
                }

                // ==========================================
                // SHEETS & DIALOGS
                // ==========================================

                // Focus Timer Sheet
                if (showFocusTimerSheet) {
                    FocusTimerSheet(
                        subject = focusTimerSubject,
                        targetMinutes = focusTargetMinutes,
                        elapsedSeconds = focusElapsedSeconds,
                        isRunning = isFocusTimerRunning,
                        onStartTimer = { viewModel.startFocusTimer() },
                        onPauseTimer = { viewModel.pauseFocusTimer() },
                        onResetTimer = { viewModel.resetFocusTimer() },
                        onCompleteSession = { viewModel.completeFocusSession() },
                        onChangeDuration = { viewModel.setFocusTargetMinutes(it) },
                        onDismiss = { viewModel.showFocusTimerSheet.value = false }
                    )
                }

                // Workout Rest Timer Sheet
                if (showWorkoutTimerSheet) {
                    WorkoutRestTimerSheet(
                        remainingSeconds = workoutRestRemainingSec,
                        targetSeconds = workoutRestTargetSec,
                        isRunning = isWorkoutTimerRunning,
                        onTogglePlayPause = {
                            if (isWorkoutTimerRunning) viewModel.pauseWorkoutRestTimer()
                            else viewModel.startWorkoutRestTimer(workoutRestRemainingSec)
                        },
                        onAdjustTime = { viewModel.adjustWorkoutRest(it) },
                        onDismiss = { viewModel.dismissWorkoutTimer() }
                    )
                }

                // AI Frost Guide Sheet
                if (showAiGuideSheet) {
                    AiGuideSheet(
                        messages = guideMessages,
                        isThinking = isGuideThinking,
                        onSendMessage = { viewModel.sendGuidePrompt(it) },
                        onApplyAction = { viewModel.applyGuideAction(it) },
                        onDismiss = { viewModel.showAiGuideSheet.value = false }
                    )
                }

                // Add Routine Task Dialog
                if (showAddRoutineDialog) {
                    AddEditRoutineDialog(
                        onDismiss = { viewModel.showAddRoutineDialog.value = false },
                        onSave = { title, category, startTime, duration, isCritical ->
                            viewModel.addRoutineItem(title, category, startTime, duration, isCritical)
                        }
                    )
                }

                // 60-Day Arc Grid Dialog
                if (showArcGridDialog) {
                    ArcGridDialog(
                        arcDays = arcDays,
                        currentDayNumber = profile?.arcCurrentDay ?: 1,
                        onDismiss = { viewModel.showArcGridDialog.value = false }
                    )
                }

                // Achievements Dialog
                if (showAchievementsDialog) {
                    AchievementsDialog(
                        achievements = achievements,
                        onDismiss = { viewModel.showAchievementsDialog.value = false }
                    )
                }

                // Backlog Matrix Sheet
                if (showBacklogSheet) {
                    BacklogSheet(
                        subjects = subjects,
                        onUpdateBacklog = { id, isPending, chapters, notes ->
                            viewModel.updateSubjectBacklog(id, isPending, chapters, notes)
                        },
                        onDismiss = { viewModel.showBacklogSheet.value = false }
                    )
                }

                // Settings Sheet
                if (showSettingsSheet) {
                    SettingsSheet(
                        profile = profile,
                        onToggleDarkMode = { viewModel.toggleDarkMode() },
                        onUpdateWeights = { s, r, w, t ->
                            viewModel.updateWeights(s, r, w, t)
                        },
                        onResetOnboarding = { viewModel.resetOnboarding() },
                        onDismiss = { viewModel.showSettingsSheet.value = false }
                    )
                }

                // Congratulations 100% Completion Popup
                if (showCongratulationsPopup) {
                    CongratulationsDialog(
                        streakCount = congratulationsStreak,
                        onDismiss = { viewModel.dismissCongratulationsPopup() }
                    )
                }
            } // Close else branch for Scaffold
            }
        }
    }
}
