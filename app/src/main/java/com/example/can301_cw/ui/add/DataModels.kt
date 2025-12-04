package com.example.can301_cw.ui.add

import android.net.Uri
import com.example.can301_cw.model.ApiResponse

data class AddMemoUiState(
    val title: String = "",
    val content: String = "",
    val selectedImageUri: Uri? = null,
    val recognizedText: String = "", // OCR result
    val isParsing: Boolean = false,
    val apiResponse: ApiResponse? = null,
    val selectedTags: Set<String> = emptySet(),
    val localTags: List<String> = emptyList(), // Loaded from DB or predefined
    val useAIParsing: Boolean = true,
    val showTagInputDialog: Boolean = false, // Control dialog visibility
    val isSaving: Boolean = false,
    val error: String? = null
)

