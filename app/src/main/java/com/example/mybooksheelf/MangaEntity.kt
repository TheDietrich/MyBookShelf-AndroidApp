package com.example.mybooksheelf

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
    val nextVolumeDate: String? = null       // <--- Neu
)

