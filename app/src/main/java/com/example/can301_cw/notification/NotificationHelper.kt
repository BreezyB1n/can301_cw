package com.example.can301_cw.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.can301_cw.MainActivity
import com.example.can301_cw.R

import android.util.Log

/**
 * 通知帮助类
 * 负责创建通知渠道和发送通知
 */
object NotificationHelper {
    
    private const val TAG = "NotificationHelper"
    const val CHANNEL_ID = "memo_reminder_channel"
    private const val CHANNEL_NAME = "备忘录提醒"
    private const val CHANNEL_DESCRIPTION = "备忘录任务提醒通知"
    
    /**
     * 创建通知渠道（Android 8.0+ 必需）
     * 应在 Application 或 MainActivity 的 onCreate 中调用一次
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH // 高优先级，会弹出悬浮通知
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 显示通知
     * @param context 上下文
     * @param memoId 备忘录ID，用于点击通知后跳转
     * @param title 通知标题
     * @param content 通知内容
     */
    fun showNotification(
        context: Context,
        memoId: String,
        title: String,
        content: String
    ) {
        // 检查权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "showNotification: POST_NOTIFICATIONS permission denied")
                return
            }
        }
        
        Log.d(TAG, "showNotification: Building and showing notification for $title")
        
        // 构建点击通知后的 Intent
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("memo_id", memoId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            memoId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 构建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round) // 使用系统 mipmap 图标作为 fallback
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 高优先级
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true) // 点击后自动消失
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // 默认震动、铃声、LED
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        
        // 发送通知
        val notificationManager = NotificationManagerCompat.from(context)
        
        notificationManager.notify(memoId.hashCode(), notification)
        Log.d(TAG, "showNotification: Notification posted with ID ${memoId.hashCode()}")
    }
    
    /**
     * 取消指定通知
     * @param context 上下文
     * @param memoId 备忘录ID
     */
    fun cancelNotification(context: Context, memoId: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(memoId.hashCode())
    }
    
    /**
     * 检查是否有通知权限
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 13 以下不需要运行时权限
        }
    }
}

