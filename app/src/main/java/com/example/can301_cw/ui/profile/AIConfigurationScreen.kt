package com.example.can301_cw.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.can301_cw.ui.theme.CAN301_CWTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIConfigurationScreen(
    initialEndpoint: String,
    initialApiKey: String,
    onSaveConfig: (String, String) -> Unit,
    onBackClick: () -> Unit
) {
    var endpoint by remember(initialEndpoint) { mutableStateOf(initialEndpoint) }
    var apiKey by remember(initialApiKey) { mutableStateOf(initialApiKey) }
    var showApiKey by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Configuration") },
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
            // LLM Section
            Text(
                text = "LLM",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "doubao-seed-1-6-vision-250815",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // API Key Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Endpoint (Hidden as requested, but keeping state if needed, or removing if strictly following instructions.
                    // User asked to change Endpoint URL to a fixed LLM selection.
                    // But wait, the original code had Endpoint URL input.
                    // The prompt says "将AI EndPoint URL改为固定的一行的圆角矩形...".
                    // It implies the endpoint configuration is replaced by this fixed selection or maybe the endpoint is hardcoded now?
                    // "API Key和 save按钮参考Theme主题选择的样式... 嵌套在大圆角矩形中"
                    // So we should have a card for API Key and Save button.

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key") },
                        visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showApiKey = !showApiKey }) {
                                Icon(
                                    if (showApiKey) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle API Key visibility"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        )
                    )

                    // Save Button
                    FilledTonalButton(
                        onClick = { onSaveConfig(endpoint, apiKey) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                    ) {
                        Text("Save Configuration")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AIConfigurationScreenPreview() {
    CAN301_CWTheme {
        AIConfigurationScreen(
            initialEndpoint = "https://api.example.com",
            initialApiKey = "sk-1234567890",
            onSaveConfig = { _, _ -> },
            onBackClick = {}
        )
    }
}
