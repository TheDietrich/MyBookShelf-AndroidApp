/**
 * Code by: Jonas Dietrich
 * Date: 18.03.2025
 */
package com.my.bookshelf

/**
 * Komplettes Backup.
 * version = 3 (neues Format mit Audio + Wunschliste)
 */
data class BackupData(
    val version: Int = 3,
    val mangaList: List<MangaExportDto>,
    val wishlist: List<WishlistExportDto>? = null   // NEU: Wunschliste (kann bei alten Backups fehlen)
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

    // NEU ab version>=3: Audio
    val audioNoteEnabled: Boolean? = null,    // null in alten Backups
    val audioBase64: String? = null,          // die Audio-Datei (m4a) als Base64
    val audioUpdatedAt: Long? = null          // Zeitstempel der Audio-Notiz
)

/** Sonderreihe-Eintrag */
data class SpecialSeriesExportDto(
    val id: String,
    val parentMangaId: String,
    val name: String,
    val aktuellerBand: Int,
    val gekaufteBaende: Int
)

/** NEU: Wunschlisten-Element */
data class WishlistExportDto(
    val id: String,
    val title: String,
    val dateAdded: Long
)
