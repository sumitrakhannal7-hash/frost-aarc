package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FrostDao {

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)

    // Subjects
    @Query("SELECT * FROM subjects ORDER BY id ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: Long): Subject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<Subject>)

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    @Query("DELETE FROM subjects")
    suspend fun deleteAllSubjects()

    // Routine Items
    @Query("SELECT * FROM routine_items ORDER BY orderIndex ASC, id ASC")
    fun getAllRoutineItems(): Flow<List<RoutineItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineItem(item: RoutineItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineItems(items: List<RoutineItem>)

    @Update
    suspend fun updateRoutineItem(item: RoutineItem)

    @Delete
    suspend fun deleteRoutineItem(item: RoutineItem)

    // Workout Exercises
    @Query("SELECT * FROM workout_exercises ORDER BY orderIndex ASC, id ASC")
    fun getAllWorkoutExercises(): Flow<List<WorkoutExercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercise(exercise: WorkoutExercise): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercises(exercises: List<WorkoutExercise>)

    @Update
    suspend fun updateWorkoutExercise(exercise: WorkoutExercise)

    @Delete
    suspend fun deleteWorkoutExercise(exercise: WorkoutExercise)

    // Arc Day Records (1..60)
    @Query("SELECT * FROM arc_day_records ORDER BY dayNumber ASC")
    fun getAllArcDays(): Flow<List<ArcDayRecord>>

    @Query("SELECT * FROM arc_day_records WHERE dayNumber = :dayNumber LIMIT 1")
    suspend fun getArcDay(dayNumber: Int): ArcDayRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArcDays(days: List<ArcDayRecord>)

    @Update
    suspend fun updateArcDay(day: ArcDayRecord)

    // Study Sessions
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<StudySessionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionLog): Long

    // Achievements
    @Query("SELECT * FROM achievements ORDER BY id ASC")
    fun getAllAchievements(): Flow<List<AchievementItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievements(list: List<AchievementItem>)

    @Update
    suspend fun updateAchievement(item: AchievementItem)

    // Recovery Tasks
    @Query("SELECT * FROM recovery_tasks ORDER BY id DESC")
    fun getAllRecoveryTasks(): Flow<List<RecoveryTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecoveryTask(task: RecoveryTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecoveryTasks(tasks: List<RecoveryTask>)

    @Update
    suspend fun updateRecoveryTask(task: RecoveryTask)
}
