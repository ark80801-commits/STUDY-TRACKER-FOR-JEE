package com.example.data

import com.example.data.dao.ExamSettingsDao
import com.example.data.dao.SubjectDao
import com.example.data.dao.TopicDao
import com.example.data.entity.ExamSettingsEntity
import com.example.data.entity.SubjectEntity
import com.example.data.entity.TopicEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class SubjectWithProgress(
    val subject: SubjectEntity,
    val topics: List<TopicEntity>
) {
    val totalTopics: Int get() = topics.size
    val fdCount: Int get() = topics.count { it.status == TopicEntity.STATUS_FD }
    val pdCount: Int get() = topics.count { it.status == TopicEntity.STATUS_PD }
    val noneCount: Int get() = topics.count { it.status == TopicEntity.STATUS_NONE }

    val progress: Float
        get() {
            if (totalTopics == 0) return 0f
            val score = (fdCount * 1.0f) + (pdCount * 0.5f)
            return (score / totalTopics).coerceIn(0f, 1f)
        }

    val progressPercent: Int
        get() = (progress * 100).toInt()
}

data class OverallExamProgress(
    val examTitle: String = "Final Exams",
    val targetDateEpochMillis: Long = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(21),
    val totalSubjects: Int = 0,
    val totalTopics: Int = 0,
    val totalFd: Int = 0,
    val totalPd: Int = 0,
    val totalNone: Int = 0,
    val overallProgress: Float = 0f,
    val overallPercent: Int = 0,
    val daysLeft: Long = 21,
    val isExamToday: Boolean = false,
    val isExamPassed: Boolean = false
)

class ExamRepository(
    private val subjectDao: SubjectDao,
    private val topicDao: TopicDao,
    private val examSettingsDao: ExamSettingsDao,
    private val reminderTimeDao: com.example.data.dao.ReminderTimeDao
) {

    val allReminderTimes: Flow<List<com.example.data.entity.ReminderTimeEntity>> =
        reminderTimeDao.getAllReminderTimes()

    val allSubjectsWithProgress: Flow<List<SubjectWithProgress>> = combine(
        subjectDao.getAllSubjects(),
        topicDao.getAllTopics()
    ) { subjects, allTopics ->
        val topicsBySubject = allTopics.groupBy { it.subjectId }
        subjects.map { subject ->
            val topics = topicsBySubject[subject.id] ?: emptyList()
            SubjectWithProgress(subject = subject, topics = topics)
        }
    }

    val examSettings: Flow<ExamSettingsEntity?> = examSettingsDao.getSettings()

    val overallExamProgress: Flow<OverallExamProgress> = combine(
        examSettings,
        allSubjectsWithProgress
    ) { settings, subjectsWithProgress ->
        val now = System.currentTimeMillis()
        val defaultTargetDate = now + TimeUnit.DAYS.toMillis(21)
        val targetDate = settings?.targetDateEpochMillis ?: defaultTargetDate
        val title = settings?.examTitle ?: "JEE Main & Advanced"

        val diffMillis = targetDate - now
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
        val isToday = days == 0L && diffMillis >= 0
        val isPassed = diffMillis < 0 && days < 0

        val totalSubs = subjectsWithProgress.size
        var totalTopicsCount = 0
        var totalFdCount = 0
        var totalPdCount = 0

        for (item in subjectsWithProgress) {
            totalTopicsCount += item.totalTopics
            totalFdCount += item.fdCount
            totalPdCount += item.pdCount
        }

        val totalNoneCount = totalTopicsCount - totalFdCount - totalPdCount
        val progress = if (totalTopicsCount == 0) 0f else {
            ((totalFdCount * 1.0f + totalPdCount * 0.5f) / totalTopicsCount).coerceIn(0f, 1f)
        }

        OverallExamProgress(
            examTitle = title,
            targetDateEpochMillis = targetDate,
            totalSubjects = totalSubs,
            totalTopics = totalTopicsCount,
            totalFd = totalFdCount,
            totalPd = totalPdCount,
            totalNone = totalNoneCount,
            overallProgress = progress,
            overallPercent = (progress * 100).toInt(),
            daysLeft = if (diffMillis < 0) -TimeUnit.MILLISECONDS.toDays(-diffMillis) else days,
            isExamToday = isToday,
            isExamPassed = isPassed
        )
    }

    fun getSubjectWithTopics(subjectId: Long): Flow<SubjectWithProgress?> {
        return combine(
            subjectDao.getSubjectById(subjectId),
            topicDao.getTopicsForSubject(subjectId)
        ) { subject, topics ->
            if (subject == null) null
            else SubjectWithProgress(subject = subject, topics = topics)
        }
    }

    suspend fun toggleTopicStatus(topicId: Long, currentStatus: String, targetStatus: String) {
        val newStatus = if (currentStatus == targetStatus) {
            TopicEntity.STATUS_NONE
        } else {
            targetStatus
        }
        topicDao.updateTopicStatus(topicId, newStatus)
    }

    suspend fun addSubject(name: String, colorHex: String, targetDateMillis: Long?): Long {
        return subjectDao.insertSubject(
            SubjectEntity(
                name = name.trim(),
                colorHex = colorHex,
                targetDateEpochMillis = targetDateMillis
            )
        )
    }

    suspend fun updateSubject(subject: SubjectEntity) {
        subjectDao.updateSubject(subject)
    }

    suspend fun deleteSubject(subjectId: Long) {
        topicDao.deleteTopicsForSubject(subjectId)
        subjectDao.deleteSubjectById(subjectId)
    }

    suspend fun addTopic(subjectId: Long, title: String, notes: String = "", initialStatus: String = TopicEntity.STATUS_NONE): Long {
        return topicDao.insertTopic(
            TopicEntity(
                subjectId = subjectId,
                title = title.trim(),
                notes = notes.trim(),
                status = initialStatus
            )
        )
    }

    suspend fun updateTopic(topic: TopicEntity) {
        topicDao.updateTopic(topic)
    }

    suspend fun deleteTopic(topicId: Long) {
        topicDao.deleteTopicById(topicId)
    }

    suspend fun updateExamSettings(title: String, targetDateEpochMillis: Long, notes: String = "") {
        examSettingsDao.insertOrUpdate(
            ExamSettingsEntity(
                id = 1,
                examTitle = title.trim(),
                targetDateEpochMillis = targetDateEpochMillis,
                notes = notes.trim()
            )
        )
    }

    suspend fun addReminderTime(hour: Int, minute: Int, label: String = ""): Long {
        return reminderTimeDao.insertReminderTime(
            com.example.data.entity.ReminderTimeEntity(hour = hour, minute = minute, isEnabled = true, label = label.trim())
        )
    }

    suspend fun updateReminderTime(reminder: com.example.data.entity.ReminderTimeEntity) {
        reminderTimeDao.updateReminderTime(reminder)
    }

    suspend fun toggleReminderTime(id: Long, isEnabled: Boolean) {
        reminderTimeDao.toggleReminder(id, isEnabled)
    }

    suspend fun deleteReminderTime(id: Long) {
        reminderTimeDao.deleteReminderTimeById(id)
    }

    suspend fun seedInitialDataIfEmpty() {
        if (subjectDao.getSubjectCount() == 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, 90)
            val defaultExamDate = cal.timeInMillis

            examSettingsDao.insertOrUpdate(
                ExamSettingsEntity(
                    id = 1,
                    examTitle = "JEE Main & Advanced",
                    targetDateEpochMillis = defaultExamDate,
                    notes = "Target IIT / NIT Entrance Roadmap"
                )
            )

            // Seed JEE Core Trio: Physics, Chemistry, Mathematics
            val physicsId = subjectDao.insertSubject(
                SubjectEntity(
                    name = "Physics",
                    colorHex = "#0284C7", // Sky Blue
                    targetDateEpochMillis = defaultExamDate
                )
            )
            topicDao.insertTopics(
                listOf(
                    TopicEntity(subjectId = physicsId, title = "Newton's Laws & Mechanics", status = TopicEntity.STATUS_FD, orderIndex = 0),
                    TopicEntity(subjectId = physicsId, title = "Electrostatics & Current Electricity", status = TopicEntity.STATUS_PD, orderIndex = 1),
                    TopicEntity(subjectId = physicsId, title = "Optics & Wave Motion", status = TopicEntity.STATUS_NONE, orderIndex = 2),
                    TopicEntity(subjectId = physicsId, title = "Thermodynamics & Kinetic Theory", status = TopicEntity.STATUS_NONE, orderIndex = 3),
                    TopicEntity(subjectId = physicsId, title = "Modern Physics & Nuclear Physics", status = TopicEntity.STATUS_NONE, orderIndex = 4)
                )
            )

            val chemId = subjectDao.insertSubject(
                SubjectEntity(
                    name = "Chemistry",
                    colorHex = "#0D9488", // Teal
                    targetDateEpochMillis = defaultExamDate
                )
            )
            topicDao.insertTopics(
                listOf(
                    TopicEntity(subjectId = chemId, title = "Atomic Structure & Chemical Bonding", status = TopicEntity.STATUS_FD, orderIndex = 0),
                    TopicEntity(subjectId = chemId, title = "Organic Chemistry: Hydrocarbons & GOC", status = TopicEntity.STATUS_PD, orderIndex = 1),
                    TopicEntity(subjectId = chemId, title = "Coordination Compounds", status = TopicEntity.STATUS_NONE, orderIndex = 2),
                    TopicEntity(subjectId = chemId, title = "Thermodynamics & Equilibrium", status = TopicEntity.STATUS_NONE, orderIndex = 3)
                )
            )

            val mathId = subjectDao.insertSubject(
                SubjectEntity(
                    name = "Mathematics",
                    colorHex = "#4F46E5", // Indigo
                    targetDateEpochMillis = defaultExamDate
                )
            )
            topicDao.insertTopics(
                listOf(
                    TopicEntity(subjectId = mathId, title = "Differential & Integral Calculus", status = TopicEntity.STATUS_FD, orderIndex = 0),
                    TopicEntity(subjectId = mathId, title = "Coordinate Geometry: Conic Sections", status = TopicEntity.STATUS_PD, orderIndex = 1),
                    TopicEntity(subjectId = mathId, title = "Matrices, Determinants & Vectors", status = TopicEntity.STATUS_NONE, orderIndex = 2),
                    TopicEntity(subjectId = mathId, title = "Probability & Permutations", status = TopicEntity.STATUS_NONE, orderIndex = 3)
                )
            )
        }
    }
}
