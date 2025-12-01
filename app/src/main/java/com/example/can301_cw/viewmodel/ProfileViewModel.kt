package com.example.can301_cw.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.can301_cw.data.AppDatabase
import com.example.can301_cw.data.SettingsRepository
import com.example.can301_cw.data.TaskDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserStats(
    val pendingTasks: String,
    val completedTasks: String,
    val timeSaved: String
)

data class ProfileUiState(
    val notificationsEnabled: Boolean = true,
    val defaultRemindOffset: Int = 30,
    val aiEndpoint: String = "",
    val aiApiKey: String = "",
    val isCalendarSyncEnabled: Boolean = false,
    val isDarkModeEnabled: Boolean = false,
    val stats: UserStats = UserStats("0", "0", "0h")
)

class ProfileViewModel(
    private val settingsRepository: SettingsRepository,
    private val taskDao: TaskDao
) : ViewModel() {

    // Mock Data
    private val mockStats = UserStats(
        pendingTasks = "12",
        completedTasks = "45",
        timeSaved = "2.5h"
    )

    val uiState: StateFlow<ProfileUiState> = combine(
        settingsRepository.notificationsEnabled,
        settingsRepository.defaultRemindOffsetMinutes,
        settingsRepository.aiEndpoint,
        settingsRepository.aiApiKey,
        settingsRepository.calendarSyncEnabled,
        settingsRepository.darkModeEnabled
    ) { args: Array<Any> ->
        val notifications = args[0] as Boolean
        val offset = args[1] as Int
        val endpoint = args[2] as String
        val apiKey = args[3] as String
        val calendarSync = args[4] as Boolean
        val darkMode = args[5] as Boolean

        ProfileUiState(
            notificationsEnabled = notifications,
            defaultRemindOffset = offset,
            aiEndpoint = endpoint,
            aiApiKey = apiKey,
            isCalendarSyncEnabled = calendarSync,
            isDarkModeEnabled = darkMode,
            stats = mockStats
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }

    fun setDefaultRemindOffset(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultRemindOffsetMinutes(minutes)
        }
    }

    fun updateAiConfig(endpoint: String, apiKey: String) {
        viewModelScope.launch {
            settingsRepository.setAiEndpoint(endpoint)
            settingsRepository.setAiApiKey(apiKey)
        }
    }

    fun setCalendarSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCalendarSyncEnabled(enabled)
        }
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkModeEnabled(enabled)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            taskDao.deleteAll()
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val database = AppDatabase.getDatabase(application)
                val settingsRepository = SettingsRepository(database.settingsDao())
                ProfileViewModel(
                    settingsRepository = settingsRepository,
                    taskDao = database.taskDao()
                )
            }
        }
    }
}

