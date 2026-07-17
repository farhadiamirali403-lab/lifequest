package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetValue: Int = 100,
    val currentValue: Int = 0,
    val deadline: Long = 0L,
    val category: String = "Personal Growth",
    val completed: Boolean = false
)
