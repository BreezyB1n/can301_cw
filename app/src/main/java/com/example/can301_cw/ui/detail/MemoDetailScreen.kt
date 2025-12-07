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
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MemoDetailScreen(
    viewModel: MemoDetailViewModel,
    onBackClick: () -> Unit = {}
) {
    val memoItem by viewModel.memoItem.collectAsState()

    if (memoItem == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val item = memoItem!!
        val data = item.apiResponse ?: createFallbackApiResponse(item)
        val isAiProcessing = item.imageData != null && !item.hasAPIResponse
        MemoDetailContent(
            onBackClick = onBackClick,
            data = data,
            createdAt = item.createdAt,
            source = item.source.ifEmpty { "Manual" },
            isAICompleted = item.hasAPIResponse,
            imageData = item.imageData,
            isAiProcessing = isAiProcessing,
            originalTitle = item.title,
            tags = item.tags
        )
    }
}

fun createFallbackApiResponse(item: MemoItem): ApiResponse {
    return ApiResponse(
        mostPossibleCategory = "General",
        information = Information(
            title = item.title.ifEmpty { "Untitled Memo" },
            informationItems = listOf(
                InformationItem(
                    id = 0,
                    header = "Content",
                    content = item.recognizedText.ifEmpty { item.userInputText },
                    node = null
                )
            ),
            relatedItems = emptyList(),
            summary = item.recognizedText.ifEmpty { item.userInputText }.ifEmpty { "No content available." },
            tags = item.tags
        ),
        schedule = Schedule(
            title = item.title,
            category = "General",
            tasks = emptyList()
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoDetailContent(
    onBackClick: () -> Unit = {},
    data: ApiResponse,
    createdAt: java.util.Date = java.util.Date(),
    source: String = "Manual",
    isAICompleted: Boolean = false,
    imageData: ByteArray? = null,
    isAiProcessing: Boolean = false,
    originalTitle: String = "",
    tags: List<String> = emptyList()
) {
    val scrollState = rememberScrollState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Info", "Schedule")
    var showFullScreenImage by remember { mutableStateOf(false) }

    if (showFullScreenImage && imageData != null && imageData.isNotEmpty()) {
        Dialog(
            onDismissRequest = { showFullScreenImage = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { showFullScreenImage = false },
                contentAlignment = Alignment.Center
            ) {
                 val bitmap = remember(imageData) {
                    BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Full screen image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Memo Detail", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
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
            // Image Section
            if (imageData != null && imageData.isNotEmpty()) {
                 val bitmap = remember(imageData) {
                    BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                }
                if (bitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.05f))
                            .clickable { showFullScreenImage = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Header Section
            HeaderSection(data, createdAt, source, isAICompleted, isAiProcessing, originalTitle, tags)

            // AI Summary Section
            AISummarySection(data)
            
            // ... (rest of the content)

            // Tabs
            val pagerState = rememberPagerState(pageCount = { tabs.size })
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(selectedTabIndex) {
                pagerState.animateScrollToPage(selectedTabIndex)
            }
            
            LaunchedEffect(pagerState.currentPage) {
                selectedTabIndex = pagerState.currentPage
            }

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
                        onClick = { 
                            selectedTabIndex = index 
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            // Tab Content with HorizontalPager
            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top
            ) { page ->
                when (page) {
                    0 -> InformationTabContent(data.information)
                    1 -> ScheduleTabContent(data.schedule)
                }
            }
        }
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeaderSection(
    data: ApiResponse,
    createdAt: java.util.Date,
    source: String,
    isAICompleted: Boolean,
    isAiProcessing: Boolean,
    originalTitle: String,
    tags: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Title Logic similar to HomeScreen
        val titleText = if (isAiProcessing) "Loading..." else originalTitle.ifEmpty { data.schedule.title.takeIf { it.isNotEmpty() } ?: data.information.title }
        
        if (titleText.isNotEmpty()) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isAiProcessing) Color.Gray else Color.Unspecified
            )
        }

        // Metadata Rows
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Text("Created: ${dateFormat.format(createdAt)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Text("Source: $source", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (isAICompleted) {
                Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF4CAF50)) // Green
                Text("AI Analysis: Completed", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
            } else {
                Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray) 
                Text("AI Analysis: Pending/None", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }

        // Tags
        if (tags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tags.forEach { tag ->
                    TagChip(tag)
                }
            }
        }
    }
}

@Composable
fun TagChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(24.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
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
fun CircularCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val animatedSize by androidx.compose.animation.core.animateFloatAsState(targetValue = if (checked) 16f else 0f, label = "checkboxSize")
    
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

@Composable
fun ScheduleTabContent(schedule: Schedule) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (schedule.tasks.isEmpty()) {
            Text("No scheduled tasks.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        } else {
            schedule.tasks.forEach { task ->
                ScheduleTaskCard(task)
            }
        }
    }
}

@Composable
fun ScheduleTaskCard(task: ScheduleTask) {
    var isCompleted by remember { mutableStateOf(false) }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Checkbox
                CircularCheckbox(
                    checked = isCompleted,
                    onCheckedChange = { isCompleted = !isCompleted }
                )
                
                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.theme.ifEmpty { "Task" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                        color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Time
                    if (task.startTime.isNotEmpty()) {
                        Text(
                            text = task.startTime,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
                    color = MaterialTheme.colorScheme.primary,
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
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
                    color = MaterialTheme.colorScheme.primary,
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
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
        MemoDetailContent(
            data = mockMemoDetailData
        )
    }
}
