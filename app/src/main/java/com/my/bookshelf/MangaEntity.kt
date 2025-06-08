/**
 * Code by: Jonas Dietrich
 * Date: 18.03.2025
 */

package com.my.bookshelf

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "manga_table")
data class MangaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val titel: String,
    val coverUri: String? = null,
    val aktuellerBand: Int,
    val gekaufteBände: Int,
    val isCompleted: Boolean = false,        // <--- Neu
    val nextVolumeDate: String? = null,       // <--- Neu
    val dateAdded: Long = System.currentTimeMillis(),      // hinzufügen
    val lastModified: Long = System.currentTimeMillis()    // hinzufügen
)

