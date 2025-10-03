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
    val isCompleted: Boolean = false,
    val nextVolumeDate: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val audioNoteUri: String? = null,
    val audioNoteUpdatedAt: Long? = null,
    val audioNoteEnabled: Boolean = false       // NEU: Feature pro Manga an/aus
)



