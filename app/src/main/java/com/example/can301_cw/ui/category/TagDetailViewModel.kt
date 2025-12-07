package com.example.can301_cw.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.can301_cw.data.ImageStorageManager
import com.example.can301_cw.data.MemoDao
import com.example.can301_cw.model.MemoItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TagDetailViewModel(
    private val memoDao: MemoDao,
    private val imageStorageManager: ImageStorageManager,
    private val tag: String
) : ViewModel() {

    val memos: StateFlow<List<MemoItem>> = memoDao.getMemosByTag(tag)
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

    companion object {
        fun Factory(
            memoDao: MemoDao,
            imageStorageManager: ImageStorageManager,
            tag: String
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TagDetailViewModel(memoDao, imageStorageManager, tag) as T
                }
            }
    }
}
