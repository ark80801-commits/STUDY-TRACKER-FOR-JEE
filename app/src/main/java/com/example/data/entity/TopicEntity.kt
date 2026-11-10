package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "topics",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subjectId"])]
)
data class TopicEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long,
    val title: String,
    val notes: String = "",
    val status: String = STATUS_NONE, // STATUS_NONE, STATUS_PD, STATUS_FD
    val orderIndex: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_NONE = "NONE"
        const val STATUS_PD = "PD"
        const val STATUS_FD = "FD"
    }

    val isPd: Boolean get() = status == STATUS_PD
    val isFd: Boolean get() = status == STATUS_FD
}
