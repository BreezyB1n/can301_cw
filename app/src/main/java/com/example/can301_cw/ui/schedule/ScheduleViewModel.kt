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

data class ScheduleUiState(
    val groupedTasks: Map<String, List<TaskWithMemoId>> = emptyMap(),
    val isLoading: Boolean = false
)

// Helper wrapper to know which Memo a task belongs to (for updating)
data class TaskWithMemoId(
    val task: ScheduleTask,
    val memoId: String,
    val memoTitle: String
)

class ScheduleViewModel(private val memoDao: MemoDao) : ViewModel() {

    private val _forceRefresh = MutableStateFlow(0L)

    // We observe all memos and transform them into a grouped list of tasks
    val uiState: StateFlow<ScheduleUiState> = combine(
        memoDao.getAllMemos(),
        _forceRefresh
    ) { memos, _ ->
            val allTasks = mutableListOf<TaskWithMemoId>()

            memos.forEach { memo ->
                memo.apiResponse?.schedule?.tasks?.forEach { task ->
                    // Only show tasks that are not ignored (optional, based on requirement)
                    // For now we show all, maybe sort ignored to bottom?
                    allTasks.add(TaskWithMemoId(task, memo.id, memo.title))
                }
            }

            // Sort by start time
            // Assuming startTime is "YYYY-MM-DD HH:mm" or similar standard format.
            // If it's natural language, parsing might be tricky without normalization.
            // Here we do a simple string sort which works for ISO formats.
            allTasks.sortBy { it.task.startTime }

            // Group by Date
            // We'll try to extract the date part from startTime string
            val grouped = allTasks.groupBy { taskWrapper ->
                extractDate(taskWrapper.task.startTime)
            }
            // Sort groups by date key (descending or ascending?) - usually ascending for schedule
            val sortedGrouped = grouped.toSortedMap()

            ScheduleUiState(groupedTasks = sortedGrouped)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ScheduleUiState(isLoading = true)
        )

    private fun extractDate(startTime: String): String {
        if (startTime.isBlank()) return "Undated"
        // Handle "2025-12-12 10:00" or "2025-12-12T10:00"
        val delimiters = charArrayOf(' ', 'T')
        return startTime.split(*delimiters)[0]
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

    class Factory(private val memoDao: MemoDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
                return ScheduleViewModel(memoDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
