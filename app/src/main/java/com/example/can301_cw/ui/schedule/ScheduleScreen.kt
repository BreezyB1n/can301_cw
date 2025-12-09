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
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Notifications
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

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.example.can301_cw.model.TaskStatus

import androidx.compose.ui.draw.scale
import androidx.compose.foundation.ExperimentalFoundationApi

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.can301_cw.ui.components.ReminderDialog

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    onMemoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val showAllTasks by viewModel.showAllTasks.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Intents",
                        fontWeight = FontWeight.Bold
                    )
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 16.dp)
        ) {
            // Checkbox Row (Always visible at top)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = showAllTasks,
                    onCheckedChange = { viewModel.setShowAllTasks(it) },
                    modifier = Modifier
                        .scale(0.8f)
                        .size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Show completed/ignored Intents",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.groupedTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Event,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No intents found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
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
                                index == 0 -> RoundedCornerShape(
                                    topStart = 24.dp,
                                    topEnd = 24.dp,
                                    bottomStart = 4.dp,
                                    bottomEnd = 4.dp
                                ) // First
                                index == tasks.size - 1 -> RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 4.dp,
                                    bottomStart = 24.dp,
                                    bottomEnd = 24.dp
                                ) // Last
                                else -> RoundedCornerShape(4.dp) // Middle
                            }

                            // Calculate bottom margin (small gap between merged cards)
                            val bottomPadding = if (index == tasks.size - 1) 0.dp else 2.dp

                            ScheduleCard(
                                taskWrapper = taskWrapper,
                                shape = shape,
                                onToggleStatus = { viewModel.toggleTaskStatus(taskWrapper) },
                                onSetStatus = { status -> viewModel.setTaskStatus(taskWrapper, status) },
                                onSetReminder = { timestamp -> viewModel.setTaskReminder(taskWrapper, timestamp) },
                                onMemoClick = onMemoClick,
                                showAllTasks = showAllTasks,
                                modifier = Modifier.padding(bottom = bottomPadding).animateItem()
                            )
                        }
                    }

                    // Bottom spacer for navigation bar
                    item { Spacer(modifier = Modifier.height(88.dp)) }
                }
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
    onSetReminder: (Long) -> Unit,
    onMemoClick: (String) -> Unit,
    showAllTasks: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val task = taskWrapper.task
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // UI states for delayed interaction
    var isVisible by remember { mutableStateOf(true) }
    var isLocallyCompleted by remember(task.taskStatus) { mutableStateOf(task.taskStatus == TaskStatus.COMPLETED) }
    var isLocallyIgnored by remember(task.taskStatus) { mutableStateOf(task.taskStatus == TaskStatus.IGNORED) }
    
    var showReminderDialog by remember { mutableStateOf(false) }

    if (showReminderDialog) {
        ReminderDialog(
            onDismissRequest = { showReminderDialog = false },
            onConfirm = { timestamp ->
                onSetReminder(timestamp)
                showReminderDialog = false
            }
        )
    }

    // If it's already ignored/completed in DB, we don't show it (handled by parent list),
    // but if it becomes ignored/completed locally, we handle animation.

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .clickable { onMemoClick(taskWrapper.memoId) }
                    .padding(16.dp)
            ) {
                // Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Custom Circle Checkbox
                    CircularCheckbox(
                        checked = isLocallyCompleted,
                        onCheckedChange = { 
                            isLocallyCompleted = !isLocallyCompleted
                            if (isLocallyCompleted) {
                                if (!showAllTasks) {
                                    coroutineScope.launch {
                                        delay(2000) // 2s delay
                                        isVisible = false
                                        delay(500) // Wait for exit animation
                                        onToggleStatus() 
                                    }
                                } else {
                                    onToggleStatus()
                                }
                            } else {
                                onToggleStatus()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.theme.ifBlank { "Untitled Task" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (isLocallyCompleted || isLocallyIgnored) TextDecoration.LineThrough else null,
                            color = if (isLocallyCompleted || isLocallyIgnored) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                        )
                        
                        val timePart = extractTime(task.startTime)
                        val shouldShowTime = shouldShowTime(task.startTime, timePart)
                        
                        Column {
                            if (shouldShowTime) {
                                Text(
                                    text = timePart,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            if (task.reminderTime != null && task.reminderTime!! > System.currentTimeMillis()) {
                                val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                                val prefix = if (shouldShowTime) "· " else ""
                                Text(
                                    text = "${prefix}Remind at ${dateFormat.format(Date(task.reminderTime!!))}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
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
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        
                        if (task.coreTasks.isNotEmpty()) {
                            Text(
                                text = "Core Tasks:",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                            
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                task.coreTasks.forEach { item ->
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(
                                            imageVector = Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp).padding(top = 2.dp),
                                            tint = Color(0xFF4CAF50)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = item,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        if (task.suggestedActions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Suggested Actions:",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                            
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                task.suggestedActions.forEach { item ->
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(
                                            imageVector = Icons.Outlined.Lightbulb,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp).padding(top = 2.dp),
                                            tint = Color(0xFFFFC107)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = item,
                                            style = MaterialTheme.typography.bodyMedium,
                                             color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Action Buttons
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_INSERT).apply {
                                        data = CalendarContract.Events.CONTENT_URI
                                        putExtra(CalendarContract.Events.TITLE, task.coreTasks.joinToString(", "))
                                        // We don't have easy access to summary here, so using theme or core tasks
                                        putExtra(CalendarContract.Events.DESCRIPTION, task.theme)
                                    }
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                shape = RoundedCornerShape(15.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Calendar", style = MaterialTheme.typography.labelMedium)
                            }

                            Button(
                                onClick = { showReminderDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                shape = RoundedCornerShape(15.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (task.reminderTime != null && task.reminderTime!! > System.currentTimeMillis()) "Update" else "Reminder", style = MaterialTheme.typography.labelMedium)
                            }

                            Button(
                                onClick = { 
                                    isLocallyIgnored = true
                                    if (!showAllTasks) {
                                        coroutineScope.launch {
                                            delay(2000)
                                            isVisible = false
                                            delay(500)
                                            onSetStatus(TaskStatus.IGNORED)
                                        }
                                    } else {
                                        onSetStatus(TaskStatus.IGNORED)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                shape = RoundedCornerShape(15.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ignore", style = MaterialTheme.typography.labelMedium, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun extractTime(startTime: String): String {
    if (startTime.isBlank()) return ""
    // Handle "2025-12-12 10:00" -> "10:00"
    val parts = startTime.split(" ", "T")
    if (parts.size > 1) {
        // If it has seconds like 10:00:00, take first two parts
        val timeParts = parts[1].split(":")
        if (timeParts.size >= 2) {
            return "${timeParts[0]}:${timeParts[1]}"
        }
        return parts[1] // The time part
    }
    return ""
}

fun shouldShowTime(startTime: String, timePart: String): Boolean {
    if (timePart.isBlank()) return false
    if (timePart == "00:00") return false
    if (timePart == "Today" || timePart == "oday") return false
    
    // Check if date is today
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = dateFormat.format(Date())
    
    val parts = startTime.split(" ", "T")
    if (parts.isNotEmpty()) {
        val datePart = parts[0]
        if (datePart == today) {
             return false
        }
    }
    
    return true
}

@Composable
fun CircularCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val animatedSize by animateFloatAsState(targetValue = if (checked) 16f else 0f, label = "checkboxSize")
    
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .border(2.dp, primaryColor, CircleShape)
            .clickable { onCheckedChange() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(animatedSize.dp)
                .clip(CircleShape)
                .background(primaryColor)
        )
    }
}
