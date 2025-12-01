package com.example.can301_cw.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val settingsDao: SettingsDao) {

    companion object {
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_DEFAULT_REMIND_OFFSET = "default_remind_offset_minutes"
        const val KEY_AI_ENDPOINT = "ai_endpoint_url"
        const val KEY_AI_API_KEY = "ai_api_key"
        const val KEY_CALENDAR_SYNC_ENABLED = "calendar_sync_enabled"
        const val KEY_DARK_MODE_ENABLED = "dark_mode_enabled" // Deprecated, use KEY_DARK_MODE_CONFIG
        const val KEY_THEME_COLOR = "theme_color"
        const val KEY_DARK_MODE_CONFIG = "dark_mode_config"
        const val KEY_LAST_SYSTEM_DARK_MODE = "last_system_dark_mode"
    }

    val notificationsEnabled: Flow<Boolean> = settingsDao.getValueFlow(KEY_NOTIFICATIONS_ENABLED)
        .map { it?.toBoolean() ?: true }

    val defaultRemindOffsetMinutes: Flow<Int> = settingsDao.getValueFlow(KEY_DEFAULT_REMIND_OFFSET)
        .map { it?.toIntOrNull() ?: 30 }

    val aiEndpoint: Flow<String> = settingsDao.getValueFlow(KEY_AI_ENDPOINT)
        .map { it ?: "https://api.example.com/analyze-image" }

    val aiApiKey: Flow<String> = settingsDao.getValueFlow(KEY_AI_API_KEY)
        .map { it ?: "" }

    val calendarSyncEnabled: Flow<Boolean> = settingsDao.getValueFlow(KEY_CALENDAR_SYNC_ENABLED)
        .map { it?.toBoolean() ?: false }

    val darkModeEnabled: Flow<Boolean> = settingsDao.getValueFlow(KEY_DARK_MODE_ENABLED)
        .map { it?.toBoolean() ?: false }

    val themeColor: Flow<String> = settingsDao.getValueFlow(KEY_THEME_COLOR)
        .map { it ?: "Blue" }

    val darkModeConfig: Flow<String> = settingsDao.getValueFlow(KEY_DARK_MODE_CONFIG)
        .map { it ?: "FOLLOW_SYSTEM" }

    val lastSystemDarkMode: Flow<Boolean?> = settingsDao.getValueFlow(KEY_LAST_SYSTEM_DARK_MODE)
        .map { it?.toBoolean() }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        settingsDao.insert(SettingsEntity(KEY_NOTIFICATIONS_ENABLED, enabled.toString()))
    }

    suspend fun setDefaultRemindOffsetMinutes(minutes: Int) {
        settingsDao.insert(SettingsEntity(KEY_DEFAULT_REMIND_OFFSET, minutes.toString()))
    }

    suspend fun setAiEndpoint(url: String) {
        settingsDao.insert(SettingsEntity(KEY_AI_ENDPOINT, url))
    }

    suspend fun setAiApiKey(key: String) {
        settingsDao.insert(SettingsEntity(KEY_AI_API_KEY, key))
    }

    suspend fun setCalendarSyncEnabled(enabled: Boolean) {
        settingsDao.insert(SettingsEntity(KEY_CALENDAR_SYNC_ENABLED, enabled.toString()))
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        settingsDao.insert(SettingsEntity(KEY_DARK_MODE_ENABLED, enabled.toString()))
    }

    suspend fun setThemeColor(colorName: String) {
        settingsDao.insert(SettingsEntity(KEY_THEME_COLOR, colorName))
    }

    suspend fun setDarkModeConfig(config: String) {
        settingsDao.insert(SettingsEntity(KEY_DARK_MODE_CONFIG, config))
    }

    suspend fun setLastSystemDarkMode(isDark: Boolean) {
        settingsDao.insert(SettingsEntity(KEY_LAST_SYSTEM_DARK_MODE, isDark.toString()))
    }
}

