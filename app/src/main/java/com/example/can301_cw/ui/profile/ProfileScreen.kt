package com.example.can301_cw.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.can301_cw.ui.theme.CAN301_CWTheme

data class ProfileUiState(
    val notificationsEnabled: Boolean = true,
    val defaultRemindOffsetMinutes: Int = 30,
    val aiEndpoint: String = "https://api.example.com/analyze-image",
    val apiKey: String = "",
    val apiKeyDraft: String = "",
    val appVersion: String = "1.0.0"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onNotificationsToggle: (Boolean) -> Unit,
    onRemindOffsetChange: (Int) -> Unit,
    onApiKeyDraftChange: (String) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onClearData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    var localApiKey by rememberSaveable { mutableStateOf(state.apiKeyDraft) }

    LaunchedEffect(state.apiKeyDraft) {
        localApiKey = state.apiKeyDraft
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "个人中心", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "配置通知、AI 与数据管理",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NotificationSection(
                notificationsEnabled = state.notificationsEnabled,
                defaultRemindOffsetMinutes = state.defaultRemindOffsetMinutes,
                onNotificationsToggle = onNotificationsToggle,
                onRemindOffsetChange = onRemindOffsetChange
            )

            AiSection(
                aiEndpoint = state.aiEndpoint,
                apiKey = localApiKey,
                onApiKeyDraftChange = {
                    localApiKey = it
                    onApiKeyDraftChange(it)
                },
                onSaveApiKey = { onSaveApiKey(localApiKey.trim()) }
            )

            DataSection(
                onClearClick = { showClearDialog = true }
            )

            AboutSection(
                appVersion = state.appVersion
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空本地数据") },
            text = { Text("此操作将删除所有任务与提醒，且无法恢复，确认继续吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClearData()
                    }
                ) { Text("确认删除") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun NotificationSection(
    notificationsEnabled: Boolean,
    defaultRemindOffsetMinutes: Int,
    onNotificationsToggle: (Boolean) -> Unit,
    onRemindOffsetChange: (Int) -> Unit
) {
    SectionCard(
        title = "通知设置",
        description = "控制应用提醒与默认提前时间"
    )
}

@Composable
private fun AiSection(
    aiEndpoint: String,
    apiKey: String,
    onApiKeyDraftChange: (String) -> Unit,
    onSaveApiKey: () -> Unit
) {
    SectionCard(
        title = "AI 设置",
        description = "配置 AI 接口与鉴权"
    ) {

    }
}

@Composable
private fun DataSection(
    onClearClick: () -> Unit
) {
    SectionCard(
        title = "数据管理",
        description = "管理本地任务数据（仅保存在本机）"
    ) {
        Text(
            text = "清空历史记录后，所有任务与提醒将被移除。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onClearClick) {
            Text("清空本地数据")
        }
    }
}

@Composable
private fun AboutSection(
    appVersion: String
) {
    SectionCard(
        title = "关于",
        description = "版本信息与隐私说明"
    ) {
        SettingRow(
            title = "当前版本",
            description = "应用版本号与构建信息",
            action = {
                Text(
                    text = appVersion,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        Text(
            text = "所有任务数据仅存储在本地设备，图片仅在调用 AI 时上传至配置的服务端。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    description: String,
    content: @Composable androidx.compose.foundation.layout.Column.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    description: String,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        action()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F6F6)
@Composable
private fun ProfileScreenPreview() {
    CAN301_CWTheme {
        var previewState by remember {
            mutableStateOf(
                ProfileUiState(
                    notificationsEnabled = true,
                    defaultRemindOffsetMinutes = 30,
                    aiEndpoint = "https://api.example.com/analyze-image",
                    apiKey = "sample-key",
                    apiKeyDraft = "sample-key",
                    appVersion = "1.0.0"
                )
            )
        }
        ProfileScreen(
            state = previewState,
            onNotificationsToggle = { previewState = previewState.copy(notificationsEnabled = it) },
            onRemindOffsetChange = { previewState = previewState.copy(defaultRemindOffsetMinutes = it) },
            onApiKeyDraftChange = { previewState = previewState.copy(apiKeyDraft = it) },
            onSaveApiKey = { key -> previewState = previewState.copy(apiKey = key, apiKeyDraft = key) },
            onClearData = {}
        )
    }
}
