package com.example

import com.example.data.model.RecoveryTask
import com.example.data.model.RoutineItem
import com.example.data.model.Subject
import com.example.data.model.WorkoutExercise
import com.example.engine.CompletionEngine
import com.example.engine.XpEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testLevelCurve() {
        assertEquals(1, XpEngine.calculateLevel(0))
        assertEquals(1, XpEngine.calculateLevel(49))
        assertEquals(2, XpEngine.calculateLevel(50))
        assertEquals(3, XpEngine.calculateLevel(200))
        assertEquals(4, XpEngine.calculateLevel(450))

        assertEquals("Frostbite", XpEngine.getLevelTitle(1))
        assertEquals("Snowdrift", XpEngine.getLevelTitle(5))
        assertEquals("Iceforged", XpEngine.getLevelTitle(10))
        assertEquals("Glacier", XpEngine.getLevelTitle(15))
        assertEquals("Permafrost", XpEngine.getLevelTitle(20))
    }

    @Test
    fun testCompletionEngine_100Percent() {
        val subjects = listOf(
            Subject(name = "Math", colorHex = "#34D399", dailyTargetMinutes = 60, completedMinutesToday = 60)
        )
        val routine = listOf(
            RoutineItem(title = "Morning Formula Recall", durationMinutes = 30, isCompleted = true)
        )
        val workout = listOf(
            WorkoutExercise(exerciseName = "Pushups", targetSets = 3, completedSets = 3, isDone = true)
        )
        val tasks = listOf<RecoveryTask>()

        val result = CompletionEngine.compute(
            subjects = subjects,
            routineItems = routine,
            workoutExercises = workout,
            recoveryTasks = tasks
        )

        assertEquals(100, result.totalPercent)
        assertTrue(result.isPerfect)
    }

    @Test
    fun testCompletionEngine_AllRoutineItemsCompleted_Yields100Percent() {
        // When all tasks on user's routine list are checked
        val routine = listOf(
            RoutineItem(title = "Physics Review", durationMinutes = 45, isCompleted = true),
            RoutineItem(title = "Calculus Problem Set", durationMinutes = 60, isCompleted = true),
            RoutineItem(title = "Workout Session", durationMinutes = 45, isCompleted = true)
        )
        val workout = listOf(
            WorkoutExercise(exerciseName = "Pushups", targetSets = 3, completedSets = 3, isDone = true)
        )

        val result = CompletionEngine.compute(
            subjects = emptyList(),
            routineItems = routine,
            workoutExercises = workout,
            recoveryTasks = emptyList()
        )

        assertEquals(100, result.totalPercent)
        assertTrue(result.isPerfect)
    }

    @Test
    fun testCompletionEngine_RestDayRedistribution() {
        val subjects = listOf(
            Subject(name = "Math", colorHex = "#34D399", dailyTargetMinutes = 60, completedMinutesToday = 60)
        )
        val routine = listOf(
            RoutineItem(title = "Review", durationMinutes = 30, isCompleted = true)
        )
        val workout = listOf(
            WorkoutExercise(exerciseName = "Rest", targetSets = 0, isRestDay = true)
        )

        val result = CompletionEngine.compute(
            subjects = subjects,
            routineItems = routine,
            workoutExercises = workout,
            recoveryTasks = emptyList()
        )

        assertEquals(100, result.totalPercent)
        assertTrue(result.isPerfect)
    }
}
