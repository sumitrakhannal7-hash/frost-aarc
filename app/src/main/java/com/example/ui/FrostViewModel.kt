package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiService
import com.example.data.api.GuideAction
import com.example.data.api.GuideMessage
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.FrostRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class FrostTab {
    HOME, FOCUS, GEMINI, ROUTINE, PROGRESS, WORKOUT
}

class FrostViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = FrostRepository(db.frostDao(), viewModelScope)
    private val geminiService = GeminiService()

    // Database reactive StateFlows
    val profile: StateFlow<UserProfile?> = repository.profileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val subjects: StateFlow<List<Subject>> = repository.subjectsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routineItems: StateFlow<List<RoutineItem>> = repository.routineItemsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutExercises: StateFlow<List<WorkoutExercise>> = repository.workoutExercisesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val arcDays: StateFlow<List<ArcDayRecord>> = repository.arcDaysFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<AchievementItem>> = repository.achievementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recoveryTasks: StateFlow<List<RecoveryTask>> = repository.recoveryTasksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studySessions: StateFlow<List<StudySessionLog>> = repository.studySessionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation Tab
    var currentTab = MutableStateFlow(FrostTab.HOME)
        private set

    // Dialogs / Sheets State
    val showAiGuideSheet = MutableStateFlow(false)
    val showSettingsSheet = MutableStateFlow(false)
    val showBacklogSheet = MutableStateFlow(false)
    val showArcGridDialog = MutableStateFlow(false)
    val showAchievementsDialog = MutableStateFlow(false)
    val showAddRoutineDialog = MutableStateFlow(false)
    val showFocusTimerSheet = MutableStateFlow(false)
    val showWorkoutTimerSheet = MutableStateFlow(false)
    val showCongratulationsPopup = MutableStateFlow(false)
    val congratulationsStreak = MutableStateFlow(1)

    // ==========================================
    // FOCUS TIMER ENGINE (TIMESTAMP-BASED)
    // ==========================================
    val isFocusTimerRunning = MutableStateFlow(false)
    val focusTimerSubject = MutableStateFlow<Subject?>(null)
    val focusTargetMinutes = MutableStateFlow(25) // Adjustable by user
    val focusElapsedSeconds = MutableStateFlow(0)
    val isFocusBreakMode = MutableStateFlow(false)
    private var focusTimerJob: Job? = null
    private var focusTimerStartTime: Long = 0
    private var focusTimerAccumulatedSec: Int = 0

    // ==========================================
    // WORKOUT REST TIMER
    // ==========================================
    val isWorkoutTimerRunning = MutableStateFlow(false)
    val workoutRestRemainingSec = MutableStateFlow(60)
    val workoutRestTargetSec = MutableStateFlow(60)
    private var workoutTimerJob: Job? = null

    // ==========================================
    // AI GUIDE CHAT STATE
    // ==========================================
    val guideMessages = MutableStateFlow<List<GuideMessage>>(
        listOf(
            GuideMessage(
                sender = "frost_ai",
                text = "Welcome to FrostArc! I am your AI Frost Guide. I can guide you through every feature of the app, recommend high-impact routines, and adjust session durations. What are we tackling today?"
            )
        )
    )
    val isGuideThinking = MutableStateFlow(false)

    init {
        // Observe 100% Congratulations Event from Repository
        viewModelScope.launch {
            repository.congratulationsEvent.collect { newStreak ->
                congratulationsStreak.value = newStreak
                showCongratulationsPopup.value = true
            }
        }
    }

    fun selectTab(tab: FrostTab) {
        currentTab.value = tab
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            repository.toggleDarkMode()
        }
    }

    // ==========================================
    // FOCUS & STUDY DURATION CONTROLS
    // ==========================================
    fun setFocusTargetMinutes(minutes: Int) {
        focusTargetMinutes.value = minutes.coerceAtLeast(1)
    }

    fun openFocusTimerForSubject(subject: Subject, targetMinutes: Int = 25) {
        focusTimerSubject.value = subject
        focusTargetMinutes.value = targetMinutes
        focusElapsedSeconds.value = 0
        focusTimerAccumulatedSec = 0
        isFocusBreakMode.value = false
        showFocusTimerSheet.value = true
    }

    fun startFocusTimer() {
        if (isFocusTimerRunning.value) return
        isFocusTimerRunning.value = true
        focusTimerStartTime = System.currentTimeMillis()

        focusTimerJob?.cancel()
        focusTimerJob = viewModelScope.launch {
            while (isActive && isFocusTimerRunning.value) {
                delay(1000)
                val elapsedSinceStart = ((System.currentTimeMillis() - focusTimerStartTime) / 1000).toInt()
                val totalElapsed = focusTimerAccumulatedSec + elapsedSinceStart
                focusElapsedSeconds.value = totalElapsed

                val targetSeconds = focusTargetMinutes.value * 60
                if (totalElapsed >= targetSeconds && !isFocusBreakMode.value) {
                    completeFocusSession()
                    break
                }
            }
        }
    }

    fun pauseFocusTimer() {
        if (!isFocusTimerRunning.value) return
        isFocusTimerRunning.value = false
        val elapsedSinceStart = ((System.currentTimeMillis() - focusTimerStartTime) / 1000).toInt()
        focusTimerAccumulatedSec += elapsedSinceStart
        focusElapsedSeconds.value = focusTimerAccumulatedSec
        focusTimerJob?.cancel()
    }

    fun resetFocusTimer() {
        isFocusTimerRunning.value = false
        focusTimerJob?.cancel()
        focusTimerAccumulatedSec = 0
        focusElapsedSeconds.value = 0
    }

    fun completeFocusSession() {
        pauseFocusTimer()
        val subject = focusTimerSubject.value
        val loggedMins = (focusElapsedSeconds.value / 60).coerceAtLeast(1)

        if (subject != null) {
            viewModelScope.launch {
                repository.logStudySession(subject.id, loggedMins, isManual = false)
            }
        }
        resetFocusTimer()
        showFocusTimerSheet.value = false
    }

    fun logManualStudySession(subjectId: Long, minutes: Int) {
        viewModelScope.launch {
            repository.logStudySession(subjectId, minutes, isManual = true)
        }
    }

    fun updateSubjectDailyTarget(subjectId: Long, newTargetMinutes: Int) {
        viewModelScope.launch {
            repository.updateSubjectDailyTarget(subjectId, newTargetMinutes)
        }
    }

    fun adjustSubjectMinutes(subjectId: Long, delta: Int) {
        viewModelScope.launch {
            repository.adjustSubjectMinutes(subjectId, delta)
        }
    }

    fun addSubject(name: String, targetMinutes: Int, colorHex: String) {
        viewModelScope.launch {
            repository.addSubject(name, targetMinutes, colorHex)
        }
    }

    fun updateSubjectBacklog(subjectId: Long, isPending: Boolean, pendingChapters: Int, notes: String) {
        viewModelScope.launch {
            repository.updateSubjectBacklogStatus(subjectId, isPending, pendingChapters, notes)
        }
    }

    // ==========================================
    // ROUTINE TO-DO LIST CONTROLS
    // ==========================================
    fun toggleRoutineItem(item: RoutineItem) {
        viewModelScope.launch {
            repository.toggleRoutineItem(item)
        }
    }

    fun markAllTasksCompletedToday() {
        viewModelScope.launch {
            repository.markAllTasksCompletedToday()
        }
    }

    fun addRoutineItem(title: String, category: String, startTime: String, durationMinutes: Int, isCritical: Boolean) {
        viewModelScope.launch {
            repository.addRoutineItem(title, category, startTime, durationMinutes, isCritical)
        }
    }

    fun deleteRoutineItem(item: RoutineItem) {
        viewModelScope.launch {
            repository.deleteRoutineItem(item)
        }
    }

    // ==========================================
    // WORKOUT CONTROLS
    // ==========================================
    fun incrementWorkoutSet(exercise: WorkoutExercise) {
        viewModelScope.launch {
            repository.incrementWorkoutSet(exercise)
            // Auto trigger rest timer if rest seconds > 0
            startWorkoutRestTimer(exercise.restSeconds)
        }
    }

    fun toggleWorkoutExerciseDone(exercise: WorkoutExercise) {
        viewModelScope.launch {
            repository.toggleWorkoutExerciseDone(exercise)
        }
    }

    fun setRestDay(isRest: Boolean) {
        viewModelScope.launch {
            repository.setRestDay(isRest)
        }
    }

    fun addWorkoutExercise(name: String, sets: Int, reps: String, restSec: Int) {
        viewModelScope.launch {
            repository.addWorkoutExercise(name, sets, reps, restSec)
        }
    }

    fun deleteWorkoutExercise(exercise: WorkoutExercise) {
        viewModelScope.launch {
            repository.deleteWorkoutExercise(exercise)
        }
    }

    fun startWorkoutRestTimer(seconds: Int = 60) {
        workoutRestTargetSec.value = seconds
        workoutRestRemainingSec.value = seconds
        isWorkoutTimerRunning.value = true
        showWorkoutTimerSheet.value = true

        workoutTimerJob?.cancel()
        workoutTimerJob = viewModelScope.launch {
            while (isActive && workoutRestRemainingSec.value > 0) {
                delay(1000)
                workoutRestRemainingSec.value -= 1
            }
            isWorkoutTimerRunning.value = false
        }
    }

    fun pauseWorkoutRestTimer() {
        isWorkoutTimerRunning.value = false
        workoutTimerJob?.cancel()
    }

    fun resumeWorkoutRestTimer() {
        if (workoutRestRemainingSec.value <= 0) return
        isWorkoutTimerRunning.value = true
        workoutTimerJob?.cancel()
        workoutTimerJob = viewModelScope.launch {
            while (isActive && workoutRestRemainingSec.value > 0) {
                delay(1000)
                workoutRestRemainingSec.value -= 1
            }
            isWorkoutTimerRunning.value = false
        }
    }

    fun adjustWorkoutRest(deltaSeconds: Int) {
        workoutRestRemainingSec.value = (workoutRestRemainingSec.value + deltaSeconds).coerceAtLeast(0)
    }

    fun dismissWorkoutTimer() {
        isWorkoutTimerRunning.value = false
        workoutTimerJob?.cancel()
        showWorkoutTimerSheet.value = false
    }

    // ==========================================
    // RECOVERY TASKS & POPUP
    // ==========================================
    fun toggleRecoveryTask(task: RecoveryTask) {
        viewModelScope.launch {
            repository.toggleRecoveryTask(task)
        }
    }

    fun dismissCongratulationsPopup() {
        showCongratulationsPopup.value = false
        viewModelScope.launch {
            repository.dismissCongratulationsPopup()
        }
    }

    fun updateWeights(study: Int, routine: Int, workout: Int, task: Int) {
        viewModelScope.launch {
            repository.updateProfileWeights(study, routine, workout, task)
        }
    }

    // ==========================================
    // AI FROST GUIDE INTERACTION
    // ==========================================
    fun sendGuidePrompt(prompt: String) {
        if (prompt.isBlank()) return
        val currentHistory = guideMessages.value
        val userMsg = GuideMessage(sender = "user", text = prompt.trim())
        guideMessages.value = currentHistory + userMsg

        isGuideThinking.value = true
        viewModelScope.launch {
            val prof = profile.value
            val currentDay = prof?.arcCurrentDay ?: 1
            val streak = prof?.streak ?: 0

            val subs = subjects.value.joinToString(", ") { "${it.name}: ${it.completedMinutesToday}/${it.dailyTargetMinutes}m" }
            val rout = routineItems.value.joinToString(", ") { "${it.title} (${if (it.isCompleted) "Done" else "Pending"})" }

            val response = geminiService.getGuideResponse(
                userPrompt = prompt,
                currentDay = currentDay,
                streak = streak,
                subjectsSummary = subs,
                routineSummary = rout,
                previousMessages = currentHistory
            )

            isGuideThinking.value = false
            guideMessages.value = guideMessages.value + response
        }
    }

    fun clearGuideMessages() {
        guideMessages.value = emptyList()
    }

    fun applyGuideAction(action: GuideAction) {
        when (action.actionType) {
            "APPLY_STUDY_TARGET" -> {
                val subject = subjects.value.find { it.name.contains(action.subjectName ?: "", ignoreCase = true) }
                if (subject != null) {
                    updateSubjectDailyTarget(subject.id, action.durationMinutes)
                    val confirmMsg = GuideMessage(
                        sender = "frost_ai",
                        text = "Updated ${subject.name} daily target to ${action.durationMinutes} minutes! Your Arc completion calculations have adjusted accordingly."
                    )
                    guideMessages.value = guideMessages.value + confirmMsg
                }
            }
            "SET_TIMER_DURATION" -> {
                focusTargetMinutes.value = action.durationMinutes
                val confirmMsg = GuideMessage(
                    sender = "frost_ai",
                    text = "Focus session duration calibrated to ${action.durationMinutes} minutes. Open the Focus tab or tap Start Session whenever you are ready."
                )
                guideMessages.value = guideMessages.value + confirmMsg
            }
            "NAVIGATE_FEATURE" -> {
                if (action.label.contains("Routine", ignoreCase = true)) {
                    currentTab.value = FrostTab.ROUTINE
                    showAiGuideSheet.value = false
                } else if (action.label.contains("Workout", ignoreCase = true)) {
                    currentTab.value = FrostTab.WORKOUT
                    showAiGuideSheet.value = false
                } else if (action.label.contains("Arc", ignoreCase = true)) {
                    currentTab.value = FrostTab.PROGRESS
                    showAiGuideSheet.value = false
                }
            }
        }
    }

    fun completeOnboarding(
        name: String,
        grade: String,
        stream: String,
        subjectsList: List<Pair<String, Int>>
    ) {
        viewModelScope.launch {
            repository.completeOnboarding(name, grade, stream, subjectsList)
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            repository.resetOnboarding()
        }
    }
}
