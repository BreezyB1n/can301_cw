package com.example.can301_cw.ui.detail

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
import androidx.compose.material.icons.outlined.Edit
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoDetailScreen(
    onBackClick: () -> Unit = {},
    data: ApiResponse = mockMemoDetailData
) {
    val scrollState = rememberScrollState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("信息", "日程")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("详细信息", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Edit action */ }) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                    }
                }
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
            Text("创建时间: 2025-12-01", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Text("来源: 手动创建", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF4CAF50)) // Green
            Text("AI分析完成", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
        }

        // Tags
        val tags = (data.schedule.tasks.firstOrNull()?.tags ?: data.information.tags)
        if (tags.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                tags.take(4).forEach { tag ->
                    SuggestionChip(
                        onClick = { },
                        label = { Text(tag) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = null
                    )
                }
            }
        }
    }
}

@Composable
fun AISummarySection(data: ApiResponse) {
    val summary = data.information.summary
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "AI 摘要",
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
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        
        // Core Tasks
        if (task.coreTasks.isNotEmpty()) {
            Text(
                text = "核心任务",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    task.coreTasks.forEach { coreTask ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = coreTask, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        // Suggested Actions
        if (task.suggestedActions.isNotEmpty()) {
            Text(
                text = "建议行动",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    task.suggestedActions.forEach { action ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Outlined.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFFFC107), // Amber
                                modifier = Modifier.size(20.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = action, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

val mockMemoDetailData = ApiResponse(
    mostPossibleCategory = "INFORMATION",
    schedule = Schedule(
        title = "关注软件工程职位招聘趋势",
        category = "职业发展",
        tasks = listOf(
            ScheduleTask(
                startTime = "2025-12-01T19:00:00+08:00",
                endTime = "2025-12-01T20:00:00+08:00",
                theme = "招聘市场分析",
                coreTasks = listOf(
                    "阅读“程序员并没有想象中那么危险”相关分析报告。",
                    "了解2024-2025年软件工程职位招聘数量变化趋势。",
                    "分析机器、数据、后端、DevOps、质量保证、安全、移动端、前端等不同技术方向职位的招聘数量变化百分比。"
                ),
                position = listOf("居家/办公室"),
                tags = listOf("职业规划", "招聘", "软件工程", "市场趋势"),
                category = "学习研究",
                suggestedActions = listOf(
                    "根据报告内容，评估自身技能与市场需求匹配度。",
                    "思考哪些技术领域在未来招聘市场中更具潜力。",
                    "查看Bloomberry.com获取更多相关数据和分析。",
                    "如果对数据分析感兴趣，可以进一步研究数据工程师和数据科学家的职位变化。"
                ),
                people = emptyList()
            )
        )
    ),
    information = Information(
        title = "2024-2025年软件工程职位新招聘职位数量变化百分比",
        informationItems = listOf(
            InformationItem(1, "机器学习工程师", "机器学习工程师的招聘职位数量增长了39.62%。", null),
            InformationItem(2, "数据工程师", "数据工程师的招聘职位数量增长了9.35%。", InformationNode(3, "CHILD")),
            InformationItem(3, "后端工程师", "后端工程师的招聘职位数量增长了4.44%。", InformationNode(4, "CHILD")),
            InformationItem(4, "数据科学家", "数据科学家的招聘职位数量增长了3.48%。", InformationNode(5, "CHILD")),
            InformationItem(5, "DevOps工程师", "DevOps工程师的招聘职位数量增长了2.92%。", InformationNode(6, "CHILD")),
            InformationItem(6, "质量保证工程师", "质量保证工程师的招聘职位数量增长了1.00%。", InformationNode(7, "CHILD")),
            InformationItem(7, "安全工程师", "安全工程师的招聘职位数量变化为-0.35%。", InformationNode(8, "CHILD")),
            InformationItem(8, "移动端工程师", "移动端工程师的招聘职位数量下降了5.73%。", InformationNode(9, "CHILD")),
            InformationItem(9, "前端工程师", "前端工程师的招聘职位数量下降了9.89%。", InformationNode(10, "CHILD")),
            InformationItem(10, "招聘职位平均下降", "整体招聘职位平均下降了8%。", null)
        ),
        relatedItems = listOf("软件工程", "招聘趋势", "职业发展"),
        summary = "该图表展示了2024-2025年间不同软件工程职位新招聘职位数量的变化百分比。其中，机器学习工程师的招聘职位数量增长最为显著，达到39.62%，而前端工程师的招聘职位数量下降最为严重，为-9.89%。整体来看，部分技术岗位的招聘需求在增长，但平均招聘职位数量有所下降。",
        tags = listOf("招聘", "软件工程", "职业趋势")
    )
)

@Preview(showBackground = true)
@Composable
fun MemoDetailScreenPreview() {
    MaterialTheme {
        MemoDetailScreen()
    }
}
