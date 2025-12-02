package com.example.can301_cw.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegrationScreen(
    isCalendarSyncEnabled: Boolean,
    onCalendarSyncEnabledChange: (Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Integrations") },
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
                headlineContent = { Text("Sync to System Calendar") },
                supportingContent = { Text("Add tasks to device calendar automatically") },
                trailingContent = {
                    Switch(
                        checked = isCalendarSyncEnabled,
                        onCheckedChange = onCalendarSyncEnabledChange
                    )
                }
            )
        }
    }
}
