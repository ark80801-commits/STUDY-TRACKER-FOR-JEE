package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.ExamRepository
import com.example.ui.ExamViewModel
import com.example.ui.ExamViewModelFactory
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SubjectDetailScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize daily notification channel and reschedule all active reminders
        com.example.notification.ExamReminderManager.createNotificationChannel(applicationContext)
        com.example.notification.ExamReminderManager.rescheduleAllActiveReminders(applicationContext)

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ExamRepository(
            subjectDao = database.subjectDao(),
            topicDao = database.topicDao(),
            examSettingsDao = database.examSettingsDao(),
            reminderTimeDao = database.reminderTimeDao()
        )
        val viewModelFactory = ExamViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ExamApp(viewModelFactory = viewModelFactory)
                }
            }
        }
    }
}

@Composable
fun ExamApp(
    viewModelFactory: ExamViewModelFactory,
    modifier: Modifier = Modifier
) {
    val viewModel: ExamViewModel = viewModel(factory = viewModelFactory)
    val selectedSubjectId by viewModel.selectedSubjectId.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = selectedSubjectId,
        transitionSpec = {
            if (targetState != null) {
                // Navigating to detail
                slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                        slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut()
            } else {
                // Navigating back to home
                slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn() togetherWith
                        slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            }
        },
        label = "screen_navigation_anim",
        modifier = modifier.fillMaxSize()
    ) { currentSubjectId ->
        if (currentSubjectId == null) {
            HomeScreen(
                viewModel = viewModel,
                onSubjectClick = { subjectId ->
                    viewModel.selectSubject(subjectId)
                }
            )
        } else {
            SubjectDetailScreen(
                viewModel = viewModel,
                onBackClick = {
                    viewModel.clearSelectedSubject()
                }
            )
        }
    }
}
