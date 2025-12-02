package com.example.can301_cw.ui.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.can301_cw.ui.theme.CAN301_CWTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    // Extract HSV from primary color to generate category colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(primaryColor.toArgb(), hsv)
    val hue = hsv[0]
    val baseSaturation = hsv[1]
    val baseBrightness = hsv[2]

    // Generate 10 different shades keeping the same hue, clamped to valid range [0, 1]
    val categoryColors = listOf(
        Color.hsv(hue, baseSaturation.coerceIn(0f, 1f), baseBrightness.coerceIn(0f, 1f)),
        Color.hsv(hue, (baseSaturation * 0.9f).coerceIn(0f, 1f), (baseBrightness * 0.95f).coerceIn(0f, 1f)),
        Color.hsv(hue, (baseSaturation * 0.8f).coerceIn(0f, 1f), (baseBrightness * 0.98f).coerceIn(0f, 1f)),
        Color.hsv(hue, (baseSaturation * 0.7f).coerceIn(0f, 1f), (baseBrightness * 0.92f).coerceIn(0f, 1f)),
        Color.hsv(hue, (baseSaturation * 1.0f).coerceIn(0f, 1f), (baseBrightness * 0.88f).coerceIn(0f, 1f)),
        Color.hsv(hue, (baseSaturation * 0.85f).coerceIn(0f, 1f), (baseBrightness * 0.96f).coerceIn(0f, 1f)),
        Color.hsv(hue, (baseSaturation * 0.95f).coerceIn(0f, 1f), (baseBrightness * 0.90f).coerceIn(0f, 1f)),
        Color.hsv(hue, (baseSaturation * 0.75f).coerceIn(0f, 1f), (baseBrightness * 0.94f).coerceIn(0f, 1f)),
        Color.hsv(hue, (baseSaturation * 0.9f).coerceIn(0f, 1f), (baseBrightness * 0.91f).coerceIn(0f, 1f)),
        Color.hsv(hue, (baseSaturation * 0.8f).coerceIn(0f, 1f), (baseBrightness * 0.93f).coerceIn(0f, 1f))
    )

    // Task type categories with sample data
    val taskCategories = listOf(
        TaskTypeCategory("Coupons", "COUPON", 12, categoryColors[0]),
        TaskTypeCategory("Kids Toys", "TOY", 8, categoryColors[1]),
        TaskTypeCategory("Learning", "LEARNING", 15, categoryColors[2]),
        TaskTypeCategory("Safety", "SAFETY", 6, categoryColors[3]),
        TaskTypeCategory("Archery", "ARCHERY", 9, categoryColors[4]),
        TaskTypeCategory("Gesture Control", "GESTURE", 11, categoryColors[5]),
        TaskTypeCategory("User Interface", "UI", 7, categoryColors[6]),
        TaskTypeCategory("Research", "RESEARCH", 13, categoryColors[7]),
        TaskTypeCategory("Headphone", "HEADPHONE", 5, categoryColors[8]),
        TaskTypeCategory("Touch", "TOUCH", 10, categoryColors[9])
    )

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Category",
                        fontWeight = FontWeight.Bold
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                start = 16.dp,
                end = 16.dp
            )
        ) {
            items(
                items = taskCategories,
                key = { it.id }
            ) { category ->
                TaskCategoryCard(category)
            }
        }
    }
}

data class TaskTypeCategory(
    val name: String,
    val type: String,
    val count: Int,
    val color: Color,
    val id: java.util.UUID = java.util.UUID.randomUUID()
)

@Composable
fun TaskCategoryCard(category: TaskTypeCategory) {
    ListItem(
        modifier = Modifier.fillMaxWidth(),
        headlineContent = {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        leadingContent = {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(4.dp),
                color = category.color
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "#",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        fontSize = 18.sp
                    )
                }
            }
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = category.count.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 12.sp
                        )
                    }
                }
                Text(
                    text = ">",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    )
}

@Preview
@Composable
fun CategoryScreenPreview() {
    CAN301_CWTheme {
        CategoryScreen()
    }
}
