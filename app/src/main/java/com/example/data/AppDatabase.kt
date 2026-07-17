package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Quest::class, PlayerProfile::class, Habit::class, Goal::class, ActiveBoss::class, Challenge::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questDao(): QuestDao
    abstract fun playerProfileDao(): PlayerProfileDao
    abstract fun habitDao(): HabitDao
    abstract fun goalDao(): GoalDao
    abstract fun activeBossDao(): ActiveBossDao
    abstract fun challengeDao(): ChallengeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to player_profile with default values
                db.execSQL("ALTER TABLE player_profile ADD COLUMN isDarkMode INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN talentPoints INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN unlockedSkills TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN avatarUrl TEXT NOT NULL DEFAULT 'ic_rpg_hero'")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN companionName TEXT NOT NULL DEFAULT 'Baby Phoenix'")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN companionLevel INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN companionXp INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN guildName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN guildLevel INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN guildContribution INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN backupHash TEXT NOT NULL DEFAULT ''")

                // Create brand new tables
                db.execSQL("CREATE TABLE IF NOT EXISTS habits (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, category TEXT NOT NULL, positiveCount INTEGER NOT NULL DEFAULT 0, negativeCount INTEGER NOT NULL DEFAULT 0, lastTickedTime INTEGER NOT NULL DEFAULT 0, difficulty TEXT NOT NULL DEFAULT 'Medium')")
                db.execSQL("CREATE TABLE IF NOT EXISTS goals (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, targetValue INTEGER NOT NULL DEFAULT 100, currentValue INTEGER NOT NULL DEFAULT 0, deadline INTEGER NOT NULL DEFAULT 0, category TEXT NOT NULL, completed INTEGER NOT NULL DEFAULT 0)")
                db.execSQL("CREATE TABLE IF NOT EXISTS active_boss (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, maxHp INTEGER NOT NULL DEFAULT 100, currentHp INTEGER NOT NULL DEFAULT 100, level INTEGER NOT NULL DEFAULT 1, rewardGold INTEGER NOT NULL DEFAULT 100, rewardXp INTEGER NOT NULL DEFAULT 100, active INTEGER NOT NULL DEFAULT 1)")
                db.execSQL("CREATE TABLE IF NOT EXISTS challenges (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, requirement TEXT NOT NULL, progress INTEGER NOT NULL DEFAULT 0, targetValue INTEGER NOT NULL DEFAULT 10, rewardGold INTEGER NOT NULL DEFAULT 100, rewardXp INTEGER NOT NULL DEFAULT 100, type TEXT NOT NULL DEFAULT 'Weekly', completed INTEGER NOT NULL DEFAULT 0, expiryTime INTEGER NOT NULL DEFAULT 0)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lifequest_database"
                )
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
