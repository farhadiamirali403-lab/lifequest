package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey val id: Int = 1,
    val heroName: String = "قهرمان",
    val level: Int = 1,
    val xp: Int = 0,
    val streak: Int = 0,
    val lastCompletionTime: Long = 0L,
    val strength: Int = 10,
    val discipline: Int = 10,
    val intelligence: Int = 10,
    val vitality: Int = 10,
    val gold: Int = 50, // Starting gold
    val characterClass: String = "Warrior", // Warrior, Mage, Rogue
    val weapon: String = "خنجر تازه‌کار",
    val armor: String = "جامه پارچه‌ای",
    val ring: String = "None",
    val pet: String = "None",
    val totalQuestsCompleted: Int = 0,
    val totalXpEarned: Int = 0,
    val longestStreak: Int = 0,
    val inventory: String = "خنجر تازه‌کار,جامه پارچه‌ای", // Comma separated list of owned items
    val unlockedAchievements: String = "", // Comma separated list of unlocked ids
    val lastLoginRewardTime: Long = 0L,
    val languageCode: String = "fa",
    val levelHistory: String = "Level 1: Genesis", // Comma-separated or semi-colon separated logs
    val isDarkMode: Boolean = true,
    val talentPoints: Int = 0,
    val unlockedSkills: String = "", // e.g., "double_dmg,xp_multiplier"
    val avatarUrl: String = "ic_rpg_hero", // Simple built-in selection index/id
    val companionName: String = "ققنوس کوچک",
    val companionLevel: Int = 1,
    val companionXp: Int = 0,
    val guildName: String = "",
    val guildLevel: Int = 1,
    val guildContribution: Int = 0,
    val backupHash: String = "",
    val gems: Int = 20, // Economy improvement: Gems premium currency
    val lastActiveTime: Long = 0L, // Returning bonuses tracking
    val dailyRewardsClaimProgress: Int = 0, // Days claimed up to 7
    val weeklyChestClaimed: Boolean = false, // Retention: weekly chests
    val monthlyChestClaimed: Boolean = false, // Retention: monthly chests
    val activeSpecialization: String = "None", // Advanced RPG character specialization path
    val itemCollectionBook: String = "خنجر تازه‌کار,جامه پارچه‌ای", // Discoveries tracking list
    val onboardingCompleted: Boolean = false, // User Experience tutorial/onboarding
    val xpHistoryLogs: String = "0,15,45,75,120,180,240,310", // Analytics progression trends graph
    val boosterMultiplierLeft: Int = 0, // Economy: XP/Gold Multipliers
    val dailyChallengeCompleted: Boolean = false, // Economy/Challenges tracker daily status
    val extraTutorialProgress: Int = 0 // Interactive step-by-step tutorial flag
)

