package com.example.can301_cw.data

import com.example.can301_cw.model.MemoItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeMemoDao : MemoDao {
    override fun getAllMemos(): Flow<List<MemoItem>> {
        return flowOf(emptyList())
    }

    override suspend fun getMemoCount(): Int {
        return 0
    }

    override suspend fun insertMemo(memo: MemoItem) {
        // No-op
    }

    override suspend fun deleteMemo(memo: MemoItem) {
        // No-op
    }

    override suspend fun deleteAll() {
        // No-op
    }
}
