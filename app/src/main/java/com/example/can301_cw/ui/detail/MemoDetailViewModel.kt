package com.example.can301_cw.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.can301_cw.data.ImageStorageManager
import com.example.can301_cw.data.MemoDao
import com.example.can301_cw.data.SettingsRepository
import com.example.can301_cw.model.MemoItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

import com.example.can301_cw.model.TaskStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.firstOrNull

import com.example.can301_cw.notification.ReminderScheduler

class MemoDetailViewModel(
    private val memoDao: MemoDao,
    private val imageStorageManager: ImageStorageManager,
    private val memoId: String,
    private val reminderScheduler: ReminderScheduler,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val defaultRemindOffset: StateFlow<Int> = settingsRepository.defaultRemindOffsetMinutes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 5
        )

    val memoItem: StateFlow<MemoItem?> = memoDao.getMemoById(memoId)
        .map { item ->
            if (item?.imagePath != null && item.imageData == null) {
                item.apply {
                    imageData = imageStorageManager.loadImage(imagePath!!)
                }
            } else {
                item
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun toggleTaskStatus(taskId: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val memo = memoItem.value ?: return@withContext
                    val currentApiResponse = memo.apiResponse ?: return@withContext
                    
                    var hasChanges = false
                    val updatedTasks = currentApiResponse.schedule.tasks.map {
                        if (it.id == taskId) {
                            val newStatus = if (it.taskStatus == TaskStatus.COMPLETED) TaskStatus.PENDING else TaskStatus.COMPLETED
                            hasChanges = true
                            it.copy(taskStatus = newStatus)
                        } else {
                            it
                        }
                    }

                    if (hasChanges) {
                        val updatedSchedule = currentApiResponse.schedule.copy(tasks = updatedTasks)
                        val updatedApiResponse = currentApiResponse.copy(schedule = updatedSchedule)
                        val updatedMemo = memo.copy(apiResponse = updatedApiResponse)
                        memoDao.insertMemo(updatedMemo)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setTaskStatus(taskId: String, status: TaskStatus) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val memo = memoItem.value ?: return@withContext
                    val currentApiResponse = memo.apiResponse ?: return@withContext
                    
                    var hasChanges = false
                    val updatedTasks = currentApiResponse.schedule.tasks.map {
                        if (it.id == taskId) {
                            hasChanges = true
                            it.copy(taskStatus = status)
                        } else {
                            it
                        }
                    }

                    if (hasChanges) {
                        val updatedSchedule = currentApiResponse.schedule.copy(tasks = updatedTasks)
                        val updatedApiResponse = currentApiResponse.copy(schedule = updatedSchedule)
                        val updatedMemo = memo.copy(apiResponse = updatedApiResponse)
                        memoDao.insertMemo(updatedMemo)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun setTaskReminder(taskId: String, timestamp: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val memo = memoItem.value ?: return@withContext
                    val currentApiResponse = memo.apiResponse ?: return@withContext
                    
                    var hasChanges = false
                    val updatedTasks = currentApiResponse.schedule.tasks.map {
                        if (it.id == taskId) {
                            hasChanges = true
                            it.copy(reminderTime = timestamp)
                        } else {
                            it
                        }
                    }

                    if (hasChanges) {
                        val updatedSchedule = currentApiResponse.schedule.copy(tasks = updatedTasks)
                        val updatedApiResponse = currentApiResponse.copy(schedule = updatedSchedule)
                        val updatedMemo = memo.copy(apiResponse = updatedApiResponse)
                        memoDao.insertMemo(updatedMemo)
                        
                        // Schedule Notification
                        reminderScheduler.scheduleTaskReminder(updatedMemo, taskId, timestamp)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    class Factory(
        private val memoDao: MemoDao,
        private val imageStorageManager: ImageStorageManager,
        private val memoId: String,
        private val reminderScheduler: ReminderScheduler,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MemoDetailViewModel::class.java)) {
                return MemoDetailViewModel(memoDao, imageStorageManager, memoId, reminderScheduler, settingsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
