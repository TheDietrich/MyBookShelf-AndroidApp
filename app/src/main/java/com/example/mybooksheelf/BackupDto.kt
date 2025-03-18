package com.example.mybooksheelf

/**
 * Struktur für das komplette Backup:
 * - version: Damit wir bei Bedarf später erkennen können, welche Felder existieren.
 * - mangaList: Die Liste aller Mangas inklusive Sonderreihen und Status.
 */
data class BackupData(
    val version: Int = 2, // z.B. 2, weil wir jetzt Sonderreihen & Status unterstützen
    val mangaList: List<MangaExportDto>
)

/**
 * Einzelner Manga im Backup
 */
data class MangaExportDto(
    val id: String,
    val titel: String,
    val coverBase64: String?,      // null falls kein Bild
    val aktuellerBand: Int,
    val gekaufteBaende: Int,
    val isCompleted: Boolean?,     // kann null sein, wenn altes Backup
    val nextVolumeDate: String?,   // kann null sein, wenn altes Backup
    val specialSeries: List<SpecialSeriesExportDto>? // kann null sein, wenn altes Backup
)

/**
 * Sonderreihe-Eintrag
 */
data class SpecialSeriesExportDto(
    val id: String,
    val parentMangaId: String,
    val name: String,
    val aktuellerBand: Int,
    val gekaufteBaende: Int
)
