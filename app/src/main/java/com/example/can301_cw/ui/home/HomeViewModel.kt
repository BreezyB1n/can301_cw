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

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (memoDao.getMemoCount() == 0) {
                // Initialize with sample data
                val sampleItems = listOf(
                    MemoItem(
                        id = "2",
                        title = "Chino的用户页面",
                        recognizedText = "这是Chino的用户页面，展示了其个人信息、好友编号SW-3802-1832-7999、以及最近的游戏记录，包括《双人成行》、《塞尔达传说 旷野之息》和《LEGO® Worlds》。页面还提供了好友列表、添加好友、邀请和用户设置等功能选项。",
                        tags = mutableListOf("用户资料", "游戏记录", "游戏", "Nintendo Switch", "娱乐"),
                        createdAt = Date(), // Mock date
                    ).apply {
                        // Create a mock image
                        imageData = ByteArray(1)
                    },
                    MemoItem(
                        id = "1",
                        title = "购物小票",
                        recognizedText = "超市购物清单：牛奶、面包、鸡蛋、苹果。总计：¥45.50。",
                        tags = mutableListOf("购物", "账单"),
                        createdAt = Date(), // Mock date
                    ).apply {
                         imageData = ByteArray(1)
                    },
                    MemoItem(
                        id = "3",
                        title = "硕士申请项目确定会议",
                        recognizedText = "关于硕士申请项目的初步讨论，确定了主要方向和时间表。",
                        tags = mutableListOf("申请", "会议", "计划"),
                        createdAt = Date(), // Mock date
                        imagePath = null
                    )
                )
                
                sampleItems.forEach { item ->
                    // Save mock images to storage
                    item.imageData?.let { bytes ->
                        val path = imageStorageManager.saveImage(bytes)
                        item.imagePath = path
                    }
                    memoDao.insertMemo(item)
                }
            }
        }
    }

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
                    val apiResponse = gson.fromJson(jsonString, ApiResponse::class.java)

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
