package com.example.can301_cw

import android.annotation.SuppressLint
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

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.can301_cw.ui.add.AddMemoScreen
import com.example.can301_cw.ui.add.AddMemoViewModel
import com.example.can301_cw.ui.detail.MemoDetailScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Application
import androidx.compose.ui.platform.LocalContext

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.example.can301_cw.data.UserRepository
import com.example.can301_cw.ui.auth.AuthViewModel
import com.example.can301_cw.ui.auth.LoginScreen
import com.example.can301_cw.ui.auth.RegisterScreen
import com.example.can301_cw.ui.profile.ProfileViewModel

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val imageStorageManager by lazy { ImageStorageManager(this) }
    private val homeViewModel by viewModels<HomeViewModel> {
        HomeViewModel.Factory(database.memoDao(), imageStorageManager)
    }
    private val settingsRepository by lazy { SettingsRepository(database.settingsDao()) }

    // 【新增 1】初始化 UserRepository
    private val userRepository by lazy { UserRepository(database.userDao()) }

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
                    settingsRepository.setDarkModeConfig(DarkModeConfig.FOLLOW_SYSTEM.name)
                }
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
                val navController = rememberNavController()

                // 【修改 2】将 startDestination 从 "main" 改为 "login"
                NavHost(navController = navController, startDestination = "login") {

                    // 【新增 3】登录页面路由
                    composable("login") {
                        val authViewModel: AuthViewModel = viewModel(
                            factory = AuthViewModel.Factory(userRepository)
                        )
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = {
                                // 登录成功后，跳转到主页，并清除登录页面的堆栈
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate("register")
                            }
                        )
                    }

                    // 【新增 4】注册页面路由
                    composable("register") {
                        val authViewModel: AuthViewModel = viewModel(
                            factory = AuthViewModel.Factory(userRepository)
                        )
                        RegisterScreen(
                            viewModel = authViewModel,
                            onRegisterSuccess = {
                                // 注册成功（并自动登录）后，跳转到主页
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("main") {
                        MainScreen(
                            homeViewModel = homeViewModel,
                            userRepository = userRepository,
                            currentTheme = appTheme,
                            onThemeChange = { newTheme ->
                                lifecycleScope.launch {
                                    settingsRepository.setThemeColor(newTheme.name)
                                }
                            },
                            onAddMemoClick = { navController.navigate("add_memo") },
                            onMemoClick = { navController.navigate("memo_detail") },
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(
                        route = "memo_detail",
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(300)
                            )
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(300)
                            )
                        },
                        popEnterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(300)
                            )
                        },
                        popExitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(300)
                            )
                        }
                    ) {
                        MemoDetailScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "add_memo",
                        enterTransition = {
                            slideInVertically(
                                initialOffsetY = { it }, // Slide in from bottom
                                animationSpec = tween(300)
                            )
                        },
                        exitTransition = {
                            slideOutVertically(
                                targetOffsetY = { it }, // Slide out to bottom
                                animationSpec = tween(300)
                            )
                        },
                        popEnterTransition = {
                            // When coming back to this screen (not applicable here as we pop it)
                            null
                        },
                        popExitTransition = {
                             slideOutVertically(
                                targetOffsetY = { it }, // Slide out to bottom
                                animationSpec = tween(300)
                            )
                        }
                    ) {
                        val context = LocalContext.current
                        val application = context.applicationContext as Application

                        val addMemoViewModel: AddMemoViewModel = viewModel(
                            factory = AddMemoViewModel.Factory(
                                application,
                                database.memoDao(),
                                imageStorageManager
                            )
                        )

                        AddMemoScreen(
                            viewModel = addMemoViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
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
    userRepository: UserRepository,
    currentTheme: AppTheme = AppTheme.Blue,
    onThemeChange: (AppTheme) -> Unit = {},
    onAddMemoClick: () -> Unit = {}, // Pass navigation callback
    onMemoClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    val items = listOf(
        BottomNavItem("Memo", Icons.Filled.Home),
        BottomNavItem("Intents", Icons.Filled.DateRange),
        BottomNavItem("Category", Icons.AutoMirrored.Filled.List),
        BottomNavItem("Account", Icons.Filled.AccountCircle)
    )

    val navigationBarHeight = 52.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

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
        Box(modifier = if (selectedItem == 0 || selectedItem == 2 || selectedItem == 3) Modifier.padding(bottom = innerPadding.calculateBottomPadding()) else Modifier.padding(innerPadding)) {
            when (selectedItem) {
                0 -> HomeScreen(
                    viewModel = homeViewModel,
                    onAddMemoClick = onAddMemoClick, // Pass it down
                    onMemoClick = onMemoClick
                )
                2 -> CategoryScreen()
                3 -> {
                    val context = LocalContext.current
                    val application = context.applicationContext as Application

                    val profileViewModel: ProfileViewModel = viewModel(
                        factory = ProfileViewModel.Factory(
                            application = application,
                            userRepository = userRepository
                        )
                    )
                    ProfileScreen(
                        viewModel = profileViewModel,
                        onLogout = onLogout
                    )
                }
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
