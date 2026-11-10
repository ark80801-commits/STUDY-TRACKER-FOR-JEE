package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ExamSettingsDao
import com.example.data.dao.ReminderTimeDao
import com.example.data.dao.SubjectDao
import com.example.data.dao.TopicDao
import com.example.data.entity.ExamSettingsEntity
import com.example.data.entity.ReminderTimeEntity
import com.example.data.entity.SubjectEntity
import com.example.data.entity.TopicEntity

@Database(
    entities = [
        SubjectEntity::class,
        TopicEntity::class,
        ExamSettingsEntity::class,
        ReminderTimeEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun topicDao(): TopicDao
    abstract fun examSettingsDao(): ExamSettingsDao
    abstract fun reminderTimeDao(): ReminderTimeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "exam_track_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
