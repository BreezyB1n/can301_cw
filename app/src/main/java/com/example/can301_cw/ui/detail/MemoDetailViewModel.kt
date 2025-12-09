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

import com.example.can301_cw.network.ArkChatClient
import com.example.can301_cw.model.ApiResponse
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.util.Date
import kotlinx.coroutines.flow.first

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

    fun regenerateAIAnalysis() {
        viewModelScope.launch(Dispatchers.IO) {
            val item = memoItem.value ?: return@launch
            
            // Set processing state
            memoDao.insertMemo(item.copy(isAPIProcessing = true))

            try {
                // 1. Prepare Content (Image or Text)
                val base64Image = if (item.imageData != null) {
                     val compressedImageData = compressImage(item.imageData!!)
                     android.util.Base64.encodeToString(compressedImageData, android.util.Base64.NO_WRAP)
                } else {
                    "" // Should handle text-only case if needed, but for now assuming image flow primarily or empty string for text
                }
                
                // For text-only memos, we might need a different API call or pass text as content.
                // Assuming chatWithImageUrl handles empty image for text-only if we modify it, 
                // OR we only support image regeneration for now based on previous HomeViewModel logic.
                // However, ArkChatClient.chatWithImageUrl seems designed for image.
                // If it's text only, we should use the text content.
                
                // Let's check ArkChatClient.chatWithImageUrl signature. 
                // It takes 'content' which is base64 image.
                
                // If we want to support text regeneration, we need to know if ArkChatClient supports it.
                // Based on HomeViewModel, it seems to focus on image.
                // But the user request is generic "Regenerate".
                // If there is no image, we should probably use the text.
                // For now, let's implement the image logic as it's the most complex and requested feature usually.
                
                if (item.imageData == null && item.userInputText.isBlank() && item.recognizedText.isBlank()) {
                    memoDao.insertMemo(item.copy(isAPIProcessing = false))
                    return@launch
                }

                // 2. Call API
                val apiKey = settingsRepository.aiApiKey.first()
                val result = ArkChatClient.chatWithImageUrl(
                    tags = emptyList(),
                    content = if (item.imageData != null) base64Image else (item.userInputText.ifBlank { item.recognizedText }),
                    isImage = item.imageData != null,
                    apiKey = apiKey
                )

                result.onSuccess { jsonString ->
                    // 3. Parse JSON
                    println("AI Regeneration Result: $jsonString")

                    val gson = Gson()
                    
                    // Parse the outer layer (OpenAI format)
                    val rootObj = JsonParser.parseString(jsonString).asJsonObject
                    val choices = rootObj.getAsJsonArray("choices")
                    
                    if (choices.size() > 0) {
                        val contentJsonStr = choices[0].asJsonObject
                            .getAsJsonObject("message")
                            .get("content").asString
                            
                        val apiResponse = gson.fromJson(contentJsonStr, ApiResponse::class.java)

                        // 4. Update MemoItem
                        val updatedItem = item.copy(
                            title = apiResponse.information.title,
                            recognizedText = apiResponse.information.summary,
                            // Ensure we don't exceed 6 tags even if API returns more or existing tags are present
                            tags = (item.tags + apiResponse.allTags).distinct().take(6).toMutableList(),
                            apiResponse = apiResponse,
                            hasAPIResponse = true,
                            isAPIProcessing = false,
                            apiProcessedAt = Date()
                        )

                        // 5. Update DB
                        memoDao.insertMemo(updatedItem)
                    } else {
                        // Restore state if no choices
                        memoDao.insertMemo(item.copy(isAPIProcessing = false))
                    }
                }.onFailure { e ->
                    e.printStackTrace()
                    // Restore state on failure
                    memoDao.insertMemo(item.copy(isAPIProcessing = false))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Restore state on exception
                memoDao.insertMemo(item.copy(isAPIProcessing = false))
            }
        }
    }

    private fun compressImage(imageData: ByteArray): ByteArray {
        val options = android.graphics.BitmapFactory.Options()
        options.inJustDecodeBounds = true
        android.graphics.BitmapFactory.decodeByteArray(imageData, 0, imageData.size, options)

        // Calculate inSampleSize
        val maxDimension = 1024 // Max width or height
        var inSampleSize = 1
        if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
            val halfHeight: Int = options.outHeight / 2
            val halfWidth: Int = options.outWidth / 2
            while ((halfHeight / inSampleSize) >= maxDimension && (halfWidth / inSampleSize) >= maxDimension) {
                inSampleSize *= 2
            }
        }

        options.inJustDecodeBounds = false
        options.inSampleSize = inSampleSize
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageData, 0, imageData.size, options)

        val outputStream = java.io.ByteArrayOutputStream()
        // Compress to JPEG with 80% quality
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
        return outputStream.toByteArray()
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
