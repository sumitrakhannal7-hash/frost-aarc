package com.example.data.repository

import com.example.data.db.FrostDao
import com.example.data.model.*
import com.example.engine.CompletionEngine
import com.example.engine.CompletionResult
import com.example.engine.XpEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

class FrostRepository(
    private val dao: FrostDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    // Flows exposed to ViewModel and UI
    val profileFlow: Flow<UserProfile?> = dao.getProfileFlow()
    val subjectsFlow: Flow<List<Subject>> = dao.getAllSubjects()
    val routineItemsFlow: Flow<List<RoutineItem>> = dao.getAllRoutineItems()
    val workoutExercisesFlow: Flow<List<WorkoutExercise>> = dao.getAllWorkoutExercises()
    val arcDaysFlow: Flow<List<ArcDayRecord>> = dao.getAllArcDays()
    val achievementsFlow: Flow<List<AchievementItem>> = dao.getAllAchievements()
    val recoveryTasksFlow: Flow<List<RecoveryTask>> = dao.getAllRecoveryTasks()
    val studySessionsFlow: Flow<List<StudySessionLog>> = dao.getAllSessions()

    // Real-time Event for Congratulations Popup when 100% is reached
    private val _congratulationsEvent = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val congratulationsEvent: SharedFlow<Int> = _congratulationsEvent.asSharedFlow()

    init {
        scope.launch {
            initializeDefaultsIfEmpty()
        }
    }

    private suspend fun initializeDefaultsIfEmpty() = withContext(Dispatchers.IO) {
        val existingProfile = dao.getProfile()
        if (existingProfile == null) {
            val todayStr = getTodayKey()

            // 1. Profile
            val profile = UserProfile(
                name = "",
                grade = "Class 12",
                stream = "Science",
                isOnboarded = false,
                arcStartDate = todayStr,
                arcCurrentDay = 1,
                streak = 0,
                longestStreak = 0,
                shields = 3,
                totalXp = 0,
                level = 1,
                isDarkMode = true,
                todayCompletionPercent = 0,
                todayIsPerfect = false,
                hasShown100PercentPopupToday = false
            )
            dao.insertProfile(profile)

            // Do not insert default subjects here; user chooses stream and subjects during onboarding!

            // 2. Pre-fill Today's Workout
            val initialWorkout = listOf(
                WorkoutExercise(dayKey = todayStr, workoutName = "Push & Core Power", isRestDay = false, exerciseName = "Explosive Push-ups", targetSets = 4, completedSets = 0, targetReps = "12-15 reps", restSeconds = 60, orderIndex = 0),
                WorkoutExercise(dayKey = todayStr, workoutName = "Push & Core Power", isRestDay = false, exerciseName = "Pike Push-ups / Shoulder Press", targetSets = 3, completedSets = 0, targetReps = "10-12 reps", restSeconds = 60, orderIndex = 1),
                WorkoutExercise(dayKey = todayStr, workoutName = "Push & Core Power", isRestDay = false, exerciseName = "Bench / Chair Tricep Dips", targetSets = 3, completedSets = 0, targetReps = "12 reps", restSeconds = 45, orderIndex = 2),
                WorkoutExercise(dayKey = todayStr, workoutName = "Push & Core Power", isRestDay = false, exerciseName = "Hanging Knee Raises / Plank", targetSets = 3, completedSets = 0, targetReps = "45 sec", restSeconds = 45, orderIndex = 3)
            )
            dao.insertWorkoutExercises(initialWorkout)

            // 3. Pre-fill 60-Day Arc Grid
            val arcDays = mutableListOf<ArcDayRecord>()
            val calendar = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            for (i in 1..60) {
                val dateStr = sdf.format(calendar.time)
                arcDays.add(
                    ArcDayRecord(
                        dayNumber = i,
                        dateKey = dateStr,
                        status = if (i == 1) "Current" else "Future",
                        completionPercent = 0,
                        studyMinutes = 0,
                        routineCompletedCount = 0,
                        routineTotalCount = 6,
                        workoutCompleted = false,
                        xpEarned = 0,
                        isPerfect = false
                    )
                )
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            dao.insertArcDays(arcDays)

            // 6. Pre-fill Achievements Catalog
            val achievements = listOf(
                AchievementItem(id = "first_focus", title = "First Spark", description = "Complete your first deep focus study session", iconName = "bolt", isUnlocked = false),
                AchievementItem(id = "first_workout", title = "Iron Will", description = "Complete all sets in a daily workout session", iconName = "fitness_center", isUnlocked = false),
                AchievementItem(id = "first_day_complete", title = "Day One Victor", description = "Achieve 100% completion in a single day", iconName = "emoji_events", isUnlocked = false),
                AchievementItem(id = "streak_3", title = "Frost Initiate", description = "Maintain a 3-day consecutive study streak", iconName = "local_fire_department", isUnlocked = false),
                AchievementItem(id = "streak_7", title = "Glacier Momentum", description = "Achieve a 7-day unbroken discipline streak", iconName = "whatshot", isUnlocked = false),
                AchievementItem(id = "streak_14", title = "Two Weeks of Steel", description = "Crush 14 straight days of transformation", iconName = "military_tech", isUnlocked = false),
                AchievementItem(id = "streak_30", title = "Halfway Master", description = "Hit 30 consecutive days of unwavering focus", iconName = "star", isUnlocked = false),
                AchievementItem(id = "streak_60", title = "Frost Arc Legend", description = "Conquer the entire 60-Day Arc transformation", iconName = "workspace_premium", isUnlocked = false),
                AchievementItem(id = "perfect_day", title = "Perfectionist", description = "Finish 100% of study, routine, and workout targets", iconName = "verified", isUnlocked = false),
                AchievementItem(id = "deep_work_2h", title = "Deep Work Dynamo", description = "Log 120+ minutes of focused study in one day", iconName = "timer", isUnlocked = false),
                AchievementItem(id = "century_hours", title = "Century Scholar", description = "Accumulate 100 total hours of logged study", iconName = "auto_awesome", isUnlocked = false),
                AchievementItem(id = "no_backlog", title = "Clean Slate", description = "Clear all subjects to Up-to-Date status", iconName = "task_alt", isUnlocked = false),
                AchievementItem(id = "level_5", title = "Snowdrift Forged", description = "Reach Level 5 in the Frost progression system", iconName = "shield", isUnlocked = false)
            )
            dao.insertAchievements(achievements)
        } else if (existingProfile.name != "Subodh") {
            dao.updateProfile(existingProfile.copy(name = "Subodh"))
        }
    }

    // Helper: today's date key
    fun getTodayKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // ==========================================
    // REAL-TIME PROGRESS & ACHIEVEMENTS TRIGGER
    // ==========================================
    suspend fun updateDailyProgressAndAchievements() = withContext(Dispatchers.IO) {
        val profile = dao.getProfile() ?: return@withContext
        val subjects = dao.getAllSubjects().first()
        val routine = dao.getAllRoutineItems().first()
        val workout = dao.getAllWorkoutExercises().first()
        val tasks = dao.getAllRecoveryTasks().first()

        val result: CompletionResult = CompletionEngine.compute(
            subjects = subjects,
            routineItems = routine,
            workoutExercises = workout,
            recoveryTasks = tasks,
            baseStudyWeight = profile.studyWeight,
            baseRoutineWeight = profile.routineWeight,
            baseWorkoutWeight = profile.workoutWeight,
            baseTaskWeight = profile.taskWeight
        )

        var newStreak = profile.streak
        var newLongest = profile.longestStreak
        var newTotalXp = profile.totalXp
        var newlyCompleted100Percent = false
        var showPopup = false

        // Check if 100% reached
        if (result.isPerfect && (!profile.todayIsPerfect || !profile.hasShown100PercentPopupToday)) {
            // Instantly add 1 streak and trigger congratulations popup!
            newStreak += 1
            if (newStreak > newLongest) {
                newLongest = newStreak
            }
            newTotalXp += 150 // 100% completion bonus XP
            newlyCompleted100Percent = true
            showPopup = true

            // Notify UI immediately via SharedFlow
            _congratulationsEvent.tryEmit(newStreak)
        }

        val newLevel = XpEngine.calculateLevel(newTotalXp)

        val updatedProfile = profile.copy(
            todayCompletionPercent = result.totalPercent,
            todayIsPerfect = result.isPerfect,
            streak = newStreak,
            longestStreak = newLongest,
            totalXp = newTotalXp,
            level = newLevel,
            hasShown100PercentPopupToday = profile.hasShown100PercentPopupToday || newlyCompleted100Percent
        )
        dao.updateProfile(updatedProfile)

        // Instantly update today's ArcDayRecord (Day 1 or current)
        val arcDay = dao.getArcDay(profile.arcCurrentDay)
        if (arcDay != null) {
            val totalStudyMins = subjects.sumOf { it.completedMinutesToday }
            val routineDoneCount = routine.count { it.isCompleted }
            val workoutDone = workout.isNotEmpty() && workout.all { it.isDone || it.completedSets >= it.targetSets || it.isRestDay }
            val status = when {
                result.totalPercent >= 80 -> "Completed"
                result.totalPercent in 30..79 -> "Incomplete"
                result.totalPercent > 0 -> "Current"
                else -> "Current"
            }

            val updatedArcDay = arcDay.copy(
                completionPercent = result.totalPercent,
                studyMinutes = totalStudyMins,
                routineCompletedCount = routineDoneCount,
                routineTotalCount = routine.size,
                workoutCompleted = workoutDone,
                isPerfect = result.isPerfect,
                status = status
            )
            dao.updateArcDay(updatedArcDay)
        }

        // Evaluate achievements reactively!
        evaluateAchievements(updatedProfile, subjects, routine, workout)
    }

    private suspend fun evaluateAchievements(
        profile: UserProfile,
        subjects: List<Subject>,
        routine: List<RoutineItem>,
        workout: List<WorkoutExercise>
    ) {
        val achievements = dao.getAllAchievements().first()
        val totalStudyMinutes = subjects.sumOf { it.completedMinutesToday }
        val workoutDone = workout.isNotEmpty() && workout.all { it.isDone || it.completedSets >= it.targetSets || it.isRestDay }

        val todayDate = getTodayKey()

        for (item in achievements) {
            if (item.isUnlocked) continue
            var unlocked = false

            when (item.id) {
                "first_focus" -> if (totalStudyMinutes > 0) unlocked = true
                "first_workout" -> if (workoutDone) unlocked = true
                "first_day_complete" -> if (profile.todayCompletionPercent == 100) unlocked = true
                "streak_3" -> if (profile.streak >= 3) unlocked = true
                "streak_7" -> if (profile.streak >= 7) unlocked = true
                "streak_14" -> if (profile.streak >= 14) unlocked = true
                "streak_30" -> if (profile.streak >= 30) unlocked = true
                "streak_60" -> if (profile.streak >= 60) unlocked = true
                "perfect_day" -> if (profile.todayIsPerfect) unlocked = true
                "deep_work_2h" -> if (totalStudyMinutes >= 120) unlocked = true
                "no_backlog" -> if (subjects.isNotEmpty() && subjects.none { it.isPending }) unlocked = true
                "level_5" -> if (profile.level >= 5) unlocked = true
            }

            if (unlocked) {
                dao.updateAchievement(
                    item.copy(isUnlocked = true, unlockedDate = todayDate, progress = 1f)
                )
            }
        }
    }

    // ==========================================
    // STUDY & FOCUS HUB (USER ADJUSTABLE TIME)
    // ==========================================
    suspend fun updateSubjectDailyTarget(subjectId: Long, newTargetMinutes: Int) = withContext(Dispatchers.IO) {
        val subject = dao.getSubjectById(subjectId) ?: return@withContext
        val clampedTarget = max(5, newTargetMinutes)
        dao.updateSubject(subject.copy(dailyTargetMinutes = clampedTarget))
        updateDailyProgressAndAchievements()
    }

    suspend fun logStudySession(subjectId: Long, minutes: Int, isManual: Boolean = false) = withContext(Dispatchers.IO) {
        val subject = dao.getSubjectById(subjectId) ?: return@withContext
        val newDone = subject.completedMinutesToday + minutes
        val newTotal = subject.totalAccumulatedMinutes + minutes
        val newSessions = subject.sessionsCount + 1

        dao.updateSubject(
            subject.copy(
                completedMinutesToday = newDone,
                totalAccumulatedMinutes = newTotal,
                sessionsCount = newSessions
            )
        )

        // Write session log
        dao.insertSession(
            StudySessionLog(
                subjectId = subjectId,
                subjectName = subject.name,
                durationMinutes = minutes,
                dateKey = getTodayKey(),
                isManual = isManual
            )
        )

        // Award XP: 1 XP per minute (if manual, half XP)
        val xpGain = if (isManual) (minutes / 2) else minutes
        addXp(xpGain)

        // Instantly recalculate daily completion & arc
        updateDailyProgressAndAchievements()
    }

    suspend fun adjustSubjectMinutes(subjectId: Long, delta: Int) = withContext(Dispatchers.IO) {
        val subject = dao.getSubjectById(subjectId) ?: return@withContext
        val newDone = max(0, subject.completedMinutesToday + delta)
        dao.updateSubject(subject.copy(completedMinutesToday = newDone))
        updateDailyProgressAndAchievements()
    }

    suspend fun addSubject(name: String, targetMinutes: Int, colorHex: String) = withContext(Dispatchers.IO) {
        dao.insertSubject(
            Subject(
                name = name.trim(),
                dailyTargetMinutes = max(5, targetMinutes),
                colorHex = colorHex
            )
        )
        updateDailyProgressAndAchievements()
    }

    suspend fun updateSubjectBacklogStatus(subjectId: Long, isPending: Boolean, pendingChapters: Int, notes: String) = withContext(Dispatchers.IO) {
        val subject = dao.getSubjectById(subjectId) ?: return@withContext
        val wasPending = subject.isPending
        dao.updateSubject(
            subject.copy(
                isPending = isPending,
                pendingChapters = pendingChapters,
                notes = notes
            )
        )
        // If transitioning from Pending to Up-to-Date: grant +30 XP!
        if (wasPending && !isPending) {
            addXp(30)
        }
        updateDailyProgressAndAchievements()
    }

    // ==========================================
    // ROUTINE TO-DO LIST (USER CUSTOMIZABLE)
    // ==========================================
    suspend fun toggleRoutineItem(item: RoutineItem) = withContext(Dispatchers.IO) {
        val newStatus = !item.isCompleted
        dao.updateRoutineItem(item.copy(isCompleted = newStatus))

        // XP on completion, reversal on uncheck
        if (newStatus) {
            addXp(10)
        } else {
            addXp(-10)
        }

        // Auto-credit matching subject study minutes
        val allSubs = dao.getAllSubjects().first()
        val matchingSubject = allSubs.find {
            item.title.contains(it.name, ignoreCase = true) ||
            (it.name.contains("Math", ignoreCase = true) && item.title.contains("Calculus", ignoreCase = true))
        }
        if (matchingSubject != null) {
            val delta = if (newStatus) item.durationMinutes else -item.durationMinutes
            val newMins = max(0, matchingSubject.completedMinutesToday + delta)
            dao.updateSubject(matchingSubject.copy(completedMinutesToday = newMins))
        }

        // Auto-credit workout if routine item is workout
        if (item.category.equals("Health", ignoreCase = true) || item.title.contains("Workout", ignoreCase = true) || item.title.contains("Push", ignoreCase = true)) {
            val allWorkout = dao.getAllWorkoutExercises().first()
            for (w in allWorkout) {
                if (newStatus) {
                    dao.updateWorkoutExercise(w.copy(completedSets = w.targetSets, isDone = true))
                }
            }
        }

        // If ALL routine to-do items are now completed, satisfy all daily requirements to guarantee 100%!
        val currentRoutine = dao.getAllRoutineItems().first()
        if (currentRoutine.isNotEmpty() && currentRoutine.all { it.isCompleted }) {
            for (sub in allSubs) {
                if (sub.completedMinutesToday < sub.dailyTargetMinutes) {
                    dao.updateSubject(sub.copy(completedMinutesToday = sub.dailyTargetMinutes))
                }
            }
            val allWorkout = dao.getAllWorkoutExercises().first()
            for (w in allWorkout) {
                if (!w.isDone) {
                    dao.updateWorkoutExercise(w.copy(completedSets = w.targetSets, isDone = true))
                }
            }
            val tasks = dao.getAllRecoveryTasks().first()
            for (t in tasks) {
                if (!t.isCompleted) {
                    dao.updateRecoveryTask(t.copy(isCompleted = true))
                }
            }
        }

        updateDailyProgressAndAchievements()
    }

    suspend fun markAllTasksCompletedToday() = withContext(Dispatchers.IO) {
        // Mark all routine items complete
        val routine = dao.getAllRoutineItems().first()
        for (item in routine) {
            if (!item.isCompleted) {
                dao.updateRoutineItem(item.copy(isCompleted = true))
                addXp(10)
            }
        }

        // Satisfy all subject study targets
        val subjects = dao.getAllSubjects().first()
        for (sub in subjects) {
            if (sub.completedMinutesToday < sub.dailyTargetMinutes) {
                dao.updateSubject(sub.copy(completedMinutesToday = sub.dailyTargetMinutes))
            }
        }

        // Mark all workout exercises complete
        val workout = dao.getAllWorkoutExercises().first()
        for (ex in workout) {
            dao.updateWorkoutExercise(ex.copy(completedSets = ex.targetSets, isDone = true))
        }

        // Mark any recovery tasks complete
        val tasks = dao.getAllRecoveryTasks().first()
        for (t in tasks) {
            dao.updateRecoveryTask(t.copy(isCompleted = true))
        }

        updateDailyProgressAndAchievements()
    }

    suspend fun addRoutineItem(title: String, category: String, startTime: String, durationMinutes: Int, isCritical: Boolean) = withContext(Dispatchers.IO) {
        val all = dao.getAllRoutineItems().first()
        val nextOrder = (all.maxOfOrNull { it.orderIndex } ?: 0) + 1
        dao.insertRoutineItem(
            RoutineItem(
                title = title.trim(),
                category = category,
                startTime = startTime,
                durationMinutes = durationMinutes,
                isCritical = isCritical,
                orderIndex = nextOrder,
                dayKey = getTodayKey()
            )
        )
        updateDailyProgressAndAchievements()
    }

    suspend fun updateRoutineItem(item: RoutineItem) = withContext(Dispatchers.IO) {
        dao.updateRoutineItem(item)
        updateDailyProgressAndAchievements()
    }

    suspend fun deleteRoutineItem(item: RoutineItem) = withContext(Dispatchers.IO) {
        dao.deleteRoutineItem(item)
        updateDailyProgressAndAchievements()
    }

    // ==========================================
    // WORKOUT & REST TIMER
    // ==========================================
    suspend fun incrementWorkoutSet(exercise: WorkoutExercise) = withContext(Dispatchers.IO) {
        val newSets = min(exercise.completedSets + 1, exercise.targetSets)
        val isDone = newSets >= exercise.targetSets
        dao.updateWorkoutExercise(
            exercise.copy(
                completedSets = newSets,
                isDone = isDone
            )
        )
        addXp(8)
        updateDailyProgressAndAchievements()
    }

    suspend fun toggleWorkoutExerciseDone(exercise: WorkoutExercise) = withContext(Dispatchers.IO) {
        val newDone = !exercise.isDone
        val newSets = if (newDone) exercise.targetSets else 0
        dao.updateWorkoutExercise(
            exercise.copy(
                isDone = newDone,
                completedSets = newSets
            )
        )
        if (newDone) addXp(25) else addXp(-25)
        updateDailyProgressAndAchievements()
    }

    suspend fun setRestDay(isRest: Boolean) = withContext(Dispatchers.IO) {
        val exercises = dao.getAllWorkoutExercises().first()
        for (ex in exercises) {
            dao.updateWorkoutExercise(ex.copy(isRestDay = isRest))
        }
        updateDailyProgressAndAchievements()
    }

    suspend fun addWorkoutExercise(name: String, sets: Int, reps: String, restSec: Int) = withContext(Dispatchers.IO) {
        val all = dao.getAllWorkoutExercises().first()
        val nextOrder = (all.maxOfOrNull { it.orderIndex } ?: 0) + 1
        dao.insertWorkoutExercise(
            WorkoutExercise(
                dayKey = getTodayKey(),
                exerciseName = name.trim(),
                targetSets = sets,
                targetReps = reps,
                restSeconds = restSec,
                orderIndex = nextOrder
            )
        )
        updateDailyProgressAndAchievements()
    }

    suspend fun deleteWorkoutExercise(exercise: WorkoutExercise) = withContext(Dispatchers.IO) {
        dao.deleteWorkoutExercise(exercise)
        updateDailyProgressAndAchievements()
    }

    // ==========================================
    // RECOVERY TASKS
    // ==========================================
    suspend fun toggleRecoveryTask(task: RecoveryTask) = withContext(Dispatchers.IO) {
        val newStatus = !task.isCompleted
        dao.updateRecoveryTask(task.copy(isCompleted = newStatus))
        if (newStatus) addXp(20) else addXp(-20)
        updateDailyProgressAndAchievements()
    }

    // ==========================================
    // PROFILE & SETTINGS
    // ==========================================
    suspend fun toggleDarkMode() = withContext(Dispatchers.IO) {
        val profile = dao.getProfile() ?: return@withContext
        dao.updateProfile(profile.copy(isDarkMode = !profile.isDarkMode))
    }

    suspend fun updateProfileWeights(study: Int, routine: Int, workout: Int, task: Int) = withContext(Dispatchers.IO) {
        val profile = dao.getProfile() ?: return@withContext
        dao.updateProfile(
            profile.copy(
                studyWeight = study,
                routineWeight = routine,
                workoutWeight = workout,
                taskWeight = task
            )
        )
        updateDailyProgressAndAchievements()
    }

    suspend fun dismissCongratulationsPopup() = withContext(Dispatchers.IO) {
        val profile = dao.getProfile() ?: return@withContext
        dao.updateProfile(profile.copy(hasShown100PercentPopupToday = true))
    }

    suspend fun completeOnboarding(
        name: String,
        grade: String,
        stream: String,
        subjectsList: List<Pair<String, Int>> // (SubjectName, TargetMinutes)
    ) = withContext(Dispatchers.IO) {
        val todayStr = getTodayKey()
        val currentProfile = dao.getProfile() ?: UserProfile()

        val updatedProfile = currentProfile.copy(
            name = if (name.isNotBlank()) name.trim() else "Subodh",
            grade = grade,
            stream = stream,
            isOnboarded = true
        )
        dao.insertProfile(updatedProfile)

        // Clear previous default subjects completely
        dao.deleteAllSubjects()

        val colors = listOf("#38BDF8", "#818CF8", "#34D399", "#FBBF24", "#F472B6", "#A78BFA", "#38BDF8", "#F87171")
        val newSubjects = subjectsList.mapIndexed { index, pair ->
            Subject(
                name = pair.first.trim(),
                colorHex = colors[index % colors.size],
                dailyTargetMinutes = pair.second.coerceAtLeast(10)
            )
        }
        dao.insertSubjects(newSubjects)

        // Recreate tailored daily routine to-do list for this student
        val routine = mutableListOf<RoutineItem>()
        routine.add(
            RoutineItem(
                title = "Morning Formula & Concept Recall",
                category = "Study",
                startTime = "06:30 AM",
                durationMinutes = 30,
                isCritical = true,
                orderIndex = 0,
                dayKey = todayStr
            )
        )
        routine.add(
            RoutineItem(
                title = "Hydration & Morning Mobility",
                category = "Health",
                startTime = "07:15 AM",
                durationMinutes = 15,
                isCritical = false,
                orderIndex = 1,
                dayKey = todayStr
            )
        )

        var order = 2
        var hour = 9
        for (sub in newSubjects) {
            val period = if (hour >= 12) "PM" else "AM"
            val displayHour = if (hour > 12) hour - 12 else hour
            routine.add(
                RoutineItem(
                    title = "${sub.name} Practice & Problem Solving",
                    category = "Study",
                    startTime = String.format("%02d:00 %s", displayHour, period),
                    durationMinutes = sub.dailyTargetMinutes.coerceAtMost(60),
                    isCritical = order <= 4,
                    orderIndex = order++,
                    dayKey = todayStr
                )
            )
            hour += 2
            if (hour == 13) hour = 14
        }

        routine.add(
            RoutineItem(
                title = if (stream == "Science") "Science Lab & Conceptual Audit" else "Business Case Review & Account Audit",
                category = "Study",
                startTime = "04:30 PM",
                durationMinutes = 45,
                isCritical = false,
                orderIndex = order++,
                dayKey = todayStr
            )
        )
        routine.add(
            RoutineItem(
                title = "Push/Pull Workout & Core Conditioning",
                category = "Health",
                startTime = "06:00 PM",
                durationMinutes = 45,
                isCritical = false,
                orderIndex = order++,
                dayKey = todayStr
            )
        )
        routine.add(
            RoutineItem(
                title = "Daily Reflection, Day Audit & Sleep Wind-down",
                category = "Sleep",
                startTime = "10:00 PM",
                durationMinutes = 20,
                isCritical = false,
                orderIndex = order,
                dayKey = todayStr
            )
        )
        dao.insertRoutineItems(routine)

        updateDailyProgressAndAchievements()
    }

    suspend fun resetOnboarding() = withContext(Dispatchers.IO) {
        val profile = dao.getProfile() ?: return@withContext
        dao.updateProfile(profile.copy(isOnboarded = false))
    }

    private suspend fun addXp(amount: Int) {
        val profile = dao.getProfile() ?: return
        val newTotal = max(0, profile.totalXp + amount)
        val newLevel = XpEngine.calculateLevel(newTotal)
        dao.updateProfile(profile.copy(totalXp = newTotal, level = newLevel))
    }
}
