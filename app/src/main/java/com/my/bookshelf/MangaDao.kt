/**
 * Code by: Jonas Dietrich
 * Date: 18.03.2025
 */

package com.my.bookshelf

import androidx.room.Dao
import androidx.room.Delete
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

    @Delete
    suspend fun delete(manga: MangaEntity)

    @Query("UPDATE manga_table SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun reassignToCategory(oldCategoryId: String, newCategoryId: String)
}
