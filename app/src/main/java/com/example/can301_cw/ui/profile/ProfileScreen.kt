package com.example.can301_cw.ui.profile

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.can301_cw.model.UserStats
import com.example.can301_cw.ui.theme.*

private enum class ProfileDestination {
    Main,
    Appearance,
    Notifications,
    Integrations,
    AIConfiguration
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory(
        application = LocalContext.current.applicationContext as Application
    ))
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    var destination by rememberSaveable { mutableStateOf(ProfileDestination.Main) }
    
    var showClearDialog by remember { mutableStateOf(false) }
    
    // Custom color picker dialog state
    var showColorPickerDialog by remember { mutableStateOf(false) }

    // Handle Back Press
    BackHandler(enabled = destination != ProfileDestination.Main) {
        destination = ProfileDestination.Main
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear History", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("Are you sure you want to delete all tasks? This action cannot be undone.", style = MaterialTheme.typography.bodyMedium) },
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
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showColorPickerDialog) {
        CustomColorPickerDialog(
            onColorSelected = { color ->
                showColorPickerDialog = false
                // Save and apply custom theme
                viewModel.setCustomTheme(color.value.toLong())
            },
            onDismiss = { showColorPickerDialog = false },
            initialColor = if (uiState.customThemeColor != 0L) {
                try {
                    Color(uiState.customThemeColor.toULong())
                } catch (e: Exception) {
                    BluePrimary
                }
            } else {
                BluePrimary
            }
        )
    }

    // Main Content Switching
    AnimatedContent(
        targetState = destination,
        transitionSpec = {
            if (targetState != ProfileDestination.Main && initialState == ProfileDestination.Main) {
                // Navigate to details: Slide in from right, slide out to left
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width } + fadeOut())
            } else {
                // Back to main: Slide in from left, slide out to right
                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> width } + fadeOut())
            }
        },
        label = "ProfileNavigation"
    ) { targetDestination ->
        when (targetDestination) {
            ProfileDestination.Main -> {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Profile",
                                    fontWeight = FontWeight.Bold
                                )
                            },
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

                        // 2. Settings List
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column {
                                SettingsListTile(
                                    title = "Appearance",
                                    icon = Icons.Outlined.Create,
                                    onClick = { destination = ProfileDestination.Appearance }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                SettingsListTile(
                                    title = "Notifications",
                                    icon = Icons.Outlined.Notifications,
                                    onClick = { destination = ProfileDestination.Notifications }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                SettingsListTile(
                                    title = "Integrations",
                                    icon = Icons.Outlined.DateRange,
                                    onClick = { destination = ProfileDestination.Integrations }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                SettingsListTile(
                                    title = "AI Configuration",
                                    icon = Icons.Outlined.Settings,
                                    onClick = { destination = ProfileDestination.AIConfiguration }
                                )
                            }
                        }

                        // 3. Data Management
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        ) {
                            SettingsListTile(
                                title = "Clear All History",
                                icon = Icons.Outlined.Delete,
                                iconTint = MaterialTheme.colorScheme.onError,
                                textColor = MaterialTheme.colorScheme.onError,
                                onClick = { showClearDialog = true },
                                showArrow = false
                            )
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
            ProfileDestination.Appearance -> {
                AppearanceScreen(
                    currentTheme = uiState.currentTheme,
                    customThemeColor = uiState.customThemeColor,
                    darkModeConfig = uiState.darkModeConfig,
                    onThemeSelected = { viewModel.setTheme(it) },
                    onCustomizeColorClick = { showColorPickerDialog = true },
                    onDarkModeConfigSelected = { viewModel.setDarkModeConfig(it) },
                    onBackClick = { destination = ProfileDestination.Main }
                )
            }
            ProfileDestination.Notifications -> {
                NotificationScreen(
                    notificationsEnabled = uiState.notificationsEnabled,
                    defaultRemindOffset = uiState.defaultRemindOffset,
                    onNotificationsEnabledChange = { viewModel.setNotificationsEnabled(it) },
                    onEditReminderClick = { /* TODO */ },
                    onBackClick = { destination = ProfileDestination.Main }
                )
            }
            ProfileDestination.Integrations -> {
                IntegrationScreen(
                    isCalendarSyncEnabled = uiState.isCalendarSyncEnabled,
                    onCalendarSyncEnabledChange = { viewModel.setCalendarSyncEnabled(it) },
                    onBackClick = { destination = ProfileDestination.Main }
                )
            }
            ProfileDestination.AIConfiguration -> {
                AIConfigurationScreen(
                    initialEndpoint = uiState.aiEndpoint,
                    initialApiKey = uiState.aiApiKey,
                    onSaveConfig = { endpoint, apiKey -> 
                        viewModel.updateAiConfig(endpoint, apiKey)
                        destination = ProfileDestination.Main // Optional: Go back after save
                    },
                    onBackClick = { destination = ProfileDestination.Main }
                )
            }
        }
    }
}

@Composable
fun SettingsListTile(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    showArrow: Boolean = true
) {
    ListItem(
        headlineContent = { 
            Text(
                text = title,
                color = textColor
            ) 
        },
        leadingContent = { 
            Icon(
                imageVector = icon, 
                contentDescription = null,
                tint = iconTint
            ) 
        },
        trailingContent = if (showArrow) {
            {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else null,
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
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
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Avatar",
                modifier = Modifier.size(48.dp),
                tint = Color.Black
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
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
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


