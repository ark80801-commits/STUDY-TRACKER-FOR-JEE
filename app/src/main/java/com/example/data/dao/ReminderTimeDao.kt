package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.ReminderTimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderTimeDao {
    @Query("SELECT * FROM reminder_times ORDER BY hour ASC, minute ASC")
    fun getAllReminderTimes(): Flow<List<ReminderTimeEntity>>

    @Query("SELECT * FROM reminder_times WHERE isEnabled = 1 ORDER BY hour ASC, minute ASC")
    suspend fun getActiveReminderTimes(): List<ReminderTimeEntity>

    @Query("SELECT * FROM reminder_times WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: Long): ReminderTimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminderTime(reminder: ReminderTimeEntity): Long

    @Update
    suspend fun updateReminderTime(reminder: ReminderTimeEntity)

    @Query("UPDATE reminder_times SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun toggleReminder(id: Long, isEnabled: Boolean)

    @Delete
    suspend fun deleteReminderTime(reminder: ReminderTimeEntity)

    @Query("DELETE FROM reminder_times WHERE id = :id")
    suspend fun deleteReminderTimeById(id: Long)

    @Query("SELECT COUNT(*) FROM reminder_times")
    suspend fun getCount(): Int
}
