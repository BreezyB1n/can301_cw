package com.example.can301_cw.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.can301_cw.data.MemoDao
import com.example.can301_cw.model.MemoItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class TagCategory(
    val name: String,
    val count: Int,
    val id: String = java.util.UUID.randomUUID().toString()
)

class CategoryViewModel(
    private val memoDao: MemoDao
) : ViewModel() {

    // Get all memos to extract unique tags
    val tagCategories: StateFlow<List<TagCategory>> = memoDao.getAllMemos()
        .map { memos ->
            // Extract all unique tags with their counts
            val tagCounts = mutableMapOf<String, Int>()
            
            memos.forEach { memo ->
                memo.tags.forEach { tag ->
                    tagCounts[tag] = (tagCounts[tag] ?: 0) + 1
                }
            }
            
            // Convert to TagCategory list and sort by count descending
            tagCounts.map { (tag, count) ->
                TagCategory(name = tag, count = count)
            }.sortedByDescending { it.count }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getMemosByTag(tag: String): Flow<List<MemoItem>> {
        return memoDao.getMemosByTag(tag)
    }

    companion object {
        fun Factory(memoDao: MemoDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CategoryViewModel(memoDao) as T
                }
            }
    }
}
