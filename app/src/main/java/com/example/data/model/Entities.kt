package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Student",
    val grade: String = "Class 12",
    val stream: String = "Science",
    val isOnboarded: Boolean = false,
    val arcStartDate: String = "2026-09-01",
    val arcCurrentDay: Int = 1,
    val streak: Int = 0,
    val longestStreak: Int = 0,
    val shields: Int = 3,
    val totalXp: Int = 0,
    val level: Int = 1,
    val isDarkMode: Boolean = true,
    val todayCompletionPercent: Int = 0,
    val todayIsPerfect: Boolean = false,
    val hasShown100PercentPopupToday: Boolean = false,
    val studyWeight: Int = 40,
    val routineWeight: Int = 25,
    val workoutWeight: Int = 25,
    val taskWeight: Int = 10
)

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val dailyTargetMinutes: Int = 60, // Adjustable by user
    val completedMinutesToday: Int = 0,
    val totalAccumulatedMinutes: Int = 0,
    val sessionsCount: Int = 0,
    val isPending: Boolean = false,
    val pendingChapters: Int = 0,
    val notes: String = ""
)

@Entity(tableName = "routine_items")
data class RoutineItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "Study", // Study, Health, Personal, Meal, Sleep, Other
    val startTime: String = "08:00 AM",
    val durationMinutes: Int = 30,
    val isCompleted: Boolean = false,
    val isCritical: Boolean = false,
    val orderIndex: Int = 0,
    val dayKey: String = ""
)

@Entity(tableName = "workout_exercises")
data class WorkoutExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayKey: String = "",
    val workoutName: String = "Push & Core",
    val isRestDay: Boolean = false,
    val exerciseName: String,
    val targetSets: Int = 3,
    val completedSets: Int = 0,
    val targetReps: String = "10-12",
    val restSeconds: Int = 60,
    val isDone: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "arc_day_records")
data class ArcDayRecord(
    @PrimaryKey val dayNumber: Int, // 1 to 60
    val dateKey: String,
    val status: String = "Future", // Future, Current, Completed, Incomplete, Missed, Protected
    val completionPercent: Int = 0,
    val studyMinutes: Int = 0,
    val routineCompletedCount: Int = 0,
    val routineTotalCount: Int = 0,
    val workoutCompleted: Boolean = false,
    val xpEarned: Int = 0,
    val isPerfect: Boolean = false
)

@Entity(tableName = "study_sessions")
data class StudySessionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val subjectName: String,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val dateKey: String = "",
    val isManual: Boolean = false
)

@Entity(tableName = "achievements")
data class AchievementItem(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean = false,
    val unlockedDate: String? = null,
    val progress: Float = 0f
)

@Entity(tableName = "recovery_tasks")
data class RecoveryTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val reason: String,
    val assignedMinutes: Int = 20,
    val isCompleted: Boolean = false,
    val dayKey: String = ""
)
