package com.example.can301_cw.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.can301_cw.ui.theme.AppTheme
import com.example.can301_cw.ui.theme.BluePrimary
import com.example.can301_cw.ui.theme.CAN301_CWTheme
import com.example.can301_cw.ui.theme.GreenPrimary
import com.example.can301_cw.ui.theme.YellowPrimary

@Composable
fun SettingsScreen(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "Theme",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ThemeOption(
            name = "Blue (Default)",
            color = BluePrimary,
            selected = currentTheme == AppTheme.Blue,
            onClick = { onThemeChange(AppTheme.Blue) }
        )

        ThemeOption(
            name = "Yellow",
            color = YellowPrimary,
            selected = currentTheme == AppTheme.Yellow,
            onClick = { onThemeChange(AppTheme.Yellow) }
        )

        ThemeOption(
            name = "Green",
            color = GreenPrimary,
            selected = currentTheme == AppTheme.Green,
            onClick = { onThemeChange(AppTheme.Green) }
        )
    }
}

@Composable
fun ThemeOption(
    name: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        
        // Color Preview Circle
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(text = name, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    CAN301_CWTheme {
        SettingsScreen(
            currentTheme = AppTheme.Blue,
            onThemeChange = {}
        )
    }
}
