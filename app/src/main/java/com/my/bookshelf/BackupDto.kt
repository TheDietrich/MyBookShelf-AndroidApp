/**
 * Code by: Jonas Dietrich
 * Date: 18.03.2025
 */
package com.my.bookshelf

/**
 * Komplettes Backup.
 * version = 4 (mit Kategorien)
 */
data class BackupData(
    val version: Int = 4,
    val mangaList: List<MangaExportDto>,
    val wishlist: List<WishlistExportDto>? = null,
    val categories: List<CategoryExportDto>? = null
)

/** Einzelner Manga im Backup */
data class MangaExportDto(
    val id: String,
    val titel: String,
    val coverBase64: String?,                 // null falls kein Bild
    val aktuellerBand: Int,
    val gekaufteBaende: Int,
    val isCompleted: Boolean?,                // kann null sein (altes Backup)
    val nextVolumeDate: String?,              // kann null sein (altes Backup)
    val specialSeries: List<SpecialSeriesExportDto>?, // kann null sein (altes Backup)
    val dateAdded: Long?,
    val lastModified: Long?,

    val audioNoteEnabled: Boolean? = null,
    val audioBase64: String? = null,
    val audioUpdatedAt: Long? = null,
    val categoryId: String? = null            // null in alten Backups -> DEFAULT_ID
)

/** Sonderreihe-Eintrag */
data class SpecialSeriesExportDto(
    val id: String,
    val parentMangaId: String,
    val name: String,
    val aktuellerBand: Int,
    val gekaufteBaende: Int
)

data class WishlistExportDto(
    val id: String,
    val title: String,
    val dateAdded: Long
)

data class CategoryExportDto(
    val id: String,
    val name: String,
    val sortOrder: Int
)
