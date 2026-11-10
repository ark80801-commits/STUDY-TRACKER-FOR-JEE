package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#4F46E5",
    val targetDateEpochMillis: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
