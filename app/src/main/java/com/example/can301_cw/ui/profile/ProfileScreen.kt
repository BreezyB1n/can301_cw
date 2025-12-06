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
import androidx.compose.foundation.clickable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.can301_cw.data.ImageStorageManager
import com.example.can301_cw.model.User
import com.example.can301_cw.model.UserStats
import com.example.can301_cw.ui.theme.BluePrimary
import com.example.can301_cw.ui.theme.CustomColorPickerDialog
import kotlinx.coroutines.launch
import java.io.File

private enum class ProfileDestination {
    Main,
    Appearance,
    Notifications,
    Integrations,
    AIConfiguration,
    AccountDetails,
    ChangePassword
}

private fun getLevel(destination: ProfileDestination): Int {
    return when (destination) {
        ProfileDestination.Main -> 0
        ProfileDestination.ChangePassword -> 2
        else -> 1
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogout: () -> Unit,
    onLoginClick: () -> Unit
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
            val initialLevel = getLevel(initialState)
            val targetLevel = getLevel(targetState)

            if (targetLevel > initialLevel) {
                // Navigate to details (Deeper): Slide in from right, slide out to left
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width } + fadeOut())
            } else {
                // Back to main (Shallower): Slide in from left, slide out to right
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
                            user = uiState.user,
                            onLoginClick = onLoginClick,
                            onUserClick = { destination = ProfileDestination.AccountDetails }
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
                                text = "MemoFlux AI Assistant v1.0.0",
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
            ProfileDestination.AccountDetails -> {
                AccountDetailsScreen(
                    user = uiState.user,
                    onUpdateAvatar = { viewModel.updateAvatar(it) },
                    onUpdateUsername = { viewModel.updateUsername(it) },
                    onChangePasswordClick = { destination = ProfileDestination.ChangePassword },
                    onLogout = {
                        viewModel.logout()
                        onLogout()
                    },
                    onBackClick = { destination = ProfileDestination.Main }
                )
            }
            ProfileDestination.ChangePassword -> {
                ChangePasswordScreen(
                    onConfirm = { old, new ->
                        viewModel.updatePassword(
                            oldPassword = old,
                            newPassword = new,
                            onSuccess = {
                                // Maybe show a toast? Or go back
                                destination = ProfileDestination.AccountDetails
                            },
                            onError = { /* Handle error, maybe show snackbar or toast in screen */ }
                        )
                    },
                    onBackClick = { destination = ProfileDestination.AccountDetails }
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
fun UserInfoHeader(
    user: User?,
    onLoginClick: () -> Unit,
    onUserClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = user != null, onClick = onUserClick)
            .padding(vertical = 8.dp),
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
            if (user?.avatarPath != null) {
                AsyncImage(
                    model = user.avatarPath,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(48.dp),
                    tint = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (user != null) {
            Text(
                text = user.username,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "Guest",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onLoginClick,
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text("Log In / Sign Up")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsScreen(
    user: User?,
    onUpdateAvatar: (String) -> Unit,
    onUpdateUsername: (String) -> Unit,
    onChangePasswordClick: () -> Unit,
    onLogout: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val imageStorageManager = remember { ImageStorageManager(context) }
    var showEditUsernameDialog by remember { mutableStateOf(false) }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            // Save image to local storage
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                val path = imageStorageManager.saveImage(bytes)
                onUpdateAvatar(path)
            }
        }
    }

    if (showEditUsernameDialog) {
        var tempUsername by remember { mutableStateOf(user?.username ?: "") }
        AlertDialog(
            onDismissRequest = { showEditUsernameDialog = false },
            title = { Text("Edit Username") },
            text = {
                OutlinedTextField(
                    value = tempUsername,
                    onValueChange = { tempUsername = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tempUsername.isNotBlank()) {
                        onUpdateUsername(tempUsername)
                        showEditUsernameDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditUsernameDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Avatar
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .clickable { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                     if (user?.avatarPath != null) {
                        AsyncImage(
                            model = user.avatarPath,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit Avatar",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Info Cards
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Username") },
                        supportingContent = { Text(user?.username ?: "") },
                        leadingContent = { Icon(Icons.Filled.Person, null) },
                        trailingContent = { Icon(Icons.Filled.Edit, null) },
                        modifier = Modifier.clickable { showEditUsernameDialog = true },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Email") },
                        supportingContent = { Text(user?.email ?: "") },
                        leadingContent = { Icon(Icons.Filled.Email, null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                ListItem(
                    headlineContent = { Text("Change Password") },
                    leadingContent = { Icon(Icons.Filled.Lock, null) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
                    modifier = Modifier.clickable { onChangePasswordClick() },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log Out")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onConfirm: (String, String) -> Unit,
    onBackClick: () -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Password") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                label = { Text("Old Password") },
                visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                        Icon(if (oldPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                        Icon(if (newPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm New Password") },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = newPassword != confirmPassword && confirmPassword.isNotEmpty(),
                supportingText = {
                    if (newPassword != confirmPassword && confirmPassword.isNotEmpty()) {
                        Text("Passwords do not match")
                    }
                }
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    if (newPassword != confirmPassword) {
                        errorMessage = "Passwords do not match"
                    } else if (newPassword.length < 6) {
                        errorMessage = "Password must be at least 6 characters"
                    } else {
                        errorMessage = null
                        onConfirm(oldPassword, newPassword)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = oldPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank()
            ) {
                Text("Update Password")
            }
        }
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


