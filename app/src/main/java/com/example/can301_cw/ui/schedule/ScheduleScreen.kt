package com.example.can301_cw.ui.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.can301_cw.model.TaskStatus

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            // Optional: Add a TopBar if needed, but the request didn't explicitly ask for one.
            // Keeping it clean as per design description.
        }
    ) { padding ->
        if (uiState.groupedTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No schedules found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp) // Spacing handled by items
            ) {
                // Add a top spacer
                item { Spacer(modifier = Modifier.height(16.dp)) }

                uiState.groupedTasks.forEach { (dateString, tasks) ->
                    item {
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp, top = 16.dp)
                        )
                    }

                    itemsIndexed(
                        items = tasks,
                        key = { _, item -> item.task.id }
                    ) { index, taskWrapper ->
                        // Calculate shape based on position
                        val shape = when {
                            tasks.size == 1 -> RoundedCornerShape(24.dp) // Only one item
                            index == 0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp) // First
                            index == tasks.size - 1 -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp) // Last
                            else -> RoundedCornerShape(4.dp) // Middle
                        }

                        // Calculate bottom margin (small gap between merged cards)
                        val bottomPadding = if (index == tasks.size - 1) 0.dp else 2.dp

                        ScheduleCard(
                            taskWrapper = taskWrapper,
                            shape = shape,
                            onToggleStatus = { viewModel.toggleTaskStatus(taskWrapper) },
                            onSetStatus = { status -> viewModel.setTaskStatus(taskWrapper, status) },
                            modifier = Modifier.padding(bottom = bottomPadding)
                        )
                    }
                }
                
                // Bottom spacer for navigation bar
                item { Spacer(modifier = Modifier.height(88.dp)) }
            }
        }
    }
}

@Composable
fun ScheduleCard(
    taskWrapper: TaskWithMemoId,
    shape: RoundedCornerShape,
    onToggleStatus: () -> Unit,
    onSetStatus: (TaskStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val task = taskWrapper.task
    val isCompleted = task.taskStatus == TaskStatus.COMPLETED
    val isIgnored = task.taskStatus == TaskStatus.IGNORED

    if (isIgnored) return // Don't render ignored tasks? Or render them differently? Requirement said "Ignore" button exists, implying they start not ignored.

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Custom Circle Checkbox
                CircularCheckbox(
                    checked = isCompleted,
                    onCheckedChange = { onToggleStatus() }
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.theme.ifBlank { "Untitled Task" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                        color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (task.startTime.isNotBlank()) {
                         Text(
                            text = task.startTime, // TODO: Format this nicely if possible
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Expand indicator
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Collapsible Content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp, start = 40.dp)) { // Indent to align with text
                    
                    if (task.coreTasks.isNotEmpty()) {
                        Text(
                            text = "Core Tasks:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        task.coreTasks.forEach { item ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp).padding(top = 2.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (task.suggestedActions.isNotEmpty()) {
                        Text(
                            text = "Suggested Actions:",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFECA646), // Yellow/Orange ish
                             fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        task.suggestedActions.forEach { item ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(
                                    imageVector = Icons.Outlined.Circle, // Using Circle as bullet point
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp).padding(top = 6.dp),
                                    tint = Color(0xFFECA646)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyMedium,
                                     color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Action Buttons
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { /* TODO: Implement Reminder Logic */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFC107), // Yellowish
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(Icons.Filled.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Set Reminder", style = MaterialTheme.typography.labelLarge)
                        }

                        Button(
                            onClick = { onSetStatus(TaskStatus.IGNORED) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                             modifier = Modifier.height(40.dp)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ignore", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CircularCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    // Blue circle when unchecked (outline), Filled blue when checked
    val primaryColor = Color(0xFF2196F3) // Material Blue or use Theme primary
    
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .border(2.dp, primaryColor, CircleShape)
            .background(if (checked) primaryColor else Color.Transparent)
            .clickable { onCheckedChange() },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = checked,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

