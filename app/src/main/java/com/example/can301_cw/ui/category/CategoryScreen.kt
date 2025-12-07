package com.example.can301_cw.ui.category

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.can301_cw.data.MemoDao
import com.example.can301_cw.ui.theme.CAN301_CWTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    modifier: Modifier = Modifier,
    memoDao: MemoDao? = null,
    onTagClick: (String) -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    // Extract HSV from primary color to generate category colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(primaryColor.toArgb(), hsv)
    val hue = hsv[0]
    val baseSaturation = hsv[1]
    val baseBrightness = hsv[2]

    // Generate category colors dynamically
    val generateColor = { index: Int ->
        val colorFactors = listOf(
            1.0f to 1.0f,
            0.9f to 0.95f,
            0.8f to 0.98f,
            0.7f to 0.92f,
            1.0f to 0.88f,
            0.85f to 0.96f,
            0.95f to 0.90f,
            0.75f to 0.94f,
            0.9f to 0.91f,
            0.8f to 0.93f
        )
        val (satFactor, brightFactor) = if (index < colorFactors.size) colorFactors[index] else 1.0f to 1.0f
        Color.hsv(
            hue,
            (baseSaturation * satFactor).coerceIn(0f, 1f),
            (baseBrightness * brightFactor).coerceIn(0f, 1f)
        )
    }

    if (memoDao != null) {
        val viewModel: CategoryViewModel = viewModel(factory = CategoryViewModel.Factory(memoDao))
        val tagCategories by viewModel.tagCategories.collectAsState()

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
            if (tagCategories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tags yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = tagCategories,
                        key = { it.id }
                    ) { category ->
                        val colorIndex = tagCategories.indexOf(category) % 10
                        TaskCategoryCard(
                            category = category,
                            color = generateColor(colorIndex),
                            onClick = { onTagClick(category.name) }
                        )
                    }
                }
            }
        }
    } else {
        // Fallback for preview or when memoDao is not provided
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No data available",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

data class TaskTypeCategory(
    val name: String,
    val count: Int,
    val id: String = java.util.UUID.randomUUID().toString()
)

@Composable
fun TaskCategoryCard(
    category: TagCategory,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit = {}
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                color = color
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
