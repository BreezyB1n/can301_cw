package com.example.can301_cw.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.can301_cw.data.ImageStorageManager
import com.example.can301_cw.data.MemoDao
import com.example.can301_cw.model.MemoItem
import com.example.can301_cw.network.ArkChatClient
import com.example.can301_cw.model.ApiResponse
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class HomeViewModel(
    private val memoDao: MemoDao,
    private val imageStorageManager: ImageStorageManager
) : ViewModel() {
    
    val memoItems: StateFlow<List<MemoItem>> = memoDao.getAllMemos()
        .map { list ->
            list.map { item ->
                // Load image data from file if path exists and data is missing
                if (item.imagePath != null && item.imageData == null) {
                    item.apply {
                        imageData = imageStorageManager.loadImage(imagePath!!)
                    }
                } else {
                    item
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addMemoItem(item: MemoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            // Save image to file storage if present
            item.imageData?.let { bytes ->
                val path = imageStorageManager.saveImage(bytes)
                item.imagePath = path
            }
            memoDao.insertMemo(item)

            // Trigger AI analysis if image exists
            if (item.imageData != null) {
                processImageWithAI(item)
            }
        }
    }

    private fun processImageWithAI(item: MemoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Compress Image
                val compressedImageData = item.imageData?.let { compressImage(it) } ?: return@launch

                // 2. Prepare Base64
                val base64Image = android.util.Base64.encodeToString(compressedImageData, android.util.Base64.NO_WRAP)

                // 3. Call API
                val result = ArkChatClient.chatWithImageUrl(
                    tags = emptyList(),
                    content = base64Image,
                    isImage = true
                )

                result.onSuccess { jsonString ->
                    // 4. Parse JSON
                    println("AI Analysis Result: $jsonString")

                    val gson = Gson()
                    
                    // Parse the outer layer (OpenAI format)
                    val rootObj = JsonParser.parseString(jsonString).asJsonObject
                    val choices = rootObj.getAsJsonArray("choices")
                    
                    if (choices.size() > 0) {
                        val contentJsonStr = choices[0].asJsonObject
                            .getAsJsonObject("message")
                            .get("content").asString
                            
                        val apiResponse = gson.fromJson(contentJsonStr, ApiResponse::class.java)

                        // 5. Update MemoItem
                        val updatedItem = item.copy(
                            title = apiResponse.information.title,
                            recognizedText = apiResponse.information.summary,
                            tags = (item.tags + apiResponse.allTags).distinct().toMutableList(),
                            apiResponse = apiResponse,
                            hasAPIResponse = true,
                            apiProcessedAt = Date()
                        )

                        // 6. Update DB
                        // Use insertMemo (REPLACE strategy) to update
                        memoDao.insertMemo(updatedItem)
                    }
                }.onFailure { e ->
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
        private val imageStorageManager: ImageStorageManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(memoDao, imageStorageManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
