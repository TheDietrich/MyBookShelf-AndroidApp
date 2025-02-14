package com.example.mybooksheelf

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {
    @Query("SELECT * FROM manga_table")
    fun getAllManga(): Flow<List<MangaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(manga: MangaEntity): Long

    @Update
    suspend fun update(manga: MangaEntity): Int
}
