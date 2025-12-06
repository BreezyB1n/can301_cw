package com.example.can301_cw.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.can301_cw.model.*
import com.example.can301_cw.data.mockMemoDetailData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoDetailScreen(
    onBackClick: () -> Unit = {},
    data: ApiResponse = mockMemoDetailData
) {
    val scrollState = rememberScrollState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Info", "Schedule")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Memo Detail", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                // actions = {
                //     IconButton(onClick = { /* Edit action */ }) {
                //         Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                //     }
                // }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            HeaderSection(data)

            // AI Summary Section
            AISummarySection(data)

            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> InformationTabContent(data.information)
                1 -> ScheduleTabContent(data.schedule)
            }
            
            // Spacer for bottom bar
            if (selectedTabIndex == 1) {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun HeaderSection(data: ApiResponse) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Placeholder for Image if needed
        val title = data.schedule.title.takeIf { it.isNotEmpty() } ?: data.information.title
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Metadata Rows
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Text("Created: 2025-12-01", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Text("Source: Manually", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF4CAF50)) // Green
            Text("AI Analysis: Completed", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
        }

        // Tags
        val tags = (data.schedule.tasks.firstOrNull()?.tags ?: data.information.tags)
        if (tags.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                tags.forEach { tag ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AISummarySection(data: ApiResponse) {
    val summary = data.information.summary
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "AI Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface 
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InformationTabContent(info: Information) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        info.informationItems.forEach { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = item.header,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.content,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleTabContent(schedule: Schedule) {
    val task = schedule.tasks.firstOrNull() ?: return
    ScheduleTaskCard(task)
}

@Composable
fun ScheduleTaskCard(task: ScheduleTask) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header: Title and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.theme.ifEmpty { "Task" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            // Time
            if (task.startTime.isNotEmpty()) {
                Text(
                    text = task.startTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )

            // Core Tasks
            if (task.coreTasks.isNotEmpty()) {
                Text(
                    text = "Core Tasks:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    task.coreTasks.forEach { coreTask ->
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = coreTask,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Suggested Actions
            if (task.suggestedActions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Suggested Actions:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    task.suggestedActions.forEach { action ->
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Outlined.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(20.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = action,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            //// Tags
            //  if (task.tags.isNotEmpty()) {
            //     Spacer(modifier = Modifier.height(2.dp))
            //     Row(
            //         horizontalArrangement = Arrangement.spacedBy(6.dp),
            //         modifier = Modifier.horizontalScroll(rememberScrollState())
            //     ) {
            //         task.tags.forEach { tag ->
            //             SuggestionChip(
            //                 onClick = { },
            //                 label = { 
            //                     Text(
            //                         tag, 
            //                         style = MaterialTheme.typography.labelMedium
            //                     ) 
            //                 },
            //                 colors = SuggestionChipDefaults.suggestionChipColors(
            //                     containerColor = MaterialTheme.colorScheme.surfaceVariant
            //                 ),
            //                 border = null,
            //                 modifier = Modifier.height(30.dp),
            //                 shape = RoundedCornerShape(8.dp)
            //             )
            //         }
            //     }
            // }

            // Footer Actions
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { /* Add Reminder */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Set Reminder", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = { /* Delete */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Ignore", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ignore", style = MaterialTheme.typography.labelMedium, color = Color.White)
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MemoDetailScreenPreview() {
    MaterialTheme {
        MemoDetailScreen()
    }
}
