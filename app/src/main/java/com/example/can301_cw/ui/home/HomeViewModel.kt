package com.example.can301_cw.ui.home

import androidx.lifecycle.ViewModel
import com.example.can301_cw.model.MemoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date

class HomeViewModel : ViewModel() {
    private val _memoItems = MutableStateFlow<List<MemoItem>>(emptyList())
    val memoItems: StateFlow<List<MemoItem>> = _memoItems.asStateFlow()

    init {
        // Initialize with sample data
        _memoItems.value = listOf(
            MemoItem(
                id = "2",
                title = "Chino的用户页面",
                recognizedText = "这是Chino的用户页面，展示了其个人信息、好友编号SW-3802-1832-7999、以及最近的游戏记录，包括《双人成行》、《塞尔达传说 旷野之息》和《LEGO® Worlds》。页面还提供了好友列表、添加好友、邀请和用户设置等功能选项。",
                tags = mutableListOf("用户资料", "游戏记录", "游戏", "Nintendo Switch", "娱乐"),
                createdAt = Date(), // Mock date
                imageData = ByteArray(1) // Mock image presence
            ),
            MemoItem(
                id = "1",
                title = "购物小票",
                recognizedText = "超市购物清单：牛奶、面包、鸡蛋、苹果。总计：¥45.50。",
                tags = mutableListOf("购物", "账单"),
                createdAt = Date(), // Mock date
                imageData = ByteArray(1) // Mock image presence
            ),
            MemoItem(
                id = "3",
                title = "硕士申请项目确定会议",
                recognizedText = "关于硕士申请项目的初步讨论，确定了主要方向和时间表。",
                tags = mutableListOf("申请", "会议", "计划"),
                createdAt = Date(), // Mock date
                imageData = null
            )
        )
    }

    fun addMemoItem(item: MemoItem) {
        _memoItems.update { currentList ->
            listOf(item) + currentList
        }
    }
}
