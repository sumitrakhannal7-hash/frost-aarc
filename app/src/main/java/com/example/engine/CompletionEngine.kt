package com.example.engine

import com.example.data.model.RecoveryTask
import com.example.data.model.RoutineItem
import com.example.data.model.Subject
import com.example.data.model.WorkoutExercise
import kotlin.math.min
import kotlin.math.roundToInt

data class CompletionResult(
    val totalPercent: Int,
    val studyPercent: Int,
    val routinePercent: Int,
    val workoutPercent: Int,
    val taskPercent: Int,
    val isPerfect: Boolean
)

object CompletionEngine {

    fun compute(
        subjects: List<Subject>,
        routineItems: List<RoutineItem>,
        workoutExercises: List<WorkoutExercise>,
        recoveryTasks: List<RecoveryTask>,
        baseStudyWeight: Int = 40,
        baseRoutineWeight: Int = 25,
        baseWorkoutWeight: Int = 25,
        baseTaskWeight: Int = 10
    ): CompletionResult {
        // 1. Routine to-do list completion
        val hasRoutine = routineItems.isNotEmpty()
        val routineDoneCount = routineItems.count { it.isCompleted }
        val allRoutineDone = hasRoutine && routineDoneCount == routineItems.size
        val routinePct = when {
            !hasRoutine -> 100
            allRoutineDone -> 100
            else -> ((routineDoneCount.toDouble() / routineItems.size.toDouble()) * 100).roundToInt().coerceIn(0, 100)
        }

        // 2. Workout completion
        val isRestDay = workoutExercises.any { it.isRestDay }
        val hasWorkout = workoutExercises.isNotEmpty() && !isRestDay
        val totalWorkoutSets = workoutExercises.sumOf { it.targetSets }
        val doneWorkoutSets = workoutExercises.sumOf { min(it.completedSets, it.targetSets) }
        val allWorkoutDone = isRestDay || (hasWorkout && workoutExercises.all { it.isDone || it.completedSets >= it.targetSets })
        val workoutPct = when {
            isRestDay -> 100
            !hasWorkout -> 100
            allWorkoutDone -> 100
            totalWorkoutSets > 0 -> ((doneWorkoutSets.toDouble() / totalWorkoutSets.toDouble()) * 100).roundToInt().coerceIn(0, 100)
            else -> 100
        }

        // 3. Study completion
        val totalStudyTarget = subjects.sumOf { it.dailyTargetMinutes }
        val totalStudyDone = subjects.sumOf { min(it.completedMinutesToday, it.dailyTargetMinutes) }
        val hasStudy = totalStudyTarget > 0
        val allSubjectsDone = hasStudy && subjects.all { it.completedMinutesToday >= it.dailyTargetMinutes }
        val studyPct = when {
            !hasStudy -> 100
            allSubjectsDone -> 100
            else -> ((totalStudyDone.toDouble() / totalStudyTarget.toDouble()) * 100).roundToInt().coerceIn(0, 100)
        }

        // 4. Recovery tasks (only count if there are pending recovery tasks)
        val pendingRecoveryTasks = recoveryTasks.filter { !it.isCompleted }
        val hasTasks = recoveryTasks.isNotEmpty()
        val allTasksDone = recoveryTasks.isEmpty() || pendingRecoveryTasks.isEmpty()
        val taskPct = if (allTasksDone) 100 else {
            val doneCount = recoveryTasks.count { it.isCompleted }
            ((doneCount.toDouble() / recoveryTasks.size.toDouble()) * 100).roundToInt().coerceIn(0, 100)
        }

        // CRITICAL: When the user has completed all tasks on their routine to-do list AND workout exercises
        // OR when all routine items are marked complete (the user's master daily checklist):
        if (allRoutineDone && (allWorkoutDone || !hasWorkout) && (allSubjectsDone || !hasStudy)) {
            return CompletionResult(
                totalPercent = 100,
                studyPercent = 100,
                routinePercent = 100,
                workoutPercent = 100,
                taskPercent = 100,
                isPerfect = true
            )
        }

        // If the user completed all routine to-do tasks and workout is completed / rest day:
        if (allRoutineDone && allWorkoutDone) {
            return CompletionResult(
                totalPercent = 100,
                studyPercent = 100,
                routinePercent = 100,
                workoutPercent = 100,
                taskPercent = 100,
                isPerfect = true
            )
        }

        // Normal weighted calculation
        var activeWeightSum = 0.0
        if (hasStudy) activeWeightSum += baseStudyWeight
        if (hasRoutine) activeWeightSum += baseRoutineWeight
        if (hasWorkout) activeWeightSum += baseWorkoutWeight
        if (hasTasks && pendingRecoveryTasks.isNotEmpty()) activeWeightSum += baseTaskWeight

        if (activeWeightSum <= 0.0) {
            return CompletionResult(100, 100, 100, 100, 100, true)
        }

        var weightedTotal = 0.0
        if (hasStudy) weightedTotal += (studyPct * (baseStudyWeight / activeWeightSum))
        if (hasRoutine) weightedTotal += (routinePct * (baseRoutineWeight / activeWeightSum))
        if (hasWorkout) weightedTotal += (workoutPct * (baseWorkoutWeight / activeWeightSum))
        if (hasTasks && pendingRecoveryTasks.isNotEmpty()) weightedTotal += (taskPct * (baseTaskWeight / activeWeightSum))

        val finalTotal = weightedTotal.roundToInt().coerceIn(0, 100)
        val isPerfect = finalTotal == 100

        return CompletionResult(
            totalPercent = finalTotal,
            studyPercent = studyPct,
            routinePercent = routinePct,
            workoutPercent = workoutPct,
            taskPercent = taskPct,
            isPerfect = isPerfect
        )
    }
}
