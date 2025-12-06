package com.example.can301_cw.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import android.graphics.BitmapFactory
import com.example.can301_cw.model.MemoItem
import com.example.can301_cw.ui.theme.CAN301_CWTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onAddMemoClick: () -> Unit,
    onMemoClick: (String) -> Unit
) {
    val memoItems by viewModel.memoItems.collectAsState()
    HomeScreenContent(
        memoItems = memoItems,
        modifier = modifier.fillMaxSize(),
        onAddMemoClick = onAddMemoClick,
        onMemoClick = onMemoClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    memoItems: List<MemoItem>,
    modifier: Modifier = Modifier,
    onAddMemoClick: () -> Unit = {},
    onMemoClick: (String) -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val memoGroups = memoItems.groupBy {
        SimpleDateFormat("MM月dd日", Locale.getDefault()).format(it.createdAt)
    }

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = "Memo",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = 16.dp, end = 8.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                IconButton(onClick = { /* TODO */ }) {
                                    Icon(
                                        imageVector = Icons.Outlined.DateRange,
                                        contentDescription = "History",
                                        tint = Color.Black
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
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
                            IconButton(onClick = onAddMemoClick) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Add",
                                    tint = Color.Black
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background), // Use theme background
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            )
        ) {
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
                    MemoCard(memo, onClick = onMemoClick)
                }
            }
        }
    }
}



@Composable
fun SearchBarSection() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE6E9EF)
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
                text = "Search Memo...",
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
                tint = Color(0xFF2E7D32)
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
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = Color.LightGray
        )
    }
}

@Composable
fun MemoCard(item: MemoItem, onClick: (String) -> Unit = {}) {
    val hasImage = item.imageData != null && item.imageData!!.isNotEmpty()
    val imageAspectRatio = remember(item.imageData) {
        if (hasImage) {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(item.imageData, 0, item.imageData!!.size, options)
            if (options.outWidth > 0 && options.outHeight > 0) {
                options.outWidth.toFloat() / options.outHeight.toFloat()
            } else {
                1f // Default if decode fails
            }
        } else {
            1f
        }
    }
    
    // Decide layout based on aspect ratio
    // Portrait (ratio < 1) -> Horizontal Layout (Image Left, Content Right)
    // Landscape (ratio >= 1) -> Vertical Layout (Image Top, Content Bottom)
    val isPortrait = imageAspectRatio < 1f

    Card(
        onClick = { onClick(item.id) },
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
            if (hasImage) {
                if (isPortrait) {
                    // Horizontal Layout (Image Left, Content Right)
                    
                    // Calculate estimated max lines based on image height
                    // Image width is 120.dp, aspect ratio is constrained between 0.5 and 1.0
                    val displayedRatio = imageAspectRatio.coerceIn(0.5f, 1.0f)
                    // Image height in dp = 120 / ratio
                    val estimatedImageHeight = 120f / displayedRatio

                    // Estimate Title Height
                    // Title width is roughly (Screen - 120 - 32 - 16). Assuming 360dp screen -> 192dp available.
                    // titleMedium bold ~ 9-10dp per char?
                    // Let's assume 18 chars per line conservatively.
                    val estimatedTitleLines = (item.title.length / 18) + 1
                    // Title line height ~ 24dp (22sp + padding), plus 8dp spacing below title
                    val estimatedTitleHeight = estimatedTitleLines * 24 + 8

                    // Estimate Body Lines
                    // (ImageHeight - TitleHeight) / BodyLineHeight
                    // Body line height ~ 22dp (20sp + spacing)
                    val availableHeight = estimatedImageHeight - estimatedTitleHeight
                    val calculatedMaxLines = max(3, (availableHeight / 22).toInt())
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Image
                        MemoImage(
                            imageData = item.imageData,
                            modifier = Modifier
                                .width(120.dp)
                                .aspectRatio(imageAspectRatio.coerceIn(0.5f, 1.0f)) // Constrain aspect ratio for list view
                                .clip(RoundedCornerShape(12.dp))
                        )
                        
                        // Content
                        Column(modifier = Modifier.weight(1f)) {
                            MemoTextContent(item, maxLines = calculatedMaxLines)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    MemoBottomInfo(item)
                } else {
                    // Vertical Layout (Image Top, Content Bottom)
                    MemoImage(
                        imageData = item.imageData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(imageAspectRatio.coerceIn(1.0f, 2.0f)) // Constrain aspect ratio
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
fun MemoImage(imageData: ByteArray?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Color.LightGray), // Placeholder color
        contentAlignment = Alignment.Center
    ) {
        if (imageData != null && imageData.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = ColorPainter(Color(0xFFEEEEEE)),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Image(
                painter = ColorPainter(Color(0xFFEEEEEE)),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        if (imageData == null || imageData.isEmpty()) {
            // Simulated content inside the image
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Face,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.Gray
                )
            }
        }
    }
}

@Composable
fun MemoTextContent(item: MemoItem, maxLines: Int = 4) {
    // Determine if we are waiting for AI response
    // We assume if there is an image and no API response yet, it's processing
    val isAiProcessing = item.imageData != null && !item.hasAPIResponse

    Column {
        // Title
        val titleText = if (isAiProcessing) "Loading..." else item.title
        
        if (titleText.isNotEmpty()) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                ),
                color = if (isAiProcessing) Color.Gray else Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        val description = if (isAiProcessing) {
            "Analyzing image content..."
        } else {
            if (item.recognizedText.isNotEmpty()) item.recognizedText else item.userInputText
        }

        if (description.isNotEmpty()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemoBottomInfo(item: MemoItem) {
    // Tags and Time
    val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(item.createdAt)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            item.tags.forEach { tag ->
                TagChip(tag)
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))

        Row(
            modifier = Modifier.padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = timeString,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
            )
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

@Preview
@Composable
fun HomeScreenPreview() {
    CAN301_CWTheme {
        val memoItems = listOf(
            MemoItem(
                id = "2",
                title = "Chino的用户页面",
                recognizedText = "这是Chino的用户页面，展示了其个人信息、好友编号SW-3802-1832-7999、以及最近的游戏记录，包括《双人成行》、《塞尔达传说 旷野之息》和《LEGO® Worlds》。页面还提供了好友列表、添加好友、邀请和用户设置等功能选项。",
                tags = mutableListOf("用户资料", "游戏记录", "游戏", "Nintendo Switch", "娱乐"),
                createdAt = Date()
            ).apply {
                imageData = ByteArray(1)
            },
            MemoItem(
                id = "1",
                title = "购物小票",
                recognizedText = "超市购物清单：牛奶、面包、鸡蛋、苹果。总计：¥45.50。",
                tags = mutableListOf("购物", "账单"),
                createdAt = Date()
            ).apply {
                imageData = ByteArray(1)
            }
        )
        HomeScreenContent(memoItems = memoItems)
    }
}
