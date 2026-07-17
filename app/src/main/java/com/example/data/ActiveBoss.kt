package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_boss")
data class ActiveBoss(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val maxHp: Int = 100,
    val currentHp: Int = 100,
    val level: Int = 1,
    val rewardGold: Int = 100,
    val rewardXp: Int = 100,
    val active: Boolean = true
)
