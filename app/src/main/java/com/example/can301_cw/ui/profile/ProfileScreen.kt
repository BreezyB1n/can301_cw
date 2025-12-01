package com.example.can301_cw.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.ui.tooling.preview.Preview
import com.example.can301_cw.model.UserStats
import com.example.can301_cw.ui.theme.CAN301_CWTheme

import com.example.can301_cw.ui.theme.AppTheme
import com.example.can301_cw.ui.theme.BluePrimary
import com.example.can301_cw.ui.theme.GreenPrimary
import com.example.can301_cw.ui.theme.YellowPrimary
import androidx.compose.ui.graphics.Color
import com.example.can301_cw.ui.theme.GreyPrimary

import androidx.compose.foundation.border
import androidx.compose.ui.graphics.compositeOver

import androidx.compose.foundation.isSystemInDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory(
        application = LocalContext.current.applicationContext as Application
    ))
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val isSystemDark = isSystemInDarkTheme()

    // Dialog state for clearing history
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear History") },
            text = { Text("Are you sure you want to delete all tasks? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 0. User Info Header
            UserInfoHeader(
                username = "John Doe",
                userId = "UID: 12345678"
            )

            // 1. Statistics Dashboard
            StatisticsCard(stats = uiState.stats)

            // 2. Settings Sections
            SettingsSection(title = "Appearance") {
                // Theme Color
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Theme Color",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        val themes = listOf(
                            Triple(AppTheme.Blue, BluePrimary, "Blue"),
                            Triple(AppTheme.Yellow, YellowPrimary, "Yellow"),
                            Triple(AppTheme.Green, GreenPrimary, "Green")
                        )
                        
                        themes.forEach { (theme, color, label) ->
                            ThemeColorOption(
                                color = color,
                                label = label,
                                selected = uiState.currentTheme == theme,
                                onClick = { viewModel.setTheme(theme) }
                            )
                        }
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Dark Mode
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Dark Mode",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    
                    val darkModeOptions = listOf(
                        DarkModeConfig.FOLLOW_SYSTEM to "Follow System",
                        DarkModeConfig.LIGHT to "Light",
                        DarkModeConfig.DARK to "Dark"
                    )
                    
                    darkModeOptions.forEach { (config, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setDarkModeConfig(config) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            if (uiState.darkModeConfig == config) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            SettingsSection(title = "Notifications") {
                ListItem(
                    headlineContent = { Text("Enable Notifications") },
                    supportingContent = { Text("Receive alerts for upcoming tasks") },
                    trailingContent = {
                        Switch(
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text("Default Reminder") },
                    supportingContent = { Text("${uiState.defaultRemindOffset} minutes before") },
                    trailingContent = {
                        // Simple dropdown or dialog could be added here for selection
                        // For now, just a button to cycle or static display
                        IconButton(onClick = { /* TODO: Show picker */ }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                    }
                )
            }

            SettingsSection(title = "Integrations") {
                ListItem(
                    headlineContent = { Text("Sync to System Calendar") },
                    supportingContent = { Text("Add tasks to device calendar automatically") },
                    trailingContent = {
                        Switch(
                            checked = uiState.isCalendarSyncEnabled,
                            onCheckedChange = { viewModel.setCalendarSyncEnabled(it) }
                        )
                    }
                )
            }

            SettingsSection(title = "AI Configuration") {
                // Endpoint
                var endpoint by remember(uiState.aiEndpoint) { mutableStateOf(uiState.aiEndpoint) }
                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    label = { Text("AI Endpoint URL") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    singleLine = true
                )

                // API Key
                var apiKey by remember(uiState.aiApiKey) { mutableStateOf(uiState.aiApiKey) }
                var showApiKey by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showApiKey = !showApiKey }) {
                            Icon(
                                // TODO: Use correct Visibility icons when available
                                if (showApiKey) Icons.Filled.Check else Icons.Filled.Add,
                                contentDescription = "Toggle API Key visibility"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    singleLine = true
                )

                // Save Button
                Button(
                    onClick = { viewModel.updateAiConfig(endpoint, apiKey) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                    ) {
                    Text("Save Configuration")
                }
            }

            SettingsSection(title = "Data Management") {
                Button(
                    onClick = { showClearDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Clear All History")
                }
            }

            // About
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AI Snap Scheduler v1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Data stored locally. Images processed securely.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun UserInfoHeader(username: String, userId: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar Placeholder
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Avatar",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = username,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = userId,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StatisticsCard(stats: UserStats) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(label = "Info Saved", value = stats.savedInformation)
            StatItem(label = "Pending", value = stats.pendingTasks)
            StatItem(label = "Completed", value = stats.completedTasks)
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            content()
        }
    }
}

@Composable
fun ThemeColorOption(
    color: Color,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp) // Smaller size
                .clip(CircleShape)
                .background(color)
                .border(
                    width = 2.dp,
                    color = color.copy(alpha = 0.6f).compositeOver(Color.Black), // Slightly darker border
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    CAN301_CWTheme {
        Column(modifier = Modifier.fillMaxSize()) {
             Text("Profile Screen Preview requires mocking ViewModel or Context which is complex in this file structure. Please run the app to see the screen.")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserInfoHeaderPreview() {
    CAN301_CWTheme {
        UserInfoHeader(username = "Preview User", userId = "UID: 000000")
    }
}

@Preview(showBackground = true)
@Composable
fun StatisticsCardPreview() {
    CAN301_CWTheme {
        StatisticsCard(stats = UserStats("5", "12", "30m"))
    }
}
