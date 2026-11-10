package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.SubjectEntity
import com.example.data.entity.TopicEntity
import com.example.ui.ExamViewModel
import com.example.ui.TopicFilter
import com.example.ui.components.AddSubjectDialog
import com.example.ui.components.AddTopicDialog
import com.example.ui.components.TopicItemCard
import com.example.ui.theme.StatusFdColor
import com.example.ui.theme.StatusFdContainer
import com.example.ui.theme.StatusPdColor
import com.example.ui.theme.StatusPdContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    viewModel: ExamViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Handle hardware & gesture back navigation
    BackHandler {
        onBackClick()
    }

    val subjectData by viewModel.selectedSubjectWithTopics.collectAsStateWithLifecycle()
    val filteredTopics by viewModel.filteredTopics.collectAsStateWithLifecycle()
    val currentFilter by viewModel.topicFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.topicSearchQuery.collectAsStateWithLifecycle()

    var showAddTopicDialog by remember { mutableStateOf(false) }
    var editingTopic by remember { mutableStateOf<TopicEntity?>(null) }
    var editingSubject by remember { mutableStateOf<SubjectEntity?>(null) }
    var showTopMenu by remember { mutableStateOf(false) }

    if (subjectData == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Subject not found")
        }
        return
    }

    val currentSubject = subjectData!!.subject
    val subjectColor = try {
        Color(android.graphics.Color.parseColor(currentSubject.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val animatedProgress by animateFloatAsState(
        targetValue = subjectData!!.progress,
        animationSpec = tween(durationMillis = 600),
        label = "detail_progress_anim"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = subjectColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = subjectColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = currentSubject.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${subjectData!!.totalTopics} Topics • ${subjectData!!.progressPercent}% Completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to subjects"
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showTopMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Subject options")
                        }

                        DropdownMenu(
                            expanded = showTopMenu,
                            onDismissRequest = { showTopMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Subject Name/Color") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    showTopMenu = false
                                    editingSubject = currentSubject
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Subject", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showTopMenu = false
                                    viewModel.deleteSubject(currentSubject.id)
                                    onBackClick()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddTopicDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Topic") },
                containerColor = subjectColor,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_topic_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // PROGRESS BAR ON TOP (MANDATORY REQUIREMENT)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_progress_header_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Subject Completion",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "PD = 50% • FD = 100% credit",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = subjectColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${subjectData!!.progressPercent}%",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = subjectColor,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Large prominent Progress Bar on Top
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .testTag("subject_detail_top_progress_bar"),
                            color = subjectColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Metrics breakdown badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricBadge(
                                label = "Fully Done",
                                count = "${subjectData!!.fdCount}",
                                tag = "FD",
                                color = StatusFdColor,
                                containerColor = StatusFdContainer,
                                modifier = Modifier.weight(1f)
                            )
                            MetricBadge(
                                label = "Partial Done",
                                count = "${subjectData!!.pdCount}",
                                tag = "PD",
                                color = StatusPdColor,
                                containerColor = StatusPdContainer,
                                modifier = Modifier.weight(1f)
                            )
                            MetricBadge(
                                label = "Remaining",
                                count = "${subjectData!!.noneCount}",
                                tag = "None",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Search Bar & Filter Chips
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setTopicSearchQuery(it) },
                        placeholder = { Text("Search topics in ${currentSubject.name}...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setTopicSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("topic_search_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = currentFilter == TopicFilter.ALL,
                                onClick = { viewModel.setTopicFilter(TopicFilter.ALL) },
                                label = { Text("All (${subjectData!!.totalTopics})") },
                                modifier = Modifier.testTag("filter_all")
                            )
                        }
                        item {
                            FilterChip(
                                selected = currentFilter == TopicFilter.FD,
                                onClick = { viewModel.setTopicFilter(TopicFilter.FD) },
                                label = { Text("FD Done (${subjectData!!.fdCount})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StatusFdContainer,
                                    selectedLabelColor = StatusFdColor
                                ),
                                modifier = Modifier.testTag("filter_fd")
                            )
                        }
                        item {
                            FilterChip(
                                selected = currentFilter == TopicFilter.PD,
                                onClick = { viewModel.setTopicFilter(TopicFilter.PD) },
                                label = { Text("PD Partial (${subjectData!!.pdCount})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StatusPdContainer,
                                    selectedLabelColor = StatusPdColor
                                ),
                                modifier = Modifier.testTag("filter_pd")
                            )
                        }
                        item {
                            FilterChip(
                                selected = currentFilter == TopicFilter.NOT_STARTED,
                                onClick = { viewModel.setTopicFilter(TopicFilter.NOT_STARTED) },
                                label = { Text("Remaining (${subjectData!!.noneCount})") },
                                modifier = Modifier.testTag("filter_remaining")
                            )
                        }
                    }
                }
            }

            // Topics List
            if (filteredTopics.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (searchQuery.isNotEmpty()) "No topics match \"$searchQuery\""
                            else if (subjectData!!.totalTopics == 0) "No topics in ${currentSubject.name} yet"
                            else "No topics in this filter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Add chapters and topics to track PD and FD status",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showAddTopicDialog = true },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = subjectColor
                            ),
                            modifier = Modifier.testTag("empty_add_topic_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Topic")
                        }
                    }
                }
            } else {
                items(filteredTopics, key = { it.id }) { topic ->
                    TopicItemCard(
                        topic = topic,
                        onToggleStatus = { targetStatus ->
                            viewModel.toggleTopicStatus(
                                topicId = topic.id,
                                currentStatus = topic.status,
                                targetStatus = targetStatus
                            )
                        },
                        onEditClick = { editingTopic = topic },
                        onDeleteClick = { viewModel.deleteTopic(topic.id) }
                    )
                }
            }

            // Space at bottom for floating action button
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Add Topic Dialog
    if (showAddTopicDialog) {
        AddTopicDialog(
            onDismiss = { showAddTopicDialog = false },
            onConfirm = { title, notes, status ->
                viewModel.addTopic(currentSubject.id, title, notes)
                showAddTopicDialog = false
            }
        )
    }

    // Edit Topic Dialog
    if (editingTopic != null) {
        AddTopicDialog(
            initialTopic = editingTopic,
            onDismiss = { editingTopic = null },
            onConfirm = { title, notes, status ->
                editingTopic?.let { current ->
                    viewModel.updateTopic(current.copy(title = title, notes = notes, status = status))
                }
                editingTopic = null
            }
        )
    }

    // Edit Subject Dialog
    if (editingSubject != null) {
        AddSubjectDialog(
            initialSubject = editingSubject,
            onDismiss = { editingSubject = null },
            onConfirm = { name, colorHex ->
                editingSubject?.let { current ->
                    viewModel.updateSubject(current.copy(name = name, colorHex = colorHex))
                }
                editingSubject = null
            }
        )
    }
}

@Composable
private fun MetricBadge(
    label: String,
    count: String,
    tag: String,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = color,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Text(
                    text = count,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
