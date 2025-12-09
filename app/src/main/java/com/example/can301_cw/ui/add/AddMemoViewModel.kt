package com.example.can301_cw.ui.add

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.can301_cw.data.ImageStorageManager
import com.example.can301_cw.data.MemoDao
import com.example.can301_cw.data.SettingsRepository
import com.example.can301_cw.model.MemoItem
import com.example.can301_cw.network.ArkChatClient
import com.example.can301_cw.notification.ReminderScheduler
import com.google.gson.Gson
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Date

class AddMemoViewModel(
    private val application: Application,
    private val memoDao: MemoDao,
    private val imageStorageManager: ImageStorageManager,
    private val reminderScheduler: ReminderScheduler,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMemoUiState())
    val uiState: StateFlow<AddMemoUiState> = _uiState.asStateFlow()

    private val gson = Gson()

    // Mock local tags for demo purpose
    init {
        // Observe all tags from DB
        viewModelScope.launch(Dispatchers.IO) {
            memoDao.getAllTags().collect { tagsList ->
                // Flatten and dedup
                val allTags = tagsList.flatMap { tagString ->
                    try {
                        // Converters store it as JSON string.
                        // We use Gson to parse it back to a List<String>.
                        val listType = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
                        gson.fromJson<List<String>>(tagString, listType) ?: emptyList()
                    } catch (e: Exception) {
                        emptyList<String>()
                    }
                }.toSet().toList()

                _uiState.update { 
                    it.copy(localTags = allTags)
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun toggleAiParsing(enabled: Boolean) {
        _uiState.update { it.copy(useAIParsing = enabled) }
    }

    fun showTagInputDialog(show: Boolean) {
        _uiState.update { it.copy(showTagInputDialog = show) }
    }

    fun addTag(tag: String) {
        if (tag.isNotBlank()) {
            _uiState.update { 
                it.copy(selectedTags = it.selectedTags + tag)
            }
        }
    }

    fun removeTag(tag: String) {
        _uiState.update { 
            it.copy(selectedTags = it.selectedTags - tag)
        }
    }

    fun onImageSelected(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri, recognizedText = "") }
        if (uri != null) {
            processImageOcr(uri)
        }
    }

    fun removeImage() {
        _uiState.update { it.copy(selectedImageUri = null, recognizedText = "", apiResponse = null) }
    }

    private fun processImageOcr(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val image = InputImage.fromFilePath(application, uri)
                // Attempt to use Chinese recognizer, fallback to Latin if dependency missing
                val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
                
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        _uiState.update { it.copy(recognizedText = visionText.text) }
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun parseContent() {
        val state = _uiState.value
        
        // Prepare text content (Title + Input + OCR)
        val textToAnalyze = buildString {
            if (state.title.isNotBlank()) append("Title: ${state.title}\n")
            if (state.content.isNotBlank()) append("Content: ${state.content}\n")
            // If we have recognized text, we can append it as well, 
            // though Vision model can read image too. 
            // Let's include it as supplementary info if it exists.
            if (state.recognizedText.isNotBlank()) append("OCR Text: ${state.recognizedText}\n")
        }.trim()

        // Check if we have anything to analyze
        if (textToAnalyze.isBlank() && state.selectedImageUri == null) {
             _uiState.update { it.copy(error = "Please enter text or select an image.") }
             return
        }

        viewModelScope.launch {
            // Set parsing state to true immediately
            _uiState.update { it.copy(isParsing = true, error = null) }
            
            try {
                // Compress image if exists
                val base64Image = state.selectedImageUri?.let { uri ->
                    compressImageToBase64(uri)
                }

                // Call AI with generic analyzeContent
                val tagsContext = state.selectedTags.toList() + state.localTags
                val allAvailableTags = state.localTags
                
                // Get API Key from Settings
                val apiKey = settingsRepository.aiApiKey.first()

                val result = withContext(Dispatchers.IO) {
                    ArkChatClient.analyzeContent(
                        context = application,
                        text = if (textToAnalyze.isBlank()) null else textToAnalyze,
                        imageBase64 = base64Image,
                        tags = allAvailableTags,
                        apiKeyResId = -1, // Ignore resource ID
                        apiKey = apiKey   // Use the key string directly
                    )
                }

                result.fold(
                    onSuccess = { jsonResponse ->
                        try {
                            val rootObj = com.google.gson.JsonParser.parseString(jsonResponse).asJsonObject
                            val choices = rootObj.getAsJsonArray("choices")
                            if (choices.size() > 0) {
                                val contentJsonStr = choices[0].asJsonObject
                                    .getAsJsonObject("message")
                                    .get("content").asString
                                
                                val parsedResponse = gson.fromJson(contentJsonStr, com.example.can301_cw.model.ApiResponse::class.java)
                                
                                _uiState.update { 
                                    it.copy(
                                        apiResponse = parsedResponse,
                                        selectedTags = (it.selectedTags + parsedResponse.allTags).distinct().take(6).toSet()
                                    ) 
                                }
                            }
                        } catch (e: Exception) {
                            _uiState.update { it.copy(error = "Failed to parse response: ${e.message}") }
                            e.printStackTrace()
                        }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(error = "AI Request Failed: ${e.message}") }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Unknown Error: ${e.message}") }
            } finally {
                // Ensure parsing state is reset to false when everything is done
                _uiState.update { it.copy(isParsing = false) }
            }
        }
    }

    private suspend fun compressImageToBase64(uri: Uri): String = withContext(Dispatchers.IO) {
        val inputStream = application.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val outputStream = ByteArrayOutputStream()
        // Resize if too big (simple max dimension check)
        val maxDimension = 1024
        var width = bitmap.width
        var height = bitmap.height
        
        if (width > maxDimension || height > maxDimension) {
            val ratio = width.toFloat() / height.toFloat()
            if (width > height) {
                width = maxDimension
                height = (maxDimension / ratio).toInt()
            } else {
                height = maxDimension
                width = (maxDimension * ratio).toInt()
            }
        }
        
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        
        // Compress to JPEG with 80% quality
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val bytes = outputStream.toByteArray()
        Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun saveMemo(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.title.isBlank() && state.content.isBlank() && state.selectedImageUri == null) {
            _uiState.update { it.copy(error = "内容不能为空") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                // Save image if exists
                var savedImagePath: String? = null
                if (state.selectedImageUri != null) {
                    withContext(Dispatchers.IO) {
                        val inputStream = application.contentResolver.openInputStream(state.selectedImageUri)
                        val bytes = inputStream?.readBytes()
                        inputStream?.close()
                        if (bytes != null) {
                            savedImagePath = imageStorageManager.saveImage(bytes)
                        }
                    }
                }

                val memoItem = MemoItem(
                    title = state.title,
                    userInputText = state.content,
                    recognizedText = state.recognizedText,
                    imagePath = savedImagePath,
                    tags = state.selectedTags.toMutableList(),
                    apiResponse = state.apiResponse,
                    hasAPIResponse = state.apiResponse != null,
                    createdAt = Date(),
                    source = if (state.selectedImageUri != null) "IMAGE" else "TEXT"
                )

                memoDao.insertMemo(memoItem)
                
                // 如果设置了提醒时间，注册闹钟
                if (memoItem.scheduledDate != null) {
                    reminderScheduler.scheduleReminder(memoItem)
                }
                
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "保存失败: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    class Factory(
        private val application: Application,
        private val memoDao: MemoDao,
        private val imageStorageManager: ImageStorageManager,
        private val reminderScheduler: ReminderScheduler,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddMemoViewModel::class.java)) {
                return AddMemoViewModel(application, memoDao, imageStorageManager, reminderScheduler, settingsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
