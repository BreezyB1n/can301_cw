package com.example.can301_cw.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.can301_cw.model.MemoItem
import kotlinx.coroutines.flow.Flow

/*
DAO stands for "Data Access Object." 
In Android's Room persistence library, a DAO is an interface that defines methods for 
accessing a database. It typically contains methods for manipulating data in the database, 
such as Create, Read, Update, and Delete operations (CRUD operations).
*/
@Dao
interface MemoDao {
    @Query("SELECT * FROM memos ORDER BY createdAt DESC")
    fun getAllMemos(): Flow<List<MemoItem>>

    @Query("SELECT * FROM memos WHERE id = :memoId")
    fun getMemoById(memoId: String): Flow<MemoItem?>

    @Query("SELECT COUNT(*) FROM memos")
    suspend fun getMemoCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemo(memo: MemoItem)

    @Delete
    suspend fun deleteMemo(memo: MemoItem)

    @Query("DELETE FROM memos WHERE id = :id")
    suspend fun deleteMemoById(id: String)
    
    @Query("DELETE FROM memos")
    suspend fun deleteAll()

    @Query("SELECT * FROM memos WHERE scheduledDate IS NOT NULL AND scheduledDate > :currentTime")
    suspend fun getMemosWithFutureReminders(currentTime: Long): List<MemoItem>

    @Query("SELECT * FROM memos WHERE tags LIKE '%' || :tag || '%' ORDER BY createdAt DESC")
    fun getMemosByTag(tag: String): Flow<List<MemoItem>>

    @Query("SELECT tags FROM memos")
    fun getAllTags(): Flow<List<String>>
}
