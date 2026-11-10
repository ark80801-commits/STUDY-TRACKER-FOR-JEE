package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.TopicEntity
import com.example.ui.theme.StatusFdColor
import com.example.ui.theme.StatusFdContainer
import com.example.ui.theme.StatusFdContent
import com.example.ui.theme.StatusPdColor
import com.example.ui.theme.StatusPdContainer
import com.example.ui.theme.StatusPdContent

@Composable
fun TopicItemCard(
    topic: TopicEntity,
    onToggleStatus: (targetStatus: String) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val isFd = topic.status == TopicEntity.STATUS_FD
    val isPd = topic.status == TopicEntity.STATUS_PD

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("topic_item_${topic.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Topic title & optional notes
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Status indicator dot
                        val dotColor = when {
                            isFd -> StatusFdColor
                            isPd -> StatusPdColor
                            else -> MaterialTheme.colorScheme.outline
                        }
                        Surface(
                            shape = CircleShape,
                            color = dotColor,
                            modifier = Modifier.size(8.dp)
                        ) {}

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = topic.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (isFd) TextDecoration.LineThrough else TextDecoration.None
                        )
                    }

                    if (topic.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = topic.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }

                // More options menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Topic options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Topic") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Topic", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mutually exclusive PD and FD selection options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // PD (Partially Done) Button Option
                StatusOptionButton(
                    label = "PD",
                    subLabel = "Partially Done",
                    isSelected = isPd,
                    activeColor = StatusPdColor,
                    activeContainerColor = StatusPdContainer,
                    activeTextColor = StatusPdContent,
                    onClick = { onToggleStatus(TopicEntity.STATUS_PD) },
                    testTag = "topic_${topic.id}_pd_button",
                    modifier = Modifier.weight(1f)
                )

                // FD (Fully Done) Button Option
                StatusOptionButton(
                    label = "FD",
                    subLabel = "Fully Done",
                    isSelected = isFd,
                    activeColor = StatusFdColor,
                    activeContainerColor = StatusFdContainer,
                    activeTextColor = StatusFdContent,
                    onClick = { onToggleStatus(TopicEntity.STATUS_FD) },
                    testTag = "topic_${topic.id}_fd_button",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatusOptionButton(
    label: String,
    subLabel: String,
    isSelected: Boolean,
    activeColor: Color,
    activeContainerColor: Color,
    activeTextColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val animatedContainerColor by animateColorAsState(
        targetValue = if (isSelected) activeContainerColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 200),
        label = "btn_bg_anim"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        animationSpec = tween(durationMillis = 200),
        label = "btn_border_anim"
    )

    Surface(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = animatedContainerColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, animatedBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Checkmark or pill circle
            Surface(
                shape = CircleShape,
                color = if (isSelected) activeColor else Color.Transparent,
                border = if (!isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline) else null,
                modifier = Modifier.size(18.dp)
            ) {
                if (isSelected) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) activeTextColor else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = if (isSelected) activeTextColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
