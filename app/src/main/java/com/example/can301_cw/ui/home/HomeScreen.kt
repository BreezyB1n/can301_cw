package com.example.can301_cw.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data Models
data class MemoItem(
    val id: Int,
    val date: String,
    val title: String,
    val description: String,
    val tags: List<String>,
    val time: String,
    val hasImage: Boolean = true,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0
)

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    // Sample Data
    val memoGroups = listOf(
        "11月17日" to listOf(
            MemoItem(
                1, "11月17日", "Chino的用户页面",
                "这是Chino的用户页面，展示了其个人信息、好友编号SW-3802-1832-7999、以及最近的游戏记录，包括《双人成行》、《塞尔达传说 旷野之息》和《LEGO® Worlds》。页面还提供了好友列表、添加好友、邀请和用户设置等功能选项。",
                listOf("用户资料", "游戏记录", "游戏", "Nintendo Switch", "娱乐"),
                "21:19",
                hasImage = true,
                imageWidth = 1920,
                imageHeight = 1080 // Landscape -> Vertical Layout
            ),
            MemoItem(
                3, "11月17日", "购物小票",
                "超市购物清单：牛奶、面包、鸡蛋、苹果。总计：¥45.50。",
                listOf("购物", "账单"),
                "18:30",
                hasImage = true,
                imageWidth = 600,
                imageHeight = 1000 // Portrait -> Horizontal Layout
            )
        ),
        "10月20日" to listOf(
            MemoItem(
                2, "10月20日", "硕士申请项目确定会议",
                "关于硕士申请项目的初步讨论，确定了主要方向和时间表。",
                listOf("申请", "会议", "计划"),
                "14:30",
                hasImage = false
            )
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background), // Use theme background
        contentPadding = PaddingValues(bottom = 100.dp) // Space for bottom nav
    ) {
        // Top Bar Section
        item {
            TopBarSection()
        }

        // Search Bar Section
        item {
            SearchBarSection()
        }

        // Status Card Section
        item {
            StatusCardSection()
        }

        // Memo List
        memoGroups.forEach { (date, items) ->
            item {
                DateHeader(date)
            }
            items(items) { memo ->
                MemoCard(memo)
            }
        }
    }
}

@Composable
fun TopBarSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange, // Placeholder for the clock icon
                        contentDescription = "History",
                        tint = Color.Black
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(40.dp)
            ) {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = Color.Black
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(40.dp)
            ) {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        tint = Color.Black
                    )
                }
            }
        }
    }

    // "Memo" Title
    Text(
        text = "Memo",
        style = MaterialTheme.typography.displayMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color.Black
        ),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
fun SearchBarSection() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE6E9EF) // Light gray for search bar
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "搜索 Memo...",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun StatusCardSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD1F0D1)) // Light green
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color(0xFF2E7D32) // Dark green
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "今日暂无未处理意图!",
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.Black
        )
        Spacer(modifier = Modifier.width(8.dp))
        Divider(
            modifier = Modifier.weight(1f),
            color = Color.LightGray,
            thickness = 1.dp
        )
    }
}

@Composable
fun MemoCard(item: MemoItem) {
    // Determine if it's a portrait image based on aspect ratio < 0.85
    val isPortrait = if (item.hasImage && item.imageHeight > 0) {
        (item.imageWidth.toFloat() / item.imageHeight.toFloat()) < 0.85f
    } else {
        false
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (item.hasImage) {
                if (isPortrait) {
                    // Horizontal Layout (Image Left, Content Right)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Image
                        MemoImage(
                            modifier = Modifier
                                .width(120.dp)
                                .aspectRatio(item.imageWidth.toFloat() / item.imageHeight.toFloat())
                                .clip(RoundedCornerShape(12.dp))
                        )
                        
                        // Content
                        Column(modifier = Modifier.weight(1f)) {
                            MemoTextContent(item)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    MemoBottomInfo(item)
                } else {
                    // Vertical Layout (Image Top, Content Bottom)
                    MemoImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f) // Or calculate based on actual ratio
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    MemoTextContent(item)
                    Spacer(modifier = Modifier.height(12.dp))
                    MemoBottomInfo(item)
                }
            } else {
                // Text Only Layout
                MemoTextContent(item)
                Spacer(modifier = Modifier.height(12.dp))
                MemoBottomInfo(item)
            }
        }
    }
}

@Composable
fun MemoImage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Color.LightGray), // Placeholder color
        contentAlignment = Alignment.Center
    ) {
        // In a real app, use AsyncImage here
        Image(
            painter = ColorPainter(Color(0xFFEEEEEE)),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        // Simulated content inside the image
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Face,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Gray
            )
        }

        // Cloud Icon at top right of image
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Check, // Using Check as a placeholder for the sync icon
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun MemoTextContent(item: MemoItem) {
    Column {
        // Title
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Description
        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MemoBottomInfo(item: MemoItem) {
    // Tags and Time
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            // We only show a few tags to fit
            item.tags.take(3).forEach { tag ->
                TagChip(tag)
            }
            if (item.tags.size > 3) {
                TagChip("...")
            }
        }
        
        Text(
            text = item.time,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun TagChip(text: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFFF0F0F0)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        )
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen()
    }
}
