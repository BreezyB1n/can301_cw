package com.example.can301_cw.ui.profile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.can301_cw.data.AppDatabase
import com.example.can301_cw.data.MemoDao
import com.example.can301_cw.data.SettingsRepository
import com.example.can301_cw.model.UserStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val stats: UserStats = UserStats(),
    val isDarkModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val defaultRemindOffset: Int = 30,
    val isCalendarSyncEnabled: Boolean = false,
    val aiEndpoint: String = "",
    val aiApiKey: String = ""
)

class ProfileViewModel(
    private val memoDao: MemoDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val uiState: StateFlow<ProfileUiState> = combine(
        settingsRepository.darkModeEnabled,
        settingsRepository.notificationsEnabled,
        settingsRepository.defaultRemindOffsetMinutes,
        settingsRepository.calendarSyncEnabled,
        settingsRepository.aiEndpoint,
        settingsRepository.aiApiKey,
        memoDao.getAllMemos()
    ) { args: Array<Any> ->
        val darkMode = args[0] as Boolean
        val notifications = args[1] as Boolean
        val offset = args[2] as Int
        val calendarSync = args[3] as Boolean
        val endpoint = args[4] as String
        val apiKey = args[5] as String
        val memos = args[6] as List<*>
        
        ProfileUiState(
            stats = UserStats(
                pendingTasks = memos.size.toString(),
                completedTasks = "0",
                timeSaved = "0m"
            ),
            isDarkModeEnabled = darkMode,
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

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                val database = AppDatabase.getDatabase(application)
                val memoDao = database.memoDao()
                val settingsDao = database.settingsDao()
                val settingsRepository = SettingsRepository(settingsDao)
                return ProfileViewModel(memoDao, settingsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
