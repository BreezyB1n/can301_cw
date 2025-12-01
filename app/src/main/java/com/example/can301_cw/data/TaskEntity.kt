package com.example.can301_cw.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val aiTaskId: String? = null,
    val title: String,
    val summary: String? = null,
    val type: String? = null, // EVENT/TODO/BILL/OTHER
    val dueTime: String? = null,
    val notifyTime: String? = null,
    val confidence: Float? = null,
    val imageUri: String? = null,
    val rawJson: String? = null,
    val status: Int = 0, // 0=pending, 1=done, 2=expired
    val createdAt: String,
    val updatedAt: String
)

