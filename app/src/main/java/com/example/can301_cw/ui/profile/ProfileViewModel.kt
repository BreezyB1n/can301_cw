package com.example.can301_cw.ui.profile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.can301_cw.data.AppDatabase
import com.example.can301_cw.data.MemoDao
import com.example.can301_cw.data.SettingsRepository
import com.example.can301_cw.data.UserRepository
import com.example.can301_cw.model.User
import com.example.can301_cw.model.UserStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.can301_cw.ui.theme.AppTheme

enum class DarkModeConfig {
    FOLLOW_SYSTEM, LIGHT, DARK
}

data class ProfileUiState(
    val user: User? = null,
    val stats: UserStats = UserStats(),
    val isDarkModeEnabled: Boolean = false, // Deprecated in favor of darkModeConfig
    val darkModeConfig: DarkModeConfig = DarkModeConfig.FOLLOW_SYSTEM,
    val currentTheme: AppTheme = AppTheme.Blue,
    val customThemeColor: Long = 0L, // Store as ARGB Long
    val notificationsEnabled: Boolean = true,
    val defaultRemindOffset: Int = 30,
    val isCalendarSyncEnabled: Boolean = false,
    val aiEndpoint: String = "",
    val aiApiKey: String = ""
)

class ProfileViewModel(
    private val memoDao: MemoDao,
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    val uiState: StateFlow<ProfileUiState> = combine(
        settingsRepository.darkModeEnabled,
        settingsRepository.notificationsEnabled,
        settingsRepository.defaultRemindOffsetMinutes,
        settingsRepository.calendarSyncEnabled,
        settingsRepository.aiEndpoint,
        settingsRepository.aiApiKey,
        memoDao.getAllMemos(),
        settingsRepository.themeColor,
        settingsRepository.darkModeConfig,
        settingsRepository.customThemeColor,
        userRepository.currentUser
    ) { args: Array<Any?> ->
        val darkMode = args[0] as Boolean
        val notifications = args[1] as Boolean
        val offset = args[2] as Int
        val calendarSync = args[3] as Boolean
        val endpoint = args[4] as String
        val apiKey = args[5] as String
        val memos = args[6] as List<*>
        val themeColorName = args[7] as String
        val darkModeConfigName = args[8] as String
        val customThemeColorValue = args[9] as Long
        val currentUser = args[10] as? User
        
        val currentTheme = try {
            AppTheme.valueOf(themeColorName)
        } catch (e: IllegalArgumentException) {
            AppTheme.Blue
        }

        val darkModeConfig = try {
            DarkModeConfig.valueOf(darkModeConfigName)
        } catch (e: IllegalArgumentException) {
            DarkModeConfig.FOLLOW_SYSTEM
        }
        
        ProfileUiState(
            user = currentUser,
            stats = UserStats(
                pendingTasks = memos.size.toString(),
                completedTasks = "0",
                savedInformation = "0"
            ),
            isDarkModeEnabled = darkMode,
            darkModeConfig = darkModeConfig,
            currentTheme = currentTheme,
            customThemeColor = customThemeColorValue,
            notificationsEnabled = notifications,
            defaultRemindOffset = offset,
            isCalendarSyncEnabled = calendarSync,
            aiEndpoint = endpoint,
            aiApiKey = apiKey
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            memoDao.deleteAll()
        }
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setDarkModeEnabled(enabled)
        }
    }
    
    fun setTheme(theme: AppTheme) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setThemeColor(theme.name)
        }
    }

    fun setCustomTheme(colorValue: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            // First save the custom color
            settingsRepository.setCustomThemeColor(colorValue)
            // Then switch to custom theme
            settingsRepository.setThemeColor(AppTheme.Custom.name)
        }
    }

    fun setDarkModeConfig(config: DarkModeConfig) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setDarkModeConfig(config.name)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }

    fun setCalendarSyncEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setCalendarSyncEnabled(enabled)
        }
    }

    fun updateAiConfig(endpoint: String, apiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setAiEndpoint(endpoint)
            settingsRepository.setAiApiKey(apiKey)
        }
    }

    fun updateAvatar(imagePath: String) {
        val currentUser = uiState.value.user ?: return
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.updateUser(currentUser.copy(avatarPath = imagePath))
        }
    }

    fun updateUsername(newUsername: String) {
        val currentUser = uiState.value.user ?: return
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.updateUser(currentUser.copy(username = newUsername))
        }
    }

    fun updatePassword(oldPassword: String, newPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUser = uiState.value.user ?: return
        viewModelScope.launch(Dispatchers.IO) {
            if (userRepository.verifyPassword(oldPassword, currentUser.password)) {
                // Manually hash the new password as updateUser expects a full User object.
                // But wait, UserRepository.updateUser just saves the user.
                // We need to re-hash the password. UserRepository.register hashes it, but updateUser doesn't seem to expose hashing logic directly unless we move hashPassword to public or duplicate it.
                // Let's modify UserRepository to support password update or expose hashing.
                // Or better, add updatePassword in UserRepository.
                val success = userRepository.updatePassword(currentUser, newPassword)
                if (success) {
                    launch(Dispatchers.Main) { onSuccess() }
                } else {
                     launch(Dispatchers.Main) { onError("Failed to update password") }
                }
            } else {
                launch(Dispatchers.Main) { onError("Incorrect old password") }
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.logout()
        }
    }

    class Factory(
        private val application: Application,
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                val database = AppDatabase.getDatabase(application)
                val memoDao = database.memoDao()
                val settingsDao = database.settingsDao()
                val settingsRepository = SettingsRepository(settingsDao)
                return ProfileViewModel(memoDao, settingsRepository, userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
