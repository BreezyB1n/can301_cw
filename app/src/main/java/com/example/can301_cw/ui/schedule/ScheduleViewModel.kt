package com.example.can301_cw.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.can301_cw.data.MemoDao
import com.example.can301_cw.model.MemoItem
import com.example.can301_cw.model.ScheduleTask
import com.example.can301_cw.model.TaskStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.can301_cw.notification.ReminderScheduler

data class ScheduleUiState(
    val groupedTasks: Map<String, List<TaskWithMemoId>> = emptyMap(),
    val isLoading: Boolean = false
)

// Helper wrapper to know which Memo a task belongs to (for updating)
data class TaskWithMemoId(
    val task: ScheduleTask,
    val memoId: String,
    val memoTitle: String,
    val displayDate: String = ""
)

class ScheduleViewModel(
    private val memoDao: MemoDao,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _forceRefresh = MutableStateFlow(0L)
    private val _showAllTasks = MutableStateFlow(false)
    val showAllTasks: StateFlow<Boolean> = _showAllTasks

    fun setShowAllTasks(show: Boolean) {
        _showAllTasks.value = show
    }

    // We observe all memos and transform them into a grouped list of tasks
    val uiState: StateFlow<ScheduleUiState> = combine(
        memoDao.getAllMemos(),
        _forceRefresh,
        _showAllTasks
    ) { memos, _, showAll ->
            val allTasks = mutableListOf<TaskWithMemoId>()
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            memos.forEach { memo ->
                val memoDate = dateFormatter.format(memo.createdAt)
                memo.apiResponse?.schedule?.tasks?.forEach { task ->
                    // Filter: Show all if toggled, otherwise only PENDING
                    if (!showAll && task.taskStatus != TaskStatus.PENDING) return@forEach

                    // Use memo creation date if task date cannot be determined
                    val taskDate = extractDate(task.startTime)
                    val finalDate = if (isValidDate(taskDate)) taskDate else memoDate
                    
                    allTasks.add(TaskWithMemoId(
                        task = task,
                        memoId = memo.id,
                        memoTitle = memo.title,
                        displayDate = finalDate
                    ))
                }
            }

            // Group by Date
            val grouped = allTasks.groupBy { it.displayDate }
            
            // Sort groups by date key descending (newest first)
            val sortedGrouped = grouped.toSortedMap(compareByDescending { it })

            ScheduleUiState(groupedTasks = sortedGrouped)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ScheduleUiState(isLoading = true)
        )

    private fun extractDate(startTime: String): String {
        if (startTime.isBlank() || startTime.equals("Today", ignoreCase = true)) return ""
        // Handle "2025-12-12 10:00" or "2025-12-12T10:00"
        val delimiters = charArrayOf(' ', 'T')
        val datePart = startTime.split(*delimiters)[0]
        return if (datePart.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) datePart else ""
    }

    private fun isValidDate(dateStr: String): Boolean {
        return dateStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
    }


    fun toggleTaskStatus(taskWrapper: TaskWithMemoId) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Use firstOrNull to get the current state of the memo from the Flow
                    val memo = memoDao.getMemoById(taskWrapper.memoId).firstOrNull() ?: return@withContext
                    
                    val currentApiResponse = memo.apiResponse ?: return@withContext
                    
                    var hasChanges = false
                    val updatedTasks = currentApiResponse.schedule.tasks.map {
                        if (it.id == taskWrapper.task.id) {
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
                        // Use insertMemo (REPLACE strategy) for update
                        memoDao.insertMemo(updatedMemo)
                        
                        // Force UI update just in case Room flow is slow or deduplicates logic
                        _forceRefresh.value = System.currentTimeMillis()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setTaskStatus(taskWrapper: TaskWithMemoId, status: TaskStatus) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val memo = memoDao.getMemoById(taskWrapper.memoId).firstOrNull() ?: return@withContext
                    
                    val currentApiResponse = memo.apiResponse ?: return@withContext
                    
                    var hasChanges = false
                    val updatedTasks = currentApiResponse.schedule.tasks.map {
                        if (it.id == taskWrapper.task.id) {
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
                        // Use insertMemo (REPLACE strategy) for update
                        memoDao.insertMemo(updatedMemo)
                        
                        // Force UI update
                        _forceRefresh.value = System.currentTimeMillis()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setTaskReminder(taskWrapper: TaskWithMemoId, timestamp: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val memo = memoDao.getMemoById(taskWrapper.memoId).firstOrNull() ?: return@withContext
                    val currentApiResponse = memo.apiResponse ?: return@withContext
                    
                    var hasChanges = false
                    val updatedTasks = currentApiResponse.schedule.tasks.map {
                        if (it.id == taskWrapper.task.id) {
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
                        reminderScheduler.scheduleTaskReminder(updatedMemo, taskWrapper.task.id, timestamp)

                        // Force UI update
                        _forceRefresh.value = System.currentTimeMillis()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    class Factory(
        private val memoDao: MemoDao,
        private val reminderScheduler: ReminderScheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
                return ScheduleViewModel(memoDao, reminderScheduler) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
