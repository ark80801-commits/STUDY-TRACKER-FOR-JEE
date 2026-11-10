package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ExamRepository
import com.example.data.OverallExamProgress
import com.example.data.SubjectWithProgress
import com.example.data.entity.SubjectEntity
import com.example.data.entity.TopicEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TopicFilter(val displayName: String) {
    ALL("All"),
    FD("FD (Done)"),
    PD("PD (Partial)"),
    NOT_STARTED("Remaining")
}

class ExamViewModel(private val repository: ExamRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val overallProgress: StateFlow<OverallExamProgress> = repository.overallExamProgress
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OverallExamProgress()
        )

    val subjects: StateFlow<List<SubjectWithProgress>> = repository.allSubjectsWithProgress
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val reminderTimes: StateFlow<List<com.example.data.entity.ReminderTimeEntity>> = repository.allReminderTimes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedSubjectId = MutableStateFlow<Long?>(null)
    val selectedSubjectId: StateFlow<Long?> = _selectedSubjectId.asStateFlow()

    private val _topicFilter = MutableStateFlow(TopicFilter.ALL)
    val topicFilter: StateFlow<TopicFilter> = _topicFilter.asStateFlow()

    private val _topicSearchQuery = MutableStateFlow("")
    val topicSearchQuery: StateFlow<String> = _topicSearchQuery.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedSubjectWithTopics: StateFlow<SubjectWithProgress?> = _selectedSubjectId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getSubjectWithTopics(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val filteredTopics: StateFlow<List<TopicEntity>> = combine(
        selectedSubjectWithTopics,
        _topicFilter,
        _topicSearchQuery
    ) { subjectData, filter, query ->
        if (subjectData == null) return@combine emptyList<TopicEntity>()
        var list = subjectData.topics
        if (query.isNotBlank()) {
            list = list.filter { it.title.contains(query, ignoreCase = true) || it.notes.contains(query, ignoreCase = true) }
        }
        when (filter) {
            TopicFilter.ALL -> list
            TopicFilter.FD -> list.filter { it.status == TopicEntity.STATUS_FD }
            TopicFilter.PD -> list.filter { it.status == TopicEntity.STATUS_PD }
            TopicFilter.NOT_STARTED -> list.filter { it.status == TopicEntity.STATUS_NONE }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectSubject(id: Long) {
        _selectedSubjectId.value = id
        _topicFilter.value = TopicFilter.ALL
        _topicSearchQuery.value = ""
    }

    fun clearSelectedSubject() {
        _selectedSubjectId.value = null
        _topicSearchQuery.value = ""
    }

    fun setTopicFilter(filter: TopicFilter) {
        _topicFilter.value = filter
    }

    fun setTopicSearchQuery(query: String) {
        _topicSearchQuery.value = query
    }

    fun toggleTopicStatus(topicId: Long, currentStatus: String, targetStatus: String) {
        viewModelScope.launch {
            repository.toggleTopicStatus(topicId, currentStatus, targetStatus)
        }
    }

    fun addSubject(name: String, colorHex: String, targetDateMillis: Long?) {
        viewModelScope.launch {
            repository.addSubject(name, colorHex, targetDateMillis)
        }
    }

    fun updateSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.updateSubject(subject)
        }
    }

    fun deleteSubject(subjectId: Long) {
        viewModelScope.launch {
            if (_selectedSubjectId.value == subjectId) {
                _selectedSubjectId.value = null
            }
            repository.deleteSubject(subjectId)
        }
    }

    fun addTopic(subjectId: Long, title: String, notes: String = "") {
        viewModelScope.launch {
            repository.addTopic(subjectId, title, notes)
        }
    }

    fun updateTopic(topic: TopicEntity) {
        viewModelScope.launch {
            repository.updateTopic(topic)
        }
    }

    fun deleteTopic(topicId: Long) {
        viewModelScope.launch {
            repository.deleteTopic(topicId)
        }
    }

    fun updateExamSettings(title: String, targetDateMillis: Long, notes: String = "") {
        viewModelScope.launch {
            repository.updateExamSettings(title, targetDateMillis, notes)
        }
    }

    fun addReminderTime(context: android.content.Context, hour: Int, minute: Int, label: String = "") {
        viewModelScope.launch {
            val newId = repository.addReminderTime(hour, minute, label)
            val newReminder = com.example.data.entity.ReminderTimeEntity(id = newId, hour = hour, minute = minute, isEnabled = true, label = label.trim())
            com.example.notification.ExamReminderManager.scheduleReminder(context, newReminder)
        }
    }

    fun updateReminderTime(context: android.content.Context, reminder: com.example.data.entity.ReminderTimeEntity) {
        viewModelScope.launch {
            repository.updateReminderTime(reminder)
            if (reminder.isEnabled) {
                com.example.notification.ExamReminderManager.scheduleReminder(context, reminder)
            } else {
                com.example.notification.ExamReminderManager.cancelReminder(context, reminder.id)
            }
        }
    }

    fun toggleReminderTime(context: android.content.Context, reminder: com.example.data.entity.ReminderTimeEntity, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleReminderTime(reminder.id, isEnabled)
            if (isEnabled) {
                com.example.notification.ExamReminderManager.scheduleReminder(context, reminder.copy(isEnabled = true))
            } else {
                com.example.notification.ExamReminderManager.cancelReminder(context, reminder.id)
            }
        }
    }

    fun deleteReminderTime(context: android.content.Context, reminderId: Long) {
        viewModelScope.launch {
            com.example.notification.ExamReminderManager.cancelReminder(context, reminderId)
            repository.deleteReminderTime(reminderId)
        }
    }
}

class ExamViewModelFactory(private val repository: ExamRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExamViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExamViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
