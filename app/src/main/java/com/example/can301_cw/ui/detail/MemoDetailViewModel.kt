package com.example.can301_cw.ui.detail

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

class MemoDetailViewModel(
    private val memoDao: MemoDao,
    private val imageStorageManager: ImageStorageManager,
    private val memoId: String
) : ViewModel() {

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

    class Factory(
        private val memoDao: MemoDao,
        private val imageStorageManager: ImageStorageManager,
        private val memoId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MemoDetailViewModel::class.java)) {
                return MemoDetailViewModel(memoDao, imageStorageManager, memoId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
