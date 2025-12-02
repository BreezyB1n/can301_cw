package com.example.can301_cw.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    notificationsEnabled: Boolean,
    defaultRemindOffset: Int,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onEditReminderClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            ListItem(
                headlineContent = { Text("Enable Notifications") },
                supportingContent = { Text("Receive alerts for upcoming tasks") },
                trailingContent = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsEnabledChange
                    )
                }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Default Reminder") },
                supportingContent = { Text("$defaultRemindOffset minutes before") },
                trailingContent = {
                    IconButton(onClick = onEditReminderClick) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    }
}
