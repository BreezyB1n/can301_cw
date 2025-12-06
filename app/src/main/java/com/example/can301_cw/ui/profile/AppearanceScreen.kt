package com.example.can301_cw.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
    onThemeSelected: (AppTheme) -> Unit,
    onCustomizeColorClick: () -> Unit,
    onDarkModeConfigSelected: (DarkModeConfig) -> Unit,
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
                modifier = Modifier.padding(bottom = 12.dp)
            )
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Dark Mode
            Text(
                text = "Dark Mode",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
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
                        .clickable { onDarkModeConfigSelected(config) }
                        .padding(vertical = 12.dp),
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
            onThemeSelected = {},
            onCustomizeColorClick = {},
            onDarkModeConfigSelected = {},
            onBackClick = {}
        )
    }
}
