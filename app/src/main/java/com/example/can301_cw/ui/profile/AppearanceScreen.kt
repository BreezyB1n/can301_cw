package com.example.can301_cw.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.can301_cw.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    currentTheme: AppTheme,
    customThemeColor: Long,
    darkModeConfig: DarkModeConfig,
    showTabLabels: Boolean,
    onThemeSelected: (AppTheme) -> Unit,
    onCustomizeColorClick: () -> Unit,
    onDarkModeConfigSelected: (DarkModeConfig) -> Unit,
    onShowTabLabelsChange: (Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appearance") },
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
            // Theme Color
            Text(
                text = "Theme Color",
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
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        val themes = listOf(
                            Triple(AppTheme.Blue, BluePrimary, "Blue"),
                            Triple(AppTheme.Yellow, YellowPrimary, "Yellow"),
                            Triple(AppTheme.Green, GreenPrimary, "Green"),
                            Triple(AppTheme.Purple, PurplePrimary, "Purple"),
                            Triple(AppTheme.CherryBlossom, CherryBlossomPrimary, "Cherry"),
                            // Triple(AppTheme.StoneropGreen, StoneropGreenPrimary, "Stone")
                        )

                        themes.forEach { (theme, color, label) ->
                            ThemeColorOption(
                                color = color,
                                label = label,
                                selected = currentTheme == theme,
                                onClick = { onThemeSelected(theme) }
                            )
                        }

                        // Custom Theme Option
                        val isCustomSet = customThemeColor != 0L
                        val customColor = if (isCustomSet) {
                            try {
                                Color(customThemeColor.toULong())
                            } catch (e: Exception) {
                                Color.Blue
                            }
                        } else {
                            Color.LightGray
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                if (isCustomSet) {
                                    onThemeSelected(AppTheme.Custom)
                                } else {
                                    onCustomizeColorClick()
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(customColor)
                                    .border(
                                        width = 2.dp,
                                        color = if (isCustomSet)
                                            customColor.copy(alpha = 0.6f).compositeOver(Color.Black)
                                        else Color.Gray,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCustomSet) {
                                    if (currentTheme == AppTheme.Custom) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "?",
                                        color = Color.DarkGray,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Custom",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Custom Color Button
                    FilledTonalButton(
                        onClick = onCustomizeColorClick,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text("Customize Color")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dark Mode
            Text(
                text = "Dark Mode",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            val darkModeOptions = listOf(
                DarkModeConfig.FOLLOW_SYSTEM to "Follow System",
                DarkModeConfig.LIGHT to "Light",
                DarkModeConfig.DARK to "Dark"
            )

            darkModeOptions.forEachIndexed { index, (config, label) ->
                val shape = when {
                    darkModeOptions.size == 1 -> RoundedCornerShape(24.dp)
                    index == 0 -> RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 4.dp
                    )
                    index == darkModeOptions.size - 1 -> RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 4.dp,
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp
                    )
                    else -> RoundedCornerShape(4.dp)
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDarkModeConfigSelected(config) },
                    shape = shape,
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        if (darkModeConfig == config) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                if (index < darkModeOptions.size - 1) {
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Custom Settings
            Text(
                text = "Custom",
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Show Tab Labels",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = showTabLabels,
                        onCheckedChange = onShowTabLabelsChange
                    )
                }
            }
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
                    color = color.copy(alpha = 0.6f).compositeOver(Color.Black),
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
fun AppearanceScreenPreview() {
    CAN301_CWTheme {
        AppearanceScreen(
            currentTheme = AppTheme.Blue,
            customThemeColor = 0L,
            darkModeConfig = DarkModeConfig.FOLLOW_SYSTEM,
            showTabLabels = false,
            onThemeSelected = {},
            onCustomizeColorClick = {},
            onDarkModeConfigSelected = {},
            onShowTabLabelsChange = {},
            onBackClick = {}
        )
    }
}
