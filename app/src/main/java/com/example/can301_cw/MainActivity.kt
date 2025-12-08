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
import com.example.can301_cw.data.ImageStorageManager
import com.example.can301_cw.data.SettingsRepository
import com.example.can301_cw.model.MemoItem
import com.example.can301_cw.ui.category.CategoryScreen
import com.example.can301_cw.ui.category.TagDetailScreen
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
import com.example.can301_cw.notification.NotificationHelper
import com.example.can301_cw.notification.ReminderScheduler

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
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.can301_cw.ui.detail.MemoDetailViewModel

import com.example.can301_cw.ui.schedule.ScheduleScreen
import com.example.can301_cw.ui.schedule.ScheduleViewModel

import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val imageStorageManager by lazy { ImageStorageManager(this) }
    private val reminderScheduler by lazy { ReminderScheduler(this) }
    private val settingsRepository by lazy { SettingsRepository(database.settingsDao()) }
    private val homeViewModel by viewModels<HomeViewModel> {
        HomeViewModel.Factory(database.memoDao(), imageStorageManager, settingsRepository)
    }

    private val userRepository by lazy { UserRepository(database.userDao(), database.settingsDao()) }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            android.widget.Toast.makeText(
                this,
                "Notification permission granted. Reminders will work normally.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } else {
            android.widget.Toast.makeText(
                this,
                "Notification permission denied. You will not receive reminders.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    private val pendingMemoId = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 创建通知渠道
        NotificationHelper.createNotificationChannel(this)
        
        // Request Notification Permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Restore user session
        lifecycleScope.launch {
            userRepository.restoreSession()
        }

        handleIntent(intent)

        enableEdgeToEdge()
        setContent {
            val themeColorName by settingsRepository.themeColor.collectAsState(initial = "Blue")
            val darkModeConfigName by settingsRepository.darkModeConfig.collectAsState(initial = "FOLLOW_SYSTEM")
            val lastSystemDarkMode by settingsRepository.lastSystemDarkMode.collectAsState(initial = null)
            val customThemeColorValue by settingsRepository.customThemeColor.collectAsState(initial = 0L)
            val showTabLabels by settingsRepository.showTabLabels.collectAsState(initial = false)

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

                // Handle pending navigation from notification
                LaunchedEffect(pendingMemoId.value) {
                    pendingMemoId.value?.let { memoId ->
                        navController.navigate("memo_detail/$memoId")
                        pendingMemoId.value = null // Reset
                    }
                }

                NavHost(navController = navController, startDestination = "main") {

                    composable(
                        route = "login",
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
                             null
                        },
                        popExitTransition = {
                             slideOutVertically(
                                targetOffsetY = { it }, // Slide out to bottom
                                animationSpec = tween(300)
                            )
                        }
                    ) {
                        val authViewModel: AuthViewModel = viewModel(
                            factory = AuthViewModel.Factory(userRepository)
                        )
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = {
                                // Login success, close login screen
                                navController.popBackStack()
                            },
                            onClose = {
                                navController.popBackStack()
                            },
                            onNavigateToRegister = {
                                navController.navigate("register")
                            }
                        )
                    }

                    // 【新增 4】注册页面路由
                    composable(
                        route = "register",
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
                        val authViewModel: AuthViewModel = viewModel(
                            factory = AuthViewModel.Factory(userRepository)
                        )
                        RegisterScreen(
                            viewModel = authViewModel,
                            onRegisterSuccess = {
                                // Register success (and auto login), close screens
                                navController.popBackStack("main", inclusive = false)
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
                            database = database,
                            navController = navController,
                            currentTheme = appTheme,
                            showTabLabels = showTabLabels,
                            onThemeChange = { newTheme ->
                                lifecycleScope.launch {
                                    settingsRepository.setThemeColor(newTheme.name)
                                }
                            },
                            onAddMemoClick = { navController.navigate("add_memo") },
                            onMemoClick = { memoId -> navController.navigate("memo_detail/$memoId") },
                            onLoginClick = { navController.navigate("login") },
                            onLogout = {
                                // Logout is handled in ProfileScreen/ViewModel, we might want to refresh or do nothing
                                // Since user state is observed, UI updates automatically.
                            }
                        )
                    }

                    composable(
                        route = "memo_detail/{memoId}",
                        arguments = listOf(navArgument("memoId") { type = NavType.StringType }),
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
                    ) { backStackEntry ->
                        val memoId = backStackEntry.arguments?.getString("memoId") ?: return@composable
                        val viewModel: MemoDetailViewModel = viewModel(
                            factory = MemoDetailViewModel.Factory(database.memoDao(), imageStorageManager, memoId, reminderScheduler, settingsRepository)
                        )
                        MemoDetailScreen(
                            viewModel = viewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "tag_detail/{tag}",
                        arguments = listOf(navArgument("tag") { type = NavType.StringType }),
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
                    ) { backStackEntry ->
                        val tagEncoded = backStackEntry.arguments?.getString("tag") ?: return@composable
                        val tag = java.net.URLDecoder.decode(tagEncoded, "UTF-8")
                        TagDetailScreen(
                            tag = tag,
                            memoDao = database.memoDao(),
                            imageStorageManager = imageStorageManager,
                            onBackClick = { navController.popBackStack() },
                            onMemoClick = { memoId -> navController.navigate("memo_detail/$memoId") }
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
                                imageStorageManager,
                                reminderScheduler
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
        if (intent == null) return

        // Handle Notification Click
        if (intent.hasExtra("memo_id")) {
            val memoId = intent.getStringExtra("memo_id")
            if (memoId != null) {
                // Wait for composition to be ready or handle navigation differently?
                // Since this is Activity scope, we might not have access to navController immediately if app is starting cold.
                // But `handleIntent` is called from `onCreate` (cold start) and `onNewIntent` (warm start).
                // However, navController is created inside setContent.
                // We should store this ID and handle it inside the Composable or use a specific Intent action.
                
                // Let's use a mutableState to trigger navigation inside MainScreen/NavHost
                pendingMemoId.value = memoId
            }
        }

        if (intent.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
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
                            tags = mutableListOf()
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
    database: AppDatabase,
    navController: androidx.navigation.NavController,
    currentTheme: AppTheme = AppTheme.Blue,
    showTabLabels: Boolean = false,
    onThemeChange: (AppTheme) -> Unit = {},
    onAddMemoClick: () -> Unit = {}, // Pass navigation callback
    onMemoClick: (String) -> Unit = {},
    onLoginClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    val items = listOf(
        BottomNavItem("Memo", Icons.Filled.Home),
        BottomNavItem("Intents", Icons.Filled.DateRange),
        BottomNavItem("Category", Icons.AutoMirrored.Filled.List),
        BottomNavItem("Account", Icons.Filled.AccountCircle)
    )

    val navigationBarHeight = (if (showTabLabels) 80.dp else 52.dp) + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Scaffold(
        Modifier.fillMaxSize(), bottomBar = {
            NavigationBar(
                modifier = Modifier.height(navigationBarHeight),
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.name) },
                        label = if (showTabLabels) { { Text(item.name) } } else null,
                        alwaysShowLabel = showTabLabels,
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
            when (selectedItem) {
                0 -> HomeScreen(
                    viewModel = homeViewModel,
                    onAddMemoClick = onAddMemoClick, // Pass it down
                    onMemoClick = onMemoClick
                )
                1 -> {
                    val context = LocalContext.current
                    val application = context.applicationContext as Application

                    val scheduleViewModel: ScheduleViewModel = viewModel(
                        factory = ScheduleViewModel.Factory(database.memoDao(), ReminderScheduler(context))
                    )

                    ScheduleScreen(
                        viewModel = scheduleViewModel,
                        onMemoClick = onMemoClick
                    )
                }
                2 -> CategoryScreen(
                    memoDao = database.memoDao(),
                    onTagClick = { tag -> navController.navigate("tag_detail/${java.net.URLEncoder.encode(tag, "UTF-8")}") }
                )
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
                        onLogout = onLogout,
                        onLoginClick = onLoginClick
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
