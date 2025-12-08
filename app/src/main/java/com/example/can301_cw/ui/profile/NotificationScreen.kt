package com.example.can301_cw.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.can301_cw.ui.theme.CAN301_CWTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    notificationsEnabled: Boolean,
    defaultRemindOffset: Int,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onUpdateDefaultRemindOffset: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        var tempOffset by remember { mutableStateOf(defaultRemindOffset.toString()) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Set Default Reminder") },
            text = {
                OutlinedTextField(
                    value = tempOffset,
                    onValueChange = { tempOffset = it.filter { char -> char.isDigit() } },
                    label = { Text("Minutes after") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val minutes = tempOffset.toIntOrNull()
                    if (minutes != null && minutes >= 0) {
                        onUpdateDefaultRemindOffset(minutes)
                        showEditDialog = false
                    }
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Enable Notifications") },
                        supportingContent = { Text("Receive alerts for upcoming tasks") },
                        trailingContent = {
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = onNotificationsEnabledChange
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    ListItem(
                        headlineContent = { Text("Default Reminder") },
                        supportingContent = { Text("$defaultRemindOffset minutes after") },
                        trailingContent = {
                            IconButton(onClick = { showEditDialog = true }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    CAN301_CWTheme {
        NotificationScreen(
            notificationsEnabled = true,
            defaultRemindOffset = 15,
            onNotificationsEnabledChange = {},
            onUpdateDefaultRemindOffset = {},
            onBackClick = {}
        )
    }
}
