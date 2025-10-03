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
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist_table ORDER BY dateAdded DESC")
    fun getAll(): Flow<List<WishlistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WishlistItemEntity): Long

    @Delete
    suspend fun delete(item: WishlistItemEntity)
}
