package com.example.can301_cw.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.can301_cw.model.MemoItem
import java.util.Date

/**
 * 提醒调度器
 * 负责设置、更新和取消闹钟提醒
 */
class ReminderScheduler(private val context: Context) {
    
    private val alarmManager: AlarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
    
    /**
     * 为备忘录设置提醒
     * @param memo 备忘录对象
     * @return true 如果成功设置，false 如果失败（时间已过或无权限）
     */
    fun scheduleReminder(memo: MemoItem): Boolean {
        val scheduledDate = memo.scheduledDate ?: return false
        
        // 如果时间已过，不设置提醒
        if (scheduledDate.time <= System.currentTimeMillis()) {
            return false
        }
        
        // 检查是否有精确闹钟权限（Android 12+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                return false
            }
        }
        
        val pendingIntent = createPendingIntent(memo)
        
        // 设置精确闹钟，即使在 Doze 模式下也能触发
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            scheduledDate.time,
            pendingIntent
        )
        
        return true
    }
    
    /**
     * 为备忘录设置提醒（使用指定时间）
     * @param memo 备忘录对象
     * @param triggerTime 触发时间
     * @return true 如果成功设置，false 如果失败
     */
    fun scheduleReminder(memo: MemoItem, triggerTime: Date): Boolean {
        // 如果时间已过，不设置提醒
        if (triggerTime.time <= System.currentTimeMillis()) {
            return false
        }
        
        // 检查是否有精确闹钟权限（Android 12+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                return false
            }
        }
        
        val pendingIntent = createPendingIntent(memo)
        
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime.time,
            pendingIntent
        )
        
        return true
    }
    
    /**
     * 取消备忘录的提醒
     * @param memoId 备忘录ID
     */
    fun cancelReminder(memoId: String) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            memoId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
    
    /**
     * 取消备忘录的提醒
     * @param memo 备忘录对象
     */
    fun cancelReminder(memo: MemoItem) {
        cancelReminder(memo.id)
    }
    
    /**
     * 更新备忘录的提醒（先取消再重新设置）
     * @param memo 备忘录对象
     * @return true 如果成功更新，false 如果失败
     */
    fun updateReminder(memo: MemoItem): Boolean {
        cancelReminder(memo.id)
        return scheduleReminder(memo)
    }
    
    /**
     * 检查是否可以设置精确闹钟
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
    
    /**
     * 创建用于触发提醒的 PendingIntent
     */
    private fun createPendingIntent(memo: MemoItem): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_MEMO_ID, memo.id)
            putExtra(ReminderReceiver.EXTRA_TITLE, memo.title.ifEmpty { "备忘提醒" })
            putExtra(
                ReminderReceiver.EXTRA_CONTENT,
                memo.apiResponse?.information?.summary ?: memo.userInputText.take(100)
            )
        }
        
        return PendingIntent.getBroadcast(
            context,
            memo.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

