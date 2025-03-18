/**
 * Code by: Jonas Dietrich
 * Date: 18.03.2025
 */

package com.example.mybooksheelf

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "special_series")
data class SpecialSeriesEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val parentMangaId: String, // Verweist auf die ID des Haupt-Mangas
    var name: String,
    var aktuellerBand: Int,
    var gekaufteBände: Int
)
