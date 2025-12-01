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
        const val KEY_DARK_MODE_ENABLED = "dark_mode_enabled"
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
}

