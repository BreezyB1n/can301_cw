package com.example.can301_cw.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface TaskDao {
    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}

