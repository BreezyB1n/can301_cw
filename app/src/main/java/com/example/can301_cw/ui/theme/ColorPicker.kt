package com.example.can301_cw.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun CustomColorPickerDialog(
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit,
    initialColor: Color = Color.Blue
) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedColor by remember { mutableStateOf(initialColor) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Custom Color",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tab Navigation
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth(),
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.PrimaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                width = Dp.Unspecified,
                                height = 3.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { 
                            Text(
                                "Grid",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { 
                            Text(
                                "Spectrum",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { 
                            Text(
                                "Sliders",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }

                // Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        0 -> ColorGridTab(
                            selectedColor = selectedColor,
                            onColorSelected = { selectedColor = it }
                        )
                        1 -> ColorSpectrumTab(
                            selectedColor = selectedColor,
                            onColorSelected = { selectedColor = it }
                        )
                        2 -> ColorSlidersTab(
                            selectedColor = selectedColor,
                            onColorSelected = { selectedColor = it }
                        )
                    }
                }

                // Preview and Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // Color Preview
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Preview:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(selectedColor)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                    }

                    // Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = { onColorSelected(selectedColor) },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorGridTab(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp)
    ) {
        val columns = 10
        val rows = 10
        
        // Adaptive cell size based on screen width
        val containerWidth = 360.dp - 16.dp // Account for padding (8dp * 2)
        val cellSize = containerWidth / columns
        
        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cellSize),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                for (col in 0 until columns) {
                    val hue = if (row == 0) {
                        0f  // First row: grayscale
                    } else {
                        ((col.toFloat() / columns) * 360f)  // Other rows: hue progression
                    }
                    
                    val saturation = if (row == 0) {
                        0f  // First row: no saturation (white to black)
                    } else {
                        1f - (row.toFloat() - 1f) / (rows - 1f)  // Saturation decreases downward
                    }
                    
                    val brightness = if (row == 0) {
                        1f - (col.toFloat() / columns)  // First row: white to black
                    } else {
                        1f  // Other rows: full brightness
                    }
                    
                    val color = Color.hsv(hue, saturation, brightness)
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(color)
                            .border(
                                width = if (selectedColor == color) 1.5.dp else 0.5.dp,
                                color = if (selectedColor == color)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                            .clickable { onColorSelected(color) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSpectrumTab(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    var hue by remember { mutableStateOf(0f) }
    var saturation by remember { mutableStateOf(1f) }
    var brightness by remember { mutableStateOf(1f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hue Spectrum Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                for (i in 0..359) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.hsv(i.toFloat(), 1f, 1f))
                            .clickable {
                                hue = i.toFloat()
                                onColorSelected(
                                    Color.hsv(hue, saturation, brightness)
                                )
                            }
                    )
                }
            }
        }

        // Saturation Slider
        SliderWithLabel(
            value = saturation,
            onValueChange = {
                saturation = it
                onColorSelected(
                    Color.hsv(hue, saturation, brightness)
                )
            },
            label = "Saturation: ${(saturation * 100).toInt()}%"
        )

        // Brightness Slider
        SliderWithLabel(
            value = brightness,
            onValueChange = {
                brightness = it
                onColorSelected(
                    Color.hsv(hue, saturation, brightness)
                )
            },
            label = "Brightness: ${(brightness * 100).toInt()}%"
        )

        // Current Color Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.hsv(hue, saturation, brightness))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp)
                )
        )
    }
}

@Composable
private fun ColorSlidersTab(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(selectedColor.toArgb(), hsv)

    var hue by remember { mutableStateOf(hsv[0]) }
    var saturation by remember { mutableStateOf(hsv[1] / 100f) }
    var brightness by remember { mutableStateOf(hsv[2] / 100f) }
    var alpha by remember { mutableStateOf(selectedColor.alpha) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hue Slider
        SliderWithLabel(
            value = hue,
            onValueChange = {
                hue = it
                onColorSelected(
                    Color.hsv(hue, saturation, brightness).copy(alpha = alpha)
                )
            },
            valueRange = 0f..360f,
            label = "Hue: ${hue.toInt()}°"
        )

        // Saturation Slider
        SliderWithLabel(
            value = saturation,
            onValueChange = {
                saturation = it
                onColorSelected(
                    Color.hsv(hue, saturation, brightness).copy(alpha = alpha)
                )
            },
            label = "Saturation: ${(saturation * 100).toInt()}%"
        )

        // Brightness Slider
        SliderWithLabel(
            value = brightness,
            onValueChange = {
                brightness = it
                onColorSelected(
                    Color.hsv(hue, saturation, brightness).copy(alpha = alpha)
                )
            },
            label = "Brightness: ${(brightness * 100).toInt()}%"
        )

        // Alpha Slider
        SliderWithLabel(
            value = alpha,
            onValueChange = {
                alpha = it
                onColorSelected(
                    Color.hsv(hue, saturation, brightness).copy(alpha = alpha)
                )
            },
            label = "Alpha: ${(alpha * 100).toInt()}%"
        )

        // Color Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.hsv(hue, saturation, brightness).copy(alpha = alpha))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Composable
private fun SliderWithLabel(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
