package com.example.data

import kotlinx.coroutines.flow.Flow

class QuestRepository(
    private val questDao: QuestDao,
    private val playerProfileDao: PlayerProfileDao,
    val habitDao: HabitDao,
    val goalDao: GoalDao,
    val activeBossDao: ActiveBossDao,
    val challengeDao: ChallengeDao
) {
    val allQuests: Flow<List<Quest>> = questDao.getAllQuests()
    val playerProfile: Flow<PlayerProfile?> = playerProfileDao.getPlayerProfile()
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allGoals: Flow<List<Goal>> = goalDao.getAllGoals()
    val activeBoss: Flow<ActiveBoss?> = activeBossDao.getActiveBoss()
    val allBosses: Flow<List<ActiveBoss>> = activeBossDao.getAllBosses()
    val allChallenges: Flow<List<Challenge>> = challengeDao.getAllChallenges()

    suspend fun insertQuest(quest: Quest) {
        questDao.insertQuest(quest)
    }

    suspend fun updateQuest(quest: Quest) {
        questDao.updateQuest(quest)
    }

    suspend fun deleteQuest(quest: Quest) {
        questDao.deleteQuest(quest)
    }

    suspend fun insertOrUpdateProfile(profile: PlayerProfile) {
        playerProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun getPlayerProfileSync(): PlayerProfile? {
        return playerProfileDao.getPlayerProfileSync()
    }

    suspend fun insertHabit(habit: Habit) = habitDao.insertHabit(habit)
    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)
    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    suspend fun insertGoal(goal: Goal) = goalDao.insertGoal(goal)
    suspend fun updateGoal(goal: Goal) = goalDao.updateGoal(goal)
    suspend fun deleteGoal(goal: Goal) = goalDao.deleteGoal(goal)

    suspend fun insertBoss(boss: ActiveBoss) = activeBossDao.insertBoss(boss)
    suspend fun updateBoss(boss: ActiveBoss) = activeBossDao.updateBoss(boss)

    suspend fun insertChallenge(challenge: Challenge) = challengeDao.insertChallenge(challenge)
    suspend fun updateChallenge(challenge: Challenge) = challengeDao.updateChallenge(challenge)
}
