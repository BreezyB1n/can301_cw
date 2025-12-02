package com.example.can301_cw.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
            // Endpoint
            OutlinedTextField(
                value = endpoint,
                onValueChange = { endpoint = it },
                label = { Text("AI Endpoint URL") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )

            // API Key
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key") },
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showApiKey = !showApiKey }) {
                        Icon(
                            if (showApiKey) Icons.Filled.Check else Icons.Filled.Add, // Reusing existing icons as per original code
                            contentDescription = "Toggle API Key visibility"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                singleLine = true
            )

            // Save Button
            Button(
                onClick = { onSaveConfig(endpoint, apiKey) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Configuration")
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
