package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.entity.TopicEntity
import com.example.ui.theme.StatusFdColor
import com.example.ui.theme.StatusPdColor

@Composable
fun AddTopicDialog(
    initialTopic: TopicEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, notes: String, status: String) -> Unit
) {
    var title by remember { mutableStateOf(initialTopic?.title ?: "") }
    var notes by remember { mutableStateOf(initialTopic?.notes ?: "") }
    var status by remember { mutableStateOf(initialTopic?.status ?: TopicEntity.STATUS_NONE) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialTopic == null) "Add New Topic" else "Edit Topic",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text("Topic Name") },
                    placeholder = { Text("e.g. Chapter 4: Integration by Parts") },
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Topic title cannot be empty") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("topic_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Formula reference (optional)") },
                    placeholder = { Text("e.g. Page 120-135, Practice set 3") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("topic_notes_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Initial Status",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = status == TopicEntity.STATUS_NONE,
                        onClick = { status = TopicEntity.STATUS_NONE },
                        label = { Text("Not Started") },
                        modifier = Modifier.testTag("chip_status_none")
                    )

                    FilterChip(
                        selected = status == TopicEntity.STATUS_PD,
                        onClick = { status = TopicEntity.STATUS_PD },
                        label = { Text("PD (Partial)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusPdColor.copy(alpha = 0.2f),
                            selectedLabelColor = StatusPdColor
                        ),
                        modifier = Modifier.testTag("chip_status_pd")
                    )

                    FilterChip(
                        selected = status == TopicEntity.STATUS_FD,
                        onClick = { status = TopicEntity.STATUS_FD },
                        label = { Text("FD (Done)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusFdColor.copy(alpha = 0.2f),
                            selectedLabelColor = StatusFdColor
                        ),
                        modifier = Modifier.testTag("chip_status_fd")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        isError = true
                    } else {
                        onConfirm(title.trim(), notes.trim(), status)
                    }
                },
                modifier = Modifier.testTag("save_topic_button")
            ) {
                Text(if (initialTopic == null) "Add Topic" else "Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_topic_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
