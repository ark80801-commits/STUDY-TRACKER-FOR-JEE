package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.ExamSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamSettingsDao {
    @Query("SELECT * FROM exam_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<ExamSettingsEntity?>

    @Query("SELECT * FROM exam_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsImmediate(): ExamSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: ExamSettingsEntity)
}
