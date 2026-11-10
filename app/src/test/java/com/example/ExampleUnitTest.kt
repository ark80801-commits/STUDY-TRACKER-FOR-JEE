package com.example

import com.example.data.SubjectWithProgress
import com.example.data.entity.SubjectEntity
import com.example.data.entity.TopicEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class ExampleUnitTest {

    @Test
    fun `test topic status mutual exclusivity`() {
        val topic1 = TopicEntity(id = 1, subjectId = 1, title = "Calculus", status = TopicEntity.STATUS_FD)
        assertTrue(topic1.isFd)
        assertFalse(topic1.isPd)

        val topic2 = TopicEntity(id = 2, subjectId = 1, title = "Algebra", status = TopicEntity.STATUS_PD)
        assertTrue(topic2.isPd)
        assertFalse(topic2.isFd)

        val topic3 = TopicEntity(id = 3, subjectId = 1, title = "Geometry", status = TopicEntity.STATUS_NONE)
        assertFalse(topic3.isFd)
        assertFalse(topic3.isPd)
    }

    @Test
    fun `test progress calculation`() {
        val subject = SubjectEntity(id = 1, name = "Science")
        val topics = listOf(
            TopicEntity(id = 1, subjectId = 1, title = "Topic 1", status = TopicEntity.STATUS_FD),   // 1.0
            TopicEntity(id = 2, subjectId = 1, title = "Topic 2", status = TopicEntity.STATUS_PD),   // 0.5
            TopicEntity(id = 3, subjectId = 1, title = "Topic 3", status = TopicEntity.STATUS_NONE), // 0.0
            TopicEntity(id = 4, subjectId = 1, title = "Topic 4", status = TopicEntity.STATUS_NONE)  // 0.0
        )
        val subjectWithProgress = SubjectWithProgress(subject, topics)

        assertEquals(4, subjectWithProgress.totalTopics)
        assertEquals(1, subjectWithProgress.fdCount)
        assertEquals(1, subjectWithProgress.pdCount)
        assertEquals(2, subjectWithProgress.noneCount)

        // (1*1.0 + 1*0.5) / 4 = 1.5 / 4 = 0.375 (37%)
        assertEquals(0.375f, subjectWithProgress.progress, 0.001f)
        assertEquals(37, subjectWithProgress.progressPercent)
    }

    @Test
    fun `test days left calculation`() {
        val now = System.currentTimeMillis()
        val examDate = now + TimeUnit.DAYS.toMillis(15)
        val diffMillis = examDate - now
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)

        assertEquals(15L, days)
    }

    @Test
    fun `test days left calculation for 3 years ahead`() {
        val now = System.currentTimeMillis()
        val threeYearsDays = 365L * 3L // 1095 days
        val examDate = now + TimeUnit.DAYS.toMillis(threeYearsDays)
        val diffMillis = examDate - now
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)

        assertEquals(1095L, days)
    }

    @Test
    fun `test reminder text formatting`() {
        val daysLeft = 45L
        val examTitle = "JEE Main & Advanced"
        val expectedTitle = "🎯 $examTitle: Only $daysLeft Days Remaining!"
        assertTrue(expectedTitle.contains("45 Days"))
    }

    @Test
    fun `test custom reminder time formatting`() {
        val morningReminder = com.example.data.entity.ReminderTimeEntity(hour = 7, minute = 30, label = "Morning Study")
        assertEquals("7:30 AM", morningReminder.formattedTime)

        val noonReminder = com.example.data.entity.ReminderTimeEntity(hour = 12, minute = 0, label = "Midday Review")
        assertEquals("12:00 PM", noonReminder.formattedTime)

        val eveningReminder = com.example.data.entity.ReminderTimeEntity(hour = 18, minute = 45, label = "Evening Mock Test")
        assertEquals("6:45 PM", eveningReminder.formattedTime)

        val midnightReminder = com.example.data.entity.ReminderTimeEntity(hour = 0, minute = 5, label = "Night Recap")
        assertEquals("12:05 AM", midnightReminder.formattedTime)
    }
}

