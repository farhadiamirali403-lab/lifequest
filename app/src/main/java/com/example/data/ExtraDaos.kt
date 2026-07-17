package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY id DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY id DESC")
    fun getAllGoals(): Flow<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Query("DELETE FROM goals")
    suspend fun deleteAllGoals()
}

@Dao
interface ActiveBossDao {
    @Query("SELECT * FROM active_boss WHERE active = 1 LIMIT 1")
    fun getActiveBoss(): Flow<ActiveBoss?>

    @Query("SELECT * FROM active_boss WHERE active = 1 LIMIT 1")
    suspend fun getActiveBossSync(): ActiveBoss?

    @Query("SELECT * FROM active_boss ORDER BY id DESC")
    fun getAllBosses(): Flow<List<ActiveBoss>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoss(boss: ActiveBoss)

    @Update
    suspend fun updateBoss(boss: ActiveBoss)
}

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges ORDER BY id DESC")
    fun getAllChallenges(): Flow<List<Challenge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: Challenge)

    @Update
    suspend fun updateChallenge(challenge: Challenge)

    @Query("DELETE FROM challenges")
    suspend fun deleteAllChallenges()
}
