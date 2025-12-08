package com.example.can301_cw.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import android.util.Log

import android.os.PowerManager

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
        
        // Acquire WakeLock to ensure CPU runs long enough to show notification
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Memento:ReminderWakeLock"
        )
        
        wakeLock.acquire(10 * 1000L) // Acquire for 10 seconds max

        try {
            val memoId = intent.getStringExtra(EXTRA_MEMO_ID) ?: return
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
        } catch (e: Exception) {
            Log.e(TAG, "onReceive: Error showing notification", e)
        } finally {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }
}

