package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val requirement: String,
    val progress: Int = 0,
    val targetValue: Int = 10,
    val rewardGold: Int = 100,
    val rewardXp: Int = 100,
    val type: String, // "Weekly", "Monthly", "Seasonal", "Guild"
    val completed: Boolean = false,
    val expiryTime: Long = 0L
)
