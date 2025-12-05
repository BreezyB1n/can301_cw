package com.example.can301_cw.ui.add

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton // Ensure this is explicitly imported
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.can301_cw.model.ApiResponse
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMemoScreen(
    viewModel: AddMemoViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Image Picker
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.onImageSelected(uri)
        }
    }

    // Camera Logic
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempImageUri != null) {
            viewModel.onImageSelected(tempImageUri)
        }
    }

    fun launchCamera() {
        val tmpFile = File.createTempFile("tmp_image_file", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tmpFile)
        tempImageUri = uri
        takePicture.launch(uri)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("创建 Memo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.Close, contentDescription = "取消")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Text Input Section
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        placeholder = { Text("输入标题", style = MaterialTheme.typography.headlineSmall.copy(color = Color.Gray)) },
                        textStyle = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = androidx.compose.material3.TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    
                    HorizontalDivider()

                    OutlinedTextField(
                        value = uiState.content,
                        onValueChange = { viewModel.updateContent(it) },
                        placeholder = { Text("在这里输入文字\n或点击下方按钮，上传图片", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        colors = androidx.compose.material3.TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        supportingText = { 
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Text("${uiState.content.length}") 
                            }
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleAiParsing(!uiState.useAIParsing) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.useAIParsing) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (uiState.useAIParsing) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "使用 AI 解析",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (uiState.useAIParsing) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                        Spacer(modifier = Modifier.weight(1f)) // Push count to the right if needed, or just take up space
                    }
                }
            }

            // 2. Media Section (Dual Buttons)
            if (uiState.selectedImageUri == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Camera Button
                    MediaButton(
                        icon = Icons.Filled.PhotoCamera,
                        text = "拍照",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { launchCamera() }
                    )
                    
                    // Gallery Button
                    MediaButton(
                        icon = Icons.Filled.Image,
                        text = "从相册选择",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        onClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("* 暂时只能拍摄/选择一张照片", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            } else {
                // Image Preview
                Box(modifier = Modifier.fillMaxWidth()) {
                    ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                        AsyncImage(
                            model = uiState.selectedImageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    FilledIconButton(
                        onClick = { viewModel.removeImage() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove")
                    }
                }
                Text("✅ 图片已处理完成", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
            }

            // 3. AI Analysis Section
            if (uiState.useAIParsing) {
                Button(
                    onClick = { viewModel.parseContent() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !uiState.isParsing && (uiState.content.isNotBlank() || uiState.selectedImageUri != null),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.apiResponse != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (uiState.isParsing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("正在解析...")
                    } else {
                        Text(if (uiState.apiResponse != null) "再次解析" else "解析")
                    }
                }

                Text("智能解析结果", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                if (uiState.apiResponse == null && !uiState.isParsing) {
                    // Waiting State
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(40.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("等待解析信息", fontWeight = FontWeight.Bold)
                                Text("AI 分析：未识别到有效内容，请输入或上传信息", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("💡 添加更多内容可获得更精准的解析结果", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE6A23C))
                            }
                        }
                    }
                } else if (uiState.apiResponse != null) {
                    AnalysisResultSection(uiState.apiResponse!!, context)
                }
            }

            // 4. Tags Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("添加标签", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                TextButton(onClick = { viewModel.showTagInputDialog(true) }) {
                    Text("添加自定义")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // AI Suggested Tags
                if (uiState.apiResponse?.allTags?.isNotEmpty() == true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI 建议标签", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.apiResponse?.allTags?.forEach { tag ->
                            FilterChip(
                                selected = true,
                                onClick = { /* Already selected */ },
                                label = { Text(tag) },
                                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                // Local/Common Tags
                Text("本地标签", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.localTags.forEach { tag ->
                        val isSelected = uiState.selectedTags.contains(tag)
                        FilterChip(
                            selected = isSelected,
                            onClick = { if (isSelected) viewModel.removeTag(tag) else viewModel.addTag(tag) },
                            label = { Text(tag) },
                            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFFF5F5F5),
                                labelColor = Color.Gray,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
            
            
            Button(
                onClick = { 
                    viewModel.saveMemo(onSuccess = onNavigateBack)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isSaving,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("保存中...")
                } else {
                    Text("创建 Memo", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (uiState.showTagInputDialog) {
        TagInputDialog(
            onDismiss = { viewModel.showTagInputDialog(false) },
            onConfirm = { tag ->
                viewModel.addTag(tag)
                viewModel.showTagInputDialog(false)
            }
        )
    }
}

@Composable
fun MediaButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, color = color)
        }
    }
}

@Composable
fun HorizontalDivider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        thickness = 0.5.dp,
        color = Color.LightGray.copy(alpha = 0.5f)
    )
}

// Helper extension for scaling
fun Modifier.scale(scale: Float): Modifier = this.then(Modifier.size(width = 50.dp * scale, height = 30.dp * scale)) // Simplified scaling hack for switch

@Composable
fun AnalysisResultSection(response: ApiResponse, context: Context) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with green bar
            Row(modifier = Modifier.fillMaxWidth()) {
                 Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(40.dp)
                        .background(Color(0xFF4CAF50), RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("AI 分析完成", fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("AI 分析：${response.information.summary}", style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 3)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("意图识别", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // Intent Card
            if (response.mostPossibleCategory == "SCHEDULE") {
                Surface(
                    color = Color(0xFFFFF8E1), // Light Yellow
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = Color(0xFFFFA000), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("检测到日程安排", fontWeight = FontWeight.Bold, color = Color(0xFFFFA000))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("日程：${response.schedule.title}", style = MaterialTheme.typography.bodySmall)
                        response.schedule.tasks.firstOrNull()?.let { task ->
                            Text("时间：${task.startTime}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_INSERT).apply {
                                            data = CalendarContract.Events.CONTENT_URI
                                            putExtra(CalendarContract.Events.TITLE, task.coreTasks.joinToString(", "))
                                            putExtra(CalendarContract.Events.DESCRIPTION, response.information.summary)
                                        }
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("添加到日历", fontSize = androidx.compose.ui.unit.TextUnit.Unspecified) // scalable sp
                                }

                                OutlinedButton(
                                    onClick = { Toast.makeText(context, "已添加到提醒事项 (模拟)", Toast.LENGTH_SHORT).show() },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFFA000)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFA000))
                                ) {
                                    Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("添加到提醒事项")
                                }
                            }
                        }
                    }
                }
            } else {
                 Surface(
                    color = Color(0xFFE3F2FD), // Light Blue
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                     Column(modifier = Modifier.padding(12.dp)) {
                         Text("检测到信息整理", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                         Text(response.information.summary, style = MaterialTheme.typography.bodySmall)
                     }
                }
            }
        }
    }
}

@Composable
fun TagInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加标签") },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("标签名称") }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onConfirm(text) }) { Text("确定") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
