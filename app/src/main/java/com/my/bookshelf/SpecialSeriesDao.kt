/**
 * Code by: Jonas Dietrich
 * Date: 18.03.2025
 */

package com.my.bookshelf

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SpecialSeriesDao {
    @Query("SELECT * FROM special_series WHERE parentMangaId = :parentId")
    fun getSpecialSeriesForManga(parentId: String): Flow<List<SpecialSeriesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(series: SpecialSeriesEntity)

    @Update
    suspend fun update(series: SpecialSeriesEntity)

    @Delete
    suspend fun delete(series: SpecialSeriesEntity)
}
