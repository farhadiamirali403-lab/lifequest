package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "Health", "Fitness", "Learning", "Work", "Personal Growth", "Custom"
    val positiveCount: Int = 0,
    val negativeCount: Int = 0,
    val lastTickedTime: Long = 0L,
    val difficulty: String = "Medium" // Easy, Medium, Hard, Epic
)
