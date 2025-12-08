package com.example.can301_cw.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.os.PowerManager
import com.example.can301_cw.data.AppDatabase
import com.example.can301_cw.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 提醒广播接收器
 * 接收 AlarmManager 触发的闹钟事件，并显示通知
 */
class ReminderReceiver : BroadcastReceiver() {
    
    companion object {
        const val TAG = "ReminderReceiver"
        const val EXTRA_MEMO_ID = "memo_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_CONTENT = "content"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: Received alarm broadcast")
        
        val pendingResult = goAsync()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Memento:ReminderWakeLock"
        )
        
        wakeLock.acquire(10 * 1000L) // Acquire for 10 seconds max

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val isEnabledStr = database.settingsDao().getValue(SettingsRepository.KEY_NOTIFICATIONS_ENABLED)
                val isEnabled = isEnabledStr?.toBoolean() ?: true

                if (isEnabled) {
                    val memoId = intent.getStringExtra(EXTRA_MEMO_ID) ?: return@launch
                    val title = intent.getStringExtra(EXTRA_TITLE) ?: "Memento Reminder"
                    val content = intent.getStringExtra(EXTRA_CONTENT) ?: ""
                    
                    Log.d(TAG, "onReceive: Showing notification for memoId=$memoId, title=$title")
                    
                    // 显示通知
                    NotificationHelper.showNotification(
                        context = context,
                        memoId = memoId,
                        title = title,
                        content = content
                    )
                } else {
                    Log.d(TAG, "onReceive: Notifications are disabled, skipping.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "onReceive: Error showing notification", e)
            } finally {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
                pendingResult.finish()
            }
        }
    }
}
