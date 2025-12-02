package com.example.can301_cw

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Date
import com.example.can301_cw.data.AppDatabase
import com.example.can301_cw.data.FakeMemoDao
import com.example.can301_cw.data.ImageStorageManager
import com.example.can301_cw.data.SettingsRepository
import com.example.can301_cw.model.MemoItem
import com.example.can301_cw.ui.category.CategoryScreen
import com.example.can301_cw.ui.home.HomeScreen
import com.example.can301_cw.ui.home.HomeViewModel
import com.example.can301_cw.ui.profile.DarkModeConfig
import com.example.can301_cw.ui.profile.ProfileScreen
import com.example.can301_cw.ui.theme.AppTheme
import com.example.can301_cw.ui.theme.CAN301_CWTheme

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val imageStorageManager by lazy { ImageStorageManager(this) }
    private val homeViewModel by viewModels<HomeViewModel> {
        HomeViewModel.Factory(database.memoDao(), imageStorageManager)
    }
    private val settingsRepository by lazy { SettingsRepository(database.settingsDao()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        enableEdgeToEdge()
        setContent {
            val themeColorName by settingsRepository.themeColor.collectAsState(initial = "Blue")
            val darkModeConfigName by settingsRepository.darkModeConfig.collectAsState(initial = "FOLLOW_SYSTEM")
            val lastSystemDarkMode by settingsRepository.lastSystemDarkMode.collectAsState(initial = null)
            val customThemeColorValue by settingsRepository.customThemeColor.collectAsState(initial = 0L)

            val isSystemDark = isSystemInDarkTheme()

            LaunchedEffect(isSystemDark, lastSystemDarkMode) {
                if (lastSystemDarkMode != null && lastSystemDarkMode != isSystemDark) {
                    // System dark mode changed
                    // Reset config to FOLLOW_SYSTEM
                    settingsRepository.setDarkModeConfig(DarkModeConfig.FOLLOW_SYSTEM.name)
                }
                // Update last known state
                if (lastSystemDarkMode != isSystemDark) {
                    settingsRepository.setLastSystemDarkMode(isSystemDark)
                }
            }

            val appTheme = try {
                AppTheme.valueOf(themeColorName)
            } catch (e: IllegalArgumentException) {
                AppTheme.Blue
            }

            val darkModeConfig = try {
                DarkModeConfig.valueOf(darkModeConfigName)
            } catch (e: IllegalArgumentException) {
                DarkModeConfig.FOLLOW_SYSTEM
            }

            val darkTheme = when(darkModeConfig) {
                DarkModeConfig.FOLLOW_SYSTEM -> isSystemDark
                DarkModeConfig.LIGHT -> false
                DarkModeConfig.DARK -> true
            }

            // Convert Long to Color for custom theme
            val customColor = if (customThemeColorValue != 0L) {
                try {
                    androidx.compose.ui.graphics.Color(customThemeColorValue.toULong())
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }

            CAN301_CWTheme(appTheme = appTheme, customPrimaryColor = customColor, darkTheme = darkTheme) {
                MainScreen(
                    homeViewModel = homeViewModel,
                    currentTheme = appTheme,
                    onThemeChange = { newTheme ->
                        lifecycleScope.launch {
                            settingsRepository.setThemeColor(newTheme.name)
                        }
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            @Suppress("DEPRECATION")
            (intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri)?.let { imageUri ->
                try {
                    val inputStream = contentResolver.openInputStream(imageUri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val newItem = MemoItem(
                            createdAt = Date(),
                            title = "Shared Image",
                            recognizedText = "Shared from external app",
                            tags = mutableListOf("Shared")
                        ).apply {
                            imageData = bytes
                        }
                        homeViewModel.addMemoItem(newItem)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

data class BottomNavItem(
    val name: String,
    val icon: ImageVector
)

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    currentTheme: AppTheme = AppTheme.Blue,
    onThemeChange: (AppTheme) -> Unit = {}
) {
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    val items = listOf(
        BottomNavItem("Memo", Icons.Filled.Home),
        BottomNavItem("Intents", Icons.Filled.DateRange),
        BottomNavItem("Category", Icons.AutoMirrored.Filled.List),
        BottomNavItem("Account", Icons.Filled.AccountCircle)
    )

    val navigationBarHeight = 60.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Scaffold(
        Modifier.fillMaxSize(), bottomBar = {
            NavigationBar(
                modifier = Modifier.height(navigationBarHeight),
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.name) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = if (selectedItem == 0 || selectedItem == 3) Modifier.padding(bottom = innerPadding.calculateBottomPadding()) else Modifier.padding(innerPadding)) {
            when (selectedItem) {
                0 -> HomeScreen(viewModel = homeViewModel)
                2 -> CategoryScreen()
                3 -> ProfileScreen()
                else -> ContentScreen(
                    text = "This is ${items[selectedItem].name} Screen"
                )
            }
        }
    }
}

@Composable
fun ContentScreen(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val context = androidx.compose.ui.platform.LocalContext.current
    CAN301_CWTheme {
        MainScreen(homeViewModel = HomeViewModel(FakeMemoDao(), ImageStorageManager(context)))
    }
}
