package com.example.can301_cw.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 提醒广播接收器
 * 接收 AlarmManager 触发的闹钟事件，并显示通知
 */
class ReminderReceiver : BroadcastReceiver() {
    
    companion object {
        const val EXTRA_MEMO_ID = "memo_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_CONTENT = "content"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val memoId = intent.getStringExtra(EXTRA_MEMO_ID) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "备忘提醒"
        val content = intent.getStringExtra(EXTRA_CONTENT) ?: ""
        
        // 显示通知
        NotificationHelper.showNotification(
            context = context,
            memoId = memoId,
            title = title,
            content = content
        )
    }
}

