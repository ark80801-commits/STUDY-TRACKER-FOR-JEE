package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_settings")
data class ExamSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val examTitle: String = "Final Exams",
    val targetDateEpochMillis: Long,
    val notes: String = ""
)
