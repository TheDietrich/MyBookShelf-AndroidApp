/**
 * Code by: Jonas Dietrich
 * Date: 18.03.2025
 */

package com.my.bookshelf

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.LocalTextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.compose.material.Checkbox
import android.media.MediaRecorder
import android.media.MediaPlayer
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Delete
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.filled.BookmarkBorder


@Composable
fun MyBookSheelfTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val darkColorPalette = darkColors(
        primary = Color(0xFFFF5722),       // Deep Orange (z.B. Material Deep Orange 500)
        primaryVariant = Color(0xFFE64A19), // Etwas dunklere Variante (Deep Orange 700)
        secondary = Color(0xFF03DAC6),     // Material Teal
        secondaryVariant = Color(0xFF018786),
        background = Color(0xFF121212),    // Standard Material Dark Mode-Hintergrund
        surface = Color(0xFF111111),       // Etwas helleres Schwarz für Oberflächen
        error = Color(0xFFCF6679),         // Standard-Error für Dark Mode
        onPrimary = Color.White,           // Weißer Text auf Orange
        onSecondary = Color.Black,         // Schwarzer Text auf Teal
        onBackground = Color(0xFFE0E0E0),  // Hellgrau auf dunklem Hintergrund
        onSurface = Color(0xFFE0E0E0),     // Hellgrau auf dunklen Oberflächen
        onError = Color.Black
    )



    val lightColorPalette = lightColors(
        primary = Color(0xFF6200EE),
        primaryVariant = Color(0xFF3700B3),
        secondary = Color(0xFF03DAC6),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFFAFAFA),
        error = Color(0xFFB00020),
    )

    // Palette je nach darkTheme wählen
    val colors = if (darkTheme) darkColorPalette else lightColorPalette

    MaterialTheme(colors = colors) {
        content()
    }
}


val Context.dataStore by preferencesDataStore(name = "settings")

class MangaViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = MangaDatabase.getDatabase(app).mangaDao()
    val mangaList = dao.getAllManga()

    private val wishlistDao = MangaDatabase.getDatabase(app).wishlistDao()
    val wishlist: kotlinx.coroutines.flow.Flow<List<WishlistItemEntity>> = wishlistDao.getAll()

    private val _updateSuccess = mutableStateOf(false)
    val updateSuccess: State<Boolean> = _updateSuccess

    fun addManga(manga: MangaEntity) = viewModelScope.launch {
        val newManga = manga.copy(dateAdded = System.currentTimeMillis(), lastModified = System.currentTimeMillis())
        dao.insert(newManga)
    }

    fun updateManga(manga: MangaEntity) = viewModelScope.launch {
        val updated = manga.copy(lastModified = System.currentTimeMillis())
        dao.update(updated)
        _updateSuccess.value = true
    }

    // Audio-Notiz setzen/aktualisieren
    fun setAudioNote(manga: MangaEntity, filePath: String) = viewModelScope.launch {
        val updated = manga.copy(
            audioNoteUri = filePath,
            audioNoteUpdatedAt = System.currentTimeMillis(),
            lastModified = System.currentTimeMillis()
        )
        dao.update(updated)
    }

    // Audio-Notiz entfernen
    fun clearAudioNote(manga: MangaEntity) = viewModelScope.launch {
        val updated = manga.copy(
            audioNoteUri = null,
            audioNoteUpdatedAt = null,
            lastModified = System.currentTimeMillis()
        )
        dao.update(updated)
    }

    // NEU: Audio-Feature pro Manga an/aus schalten.
    // Beim Deaktivieren wird die bestehende Datei gelöscht und Felder geleert.
    fun setAudioNoteEnabled(manga: MangaEntity, enabled: Boolean) = viewModelScope.launch {
        var filePathToDelete: String? = null
        val updated = if (!enabled) {
            filePathToDelete = manga.audioNoteUri
            manga.copy(
                audioNoteEnabled = false,
                audioNoteUri = null,
                audioNoteUpdatedAt = null,
                lastModified = System.currentTimeMillis()
            )
        } else {
            manga.copy(
                audioNoteEnabled = true,
                lastModified = System.currentTimeMillis()
            )
        }
        dao.update(updated)
        // Datei nach DB-Update best-effort entfernen
        filePathToDelete?.let { runCatching { File(it).delete() } }
    }


    fun deleteManga(manga: MangaEntity) = viewModelScope.launch {
        dao.delete(manga)
    }

    fun resetUpdateStatus() {
        _updateSuccess.value = false
    }

    fun saveImage(context: Context, uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "${UUID.randomUUID()}.jpg")
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    // --- Sonderreihen-Funktionen ---
    fun getSpecialSeriesForManga(parentMangaId: String) =
        MangaDatabase.getDatabase(getApplication()).specialSeriesDao().getSpecialSeriesForManga(parentMangaId)

    fun addSpecialSeries(series: SpecialSeriesEntity) = viewModelScope.launch {
        MangaDatabase.getDatabase(getApplication()).specialSeriesDao().insert(series)
    }

    fun updateSpecialSeries(series: SpecialSeriesEntity) = viewModelScope.launch {
        MangaDatabase.getDatabase(getApplication()).specialSeriesDao().update(series)
    }

    fun deleteSpecialSeries(series: SpecialSeriesEntity) = viewModelScope.launch {
        MangaDatabase.getDatabase(getApplication()).specialSeriesDao().delete(series)
    }

    fun updateMangaStatus(manga: MangaEntity, isCompleted: Boolean, nextVolumeDate: String?) = viewModelScope.launch {
        val updated = manga.copy(
            isCompleted = isCompleted,
            nextVolumeDate = nextVolumeDate
        )
        MangaDatabase.getDatabase(getApplication()).mangaDao().update(updated)
        _updateSuccess.value = true
    }

    fun addWishlistItem(title: String) = viewModelScope.launch {
        if (title.isBlank()) return@launch
        wishlistDao.insert(WishlistItemEntity(title = title.trim()))
    }

    fun deleteWishlistItem(item: WishlistItemEntity) = viewModelScope.launch {
        wishlistDao.delete(item)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        // Nach setContent oder davor:
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarDividerColor = android.graphics.Color.TRANSPARENT
        setContent {
            MainApp()
        }
    }
}


@SuppressLint("ContextCastToActivity")
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var darkTheme by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        context.dataStore.data.collect { prefs ->
            darkTheme = prefs[booleanPreferencesKey("darkTheme")] ?: false
        }
    }

    MyBookSheelfTheme(darkTheme = darkTheme) {
        val primaryColor = MaterialTheme.colors.primary
        val activity = LocalContext.current as Activity
        val window = activity.window

        SideEffect {
            // Statusbar oben auf Theme-Primary setzen
            window.statusBarColor = primaryColor.toArgb()
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        }

        // StatusBar Hintergrund-Layer
        Box {
            // 1. Ziehe oben einen "Balken" in Primary-Farbe genau so hoch wie die Statusbar
            Spacer(
                modifier = Modifier
                    .background(primaryColor)
                    .fillMaxWidth()
                    .statusBarsPadding() // exakt so hoch wie Statusbar
                    .height(0.dp) // Die Höhe kommt durch .statusBarsPadding()
            )
            // 2. App-Inhalt
            NavHost(navController, startDestination = "mangaList") {
                composable("mangaList") { MangaListScreen(navController) }
                composable("mangaAdd") { MangaAddScreen(navController) }
                composable("mangaDetail/{mangaId}") { backStackEntry ->
                    MangaDetailScreen(
                        mangaId = backStackEntry.arguments?.getString("mangaId") ?: "",
                        navController = navController
                    )
                }
                composable("settings") { SettingsScreen(navController) }
                composable("wishlist") { WishlistScreen(navController) }
            }
        }
    }
}



/** Settings-Screen (Dark Mode). */
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var darkTheme by remember { mutableStateOf(false) }

    // Dark Mode aus dem DataStore laden
    LaunchedEffect(Unit) {
        context.dataStore.data.collect { prefs ->
            darkTheme = prefs[booleanPreferencesKey("darkTheme")] ?: false
        }
    }

    // Hier initialisieren wir den MangaViewModel, um auch die Backup-Daten zu erhalten
    val viewModel: MangaViewModel = viewModel()
    val mangaList by viewModel.mangaList.collectAsState(emptyList())

    // Launcher zum Erstellen eines Backups (Export)
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { documentUri ->
            context.contentResolver.openOutputStream(documentUri)?.use { stream ->

                // 1) Alle Manga aus der DB (bereits via mangaList)
                val allManga = mangaList

                // 2) Für jedes Manga zusätzlich Sonderreihen laden
                val backupMangaList = allManga.map { mangaEntity ->

                    // Sonderreihen laden
                    val specialList: List<SpecialSeriesEntity> = runBlocking {
                        try {
                            MangaDatabase.getDatabase(context)
                                .specialSeriesDao()
                                .getSpecialSeriesForManga(mangaEntity.id)
                                .first()
                        } catch (e: NoSuchElementException) {
                            emptyList()
                        }
                    }

                    // Cover als Base64
                    val coverB64 = if (!mangaEntity.coverUri.isNullOrBlank()) {
                        readFileAsBase64(mangaEntity.coverUri)
                    } else null

                    // Audio als Base64
                    val audioB64 = if (!mangaEntity.audioNoteUri.isNullOrBlank()) {
                        readFileAsBase64(mangaEntity.audioNoteUri)
                    } else null

                    // Sonderreihen in DTO umwandeln
                    val specialSeriesDto = specialList.map { series ->
                        SpecialSeriesExportDto(
                            id = series.id,
                            parentMangaId = series.parentMangaId,
                            name = series.name,
                            aktuellerBand = series.aktuellerBand,
                            gekaufteBaende = series.gekaufteBände
                        )
                    }

                    MangaExportDto(
                        id = mangaEntity.id,
                        titel = mangaEntity.titel,
                        coverBase64 = coverB64,
                        aktuellerBand = mangaEntity.aktuellerBand,
                        gekaufteBaende = mangaEntity.gekaufteBände,
                        isCompleted = mangaEntity.isCompleted,
                        nextVolumeDate = mangaEntity.nextVolumeDate,
                        specialSeries = specialSeriesDto,
                        dateAdded = mangaEntity.dateAdded,
                        lastModified = mangaEntity.lastModified,

                        audioNoteEnabled = mangaEntity.audioNoteEnabled,
                        audioBase64 = audioB64,
                        audioUpdatedAt = mangaEntity.audioNoteUpdatedAt
                    )
                }

                // 3) Wunschliste laden
                val wishlistItems: List<WishlistItemEntity> = runBlocking {
                    MangaDatabase.getDatabase(context).wishlistDao().getAll().first()
                }
                val wishlistDto = wishlistItems.map {
                    WishlistExportDto(
                        id = it.id,
                        title = it.title,
                        dateAdded = it.dateAdded
                    )
                }

                // 4) In BackupData packen (FORMAT v3)
                val backupData = BackupData(
                    version = 3,
                    mangaList = backupMangaList,
                    wishlist = wishlistDto
                )

                // 5) JSON schreiben
                val json = Gson().toJson(backupData)
                stream.write(json.toByteArray())
            }
        }
    }


    // Launcher zum Importieren eines Backups
    // Innerhalb deines SettingsScreen-Compose-Blocks, z. B. direkt vor dem Scaffold:
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { documentUri ->
            context.contentResolver.openInputStream(documentUri)?.use { stream ->
                val json = stream.bufferedReader().use { it.readText() }
                // Import in einer Coroutine ausführen:
                scope.launch {
                    // 1) Versuche zuerst neues Format (BackupData)
                    val backupData = try {
                        Gson().fromJson(json, BackupData::class.java)
                    } catch (e: Exception) {
                        // Fallback: altes Format (nur Liste MangaExportDto)
                        try {
                            val list: List<MangaExportDto> =
                                Gson().fromJson(json, Array<MangaExportDto>::class.java).toList()
                            BackupData(version = 1, mangaList = list, wishlist = null)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (backupData == null) return@launch

                    // 2) Für jedes Backup-Manga
                    backupData.mangaList.forEach { dto ->
                        val isCompleted = dto.isCompleted ?: false
                        val nextVolumeDate = dto.nextVolumeDate
                        val specialSeriesList = dto.specialSeries ?: emptyList()
                        val dateAdded = dto.dateAdded ?: System.currentTimeMillis()
                        val lastModified = dto.lastModified ?: dateAdded

                        // Cover decodieren
                        val realCoverPath = dto.coverBase64?.let { b64 ->
                            writeFileFromBase64(context, b64) // schreibt .jpg
                        }

                        // Audio decodieren (falls vorhanden)
                        val realAudioPath = dto.audioBase64?.let { b64 ->
                            writeAudioFromBase64(context, b64, extension = "m4a")
                        }
                        val audioEnabled = when {
                            dto.audioNoteEnabled != null -> dto.audioNoteEnabled
                            realAudioPath != null -> true // wenn Audio vorliegt aber Flag fehlt -> aktiv
                            else -> false
                        }
                        val audioUpdatedAt = dto.audioUpdatedAt

                        // Manga einfügen/ersetzen
                        val mangaEntity = MangaEntity(
                            id = dto.id,
                            titel = dto.titel,
                            coverUri = realCoverPath,
                            aktuellerBand = dto.aktuellerBand,
                            gekaufteBände = dto.gekaufteBaende,
                            isCompleted = isCompleted,
                            nextVolumeDate = nextVolumeDate,
                            dateAdded = dateAdded,
                            lastModified = lastModified,
                            audioNoteUri = realAudioPath,
                            audioNoteUpdatedAt = audioUpdatedAt,
                            audioNoteEnabled = audioEnabled
                        )
                        viewModel.addManga(mangaEntity)

                        // Sonderreihen einfügen
                        specialSeriesList.forEach { sDto ->
                            val seriesEntity = SpecialSeriesEntity(
                                id = sDto.id,
                                parentMangaId = sDto.parentMangaId,
                                name = sDto.name,
                                aktuellerBand = sDto.aktuellerBand,
                                gekaufteBände = sDto.gekaufteBaende
                            )
                            viewModel.addSpecialSeries(seriesEntity)
                        }
                    }

                    // 3) Wunschliste importieren (falls vorhanden)
                    val wl = backupData.wishlist ?: emptyList()
                    val wishlistDao = MangaDatabase.getDatabase(context).wishlistDao()
                    wl.forEach { w ->
                        // Original-IDs & Zeitstempel erhalten
                        wishlistDao.insert(
                            WishlistItemEntity(
                                id = w.id,
                                title = w.title,
                                dateAdded = w.dateAdded
                            )
                        )
                    }
                }
            }
        }
    }




    Scaffold(
        modifier = Modifier.systemBarsPadding(), // <- hinzufügen
        backgroundColor = MaterialTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Einstellungen") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                backgroundColor = MaterialTheme.colors.primary
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Dark Mode-Schalter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Switch(
                    checked = darkTheme,
                    onCheckedChange = { enabled ->
                        darkTheme = enabled
                        scope.launch {
                            context.dataStore.edit { settings ->
                                settings[booleanPreferencesKey("darkTheme")] = enabled
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dark Mode")
            }

            // Backup erstellen Button
            Button(
                onClick = { backupLauncher.launch("backup_${System.currentTimeMillis()}.json") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Backup erstellen")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Backup importieren Button
            Button(
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Backup importieren")
            }
        }
    }
}



enum class MangaSortOrder(val displayName: String) {
    TITLE_ASC("Titel A-Z"),
    TITLE_DESC("Titel Z-A"),
    DATE_ADDED_ASC("Hinzugefügt ↑"),
    DATE_ADDED_DESC("Hinzugefügt ↓"),
    DATE_MODIFIED_ASC("Zuletzt geändert ↑"),
    DATE_MODIFIED_DESC("Zuletzt geändert ↓")
}


val SORT_ORDER_KEY = stringPreferencesKey("mangaSortOrder")
val COMPLETED_LAST_KEY = booleanPreferencesKey("completedLast")



@Composable
fun MangaListScreen(navController: NavController) {
    val viewModel: MangaViewModel = viewModel()
    val mangaList by viewModel.mangaList.collectAsState(initial = emptyList())
    var completedLastEnabled by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Sortierung aus DataStore laden und merken
    var sortOrder by remember { mutableStateOf(MangaSortOrder.TITLE_ASC) }
    LaunchedEffect(Unit) {
        context.dataStore.data.collect { prefs ->
            val storedOrder = prefs[SORT_ORDER_KEY]
            // Migration: Falls aus alten Versionen noch "COMPLETED_LAST" gespeichert ist:
            if (storedOrder == "COMPLETED_LAST") {
                sortOrder = MangaSortOrder.TITLE_ASC
                completedLastEnabled = true
            } else {
                sortOrder = MangaSortOrder.entries.find { it.name == storedOrder } ?: MangaSortOrder.TITLE_ASC
                completedLastEnabled = prefs[COMPLETED_LAST_KEY] ?: false
            }
        }
    }


    var sortDropdownExpanded by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }


    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) focusRequester.requestFocus()
    }

    // Anwendung der Sortierung
    // Hilfsfunktion: "normale" Sortierung je nach sortOrder
    fun sortByOrder(list: List<MangaEntity>, order: MangaSortOrder): List<MangaEntity> = when (order) {
        MangaSortOrder.TITLE_ASC -> list.sortedBy { it.titel.lowercase() }
        MangaSortOrder.TITLE_DESC -> list.sortedByDescending { it.titel.lowercase() }
        MangaSortOrder.DATE_ADDED_ASC -> list.sortedBy { it.dateAdded }
        MangaSortOrder.DATE_ADDED_DESC -> list.sortedByDescending { it.dateAdded }
        MangaSortOrder.DATE_MODIFIED_ASC -> list.sortedBy { it.lastModified }
        MangaSortOrder.DATE_MODIFIED_DESC -> list.sortedByDescending { it.lastModified }
    }

    val filteredManga = mangaList
        .filter { it.titel.contains(searchQuery, ignoreCase = true) }
        .let { list ->
            if (completedLastEnabled) {
                val (open, done) = list.partition { !it.isCompleted }
                val sortedOpen = sortByOrder(open, sortOrder)           // gewählte Sortierung
                val sortedDone = done.sortedBy { it.titel.lowercase() } // abgeschlossene immer A–Z
                sortedOpen + sortedDone
            } else {
                sortByOrder(list, sortOrder)
            }
        }


    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        backgroundColor = MaterialTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colors.primary.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth(0.8f).height(IntrinsicSize.Min)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Filled.Search, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        placeholder = { Text("Suche nach Titel...") },
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                                        modifier = Modifier.weight(1f).focusRequester(focusRequester)
                                    )
                                }
                            }
                        }
                    } else {
                        Text("My Bookshelf")
                    }
                },
                actions = {
                    if (isSearchActive) {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Suche beenden")
                        }
                    } else {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Filled.Search, contentDescription = "Suche")
                        }
                        // SORTIER-Icon mit Dropdown
                        Box {
                            IconButton(onClick = { sortDropdownExpanded = true }) {
                                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sortieren")
                            }
                            DropdownMenu(
                                expanded = sortDropdownExpanded,
                                onDismissRequest = { sortDropdownExpanded = false },
                            ) {
                                // Liste der Sortieroptionen (ohne "Abgeschlossen zuletzt")
                                val sortOptions = listOf(
                                    MangaSortOrder.TITLE_ASC,
                                    MangaSortOrder.TITLE_DESC,
                                    MangaSortOrder.DATE_ADDED_ASC,
                                    MangaSortOrder.DATE_ADDED_DESC,
                                    MangaSortOrder.DATE_MODIFIED_ASC,
                                    MangaSortOrder.DATE_MODIFIED_DESC
                                )

                                sortOptions.forEach { order ->
                                    DropdownMenuItem(
                                        onClick = {
                                            sortOrder = order
                                            sortDropdownExpanded = false
                                            scope.launch {
                                                context.dataStore.edit { prefs ->
                                                    prefs[SORT_ORDER_KEY] = order.name
                                                }
                                            }
                                        }
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(order.displayName)
                                            if (order == sortOrder) {
                                                Spacer(modifier = Modifier.weight(1f))
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = "Ausgewählt",
                                                    tint = MaterialTheme.colors.primary
                                                )
                                            }
                                        }
                                    }
                                }

                                Divider()

                                // Checkbox "Abgeschlossene zuletzt" als letzter Eintrag
                                DropdownMenuItem(
                                    onClick = {
                                        val newValue = !completedLastEnabled
                                        completedLastEnabled = newValue
                                        scope.launch {
                                            context.dataStore.edit { prefs ->
                                                prefs[COMPLETED_LAST_KEY] = newValue
                                            }
                                        }
                                    }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = completedLastEnabled,
                                            onCheckedChange = { checked ->
                                                completedLastEnabled = checked
                                                scope.launch {
                                                    context.dataStore.edit { prefs ->
                                                        prefs[COMPLETED_LAST_KEY] = checked
                                                    }
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Abgeschlossene zuletzt")
                                    }
                                }
                            }
                        }
                        // NEU: Wunschliste
                        IconButton(onClick = { navController.navigate("wishlist") }) {
                            Icon(Icons.Filled.BookmarkBorder, contentDescription = "Wunschliste")
                        }

                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Einstellungen")
                        }
                    }
                },
                backgroundColor = MaterialTheme.colors.primary
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.navigationBarsPadding(),
                onClick = { navController.navigate("mangaAdd") },
                backgroundColor = MaterialTheme.colors.secondary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Book")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(8.dp)
        ) {
            items(filteredManga) { manga ->
                MangaListItem(manga, navController)
            }
        }
    }
}


fun readFileAsBase64(path: String): String? {
    val file = File(path)
    if (!file.exists()) return null
    val bytes = file.readBytes()
    return Base64.encodeToString(bytes, Base64.DEFAULT)
}

fun writeFileFromBase64(context: Context, base64: String): String {
    val bytes = Base64.decode(base64, Base64.DEFAULT)
    val file = File(context.filesDir, "${UUID.randomUUID()}.jpg")
    file.outputStream().use { it.write(bytes) }
    return file.absolutePath
}

fun writeAudioFromBase64(context: Context, base64: String, extension: String = "m4a"): String {
    val bytes = Base64.decode(base64, Base64.DEFAULT)
    val file = File(context.filesDir, "${UUID.randomUUID()}.$extension")
    file.outputStream().use { it.write(bytes) }
    return file.absolutePath
}


/** Einzelner Eintrag mit SwipeToDismiss (grau als Background). */
@Composable
fun MangaListItem(
    manga: MangaEntity,
    navController: NavController,
) {
    // Statt SwipeToDismiss nutzen wir direkt eine Card mit Clickable
    Card(
        elevation = 6.dp,
        backgroundColor = MaterialTheme.colors.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { navController.navigate("mangaDetail/${manga.id}") }
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            val coverModel = if (manga.coverUri.isNullOrBlank()) null else manga.coverUri

            // Bild / Placeholder
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(coverModel)
                    .fallback(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .build(),
                contentDescription = "Cover",
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Titel und Fortschritt
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = manga.titel,
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                val progress = manga.aktuellerBand.toFloat() /
                        manga.gekaufteBände.coerceAtLeast(1).toFloat()

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colors.primaryVariant
                )

                Text(
                    text = "Gelesen: ${manga.aktuellerBand}/${manga.gekaufteBände}",
                    style = MaterialTheme.typography.body2
                )

                // Status-Anzeige
                if (manga.isCompleted) {
                    Text(
                        text = "Status: Abgeschlossen",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.primary
                    )
                } else if (!manga.nextVolumeDate.isNullOrBlank()) {
                    Text(
                        text = "Nächster Band: ${manga.nextVolumeDate}",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.primary
                    )
                }
            }
        }
    }
}



/** Neues Buch hinzufügen (ohne KeyboardOptions). */
@Composable
fun MangaAddScreen(navController: NavController) {
    val viewModel: MangaViewModel = viewModel()
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var currentVolume by remember { mutableStateOf("0") }
    var ownedVolumes by remember { mutableStateOf("0") }
    var coverPath by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            coverPath = viewModel.saveImage(context, it)
        }
    }

    val isValid = title.isNotBlank() &&
            (currentVolume.toIntOrNull() != null) &&
            (ownedVolumes.toIntOrNull() != null)

    Scaffold(
        modifier = Modifier.systemBarsPadding(), // <- hinzufügen
        backgroundColor = MaterialTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Neues Buch hinzufügen") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                backgroundColor = MaterialTheme.colors.primary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titel*") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (coverPath != null) "Cover ändern" else "Cover auswählen")
            }
            Spacer(modifier = Modifier.height(16.dp))
            NumberInputField(
                value = currentVolume,
                onValueChange = { currentVolume = it },
                label = "Gelesen"
            )
            Spacer(modifier = Modifier.height(8.dp))
            NumberInputField(
                value = ownedVolumes,
                onValueChange = { ownedVolumes = it },
                label = "Gekaufte Bände"
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.addManga(
                        MangaEntity(
                            titel = title,
                            coverUri = coverPath,
                            aktuellerBand = currentVolume.toInt(),
                            gekaufteBände = ownedVolumes.toInt()
                        )
                    )
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isValid
            ) {
                Text("Buch speichern")
            }
        }
    }
}

/**
 * Einfaches Eingabefeld mit +/− Buttons zum Ändern einer Zahl.
 * Keine KeyboardOptions mehr.
 */
@Composable
fun NumberInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = {
                val newValue = (value.toIntOrNull() ?: 0) - 1
                onValueChange(newValue.coerceAtLeast(0).toString())
            }
        ) {
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "-")
        }

        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                if (input.toIntOrNull() != null) {
                    onValueChange(input)
                }
            },
            modifier = Modifier.weight(1f),
            label = { Text(label) }
        )

        IconButton(
            onClick = {
                val newValue = (value.toIntOrNull() ?: 0) + 1
                onValueChange(newValue.toString())
            }
        ) {
            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "+")
        }
    }
}

/**
 * Detail-Screen liest neu in "LaunchedEffect(manga)",
 * damit die Felder immer die korrekten DB-Werte zeigen.
 */
@Composable
fun MangaDetailScreen(mangaId: String, navController: NavController) {
    val viewModel: MangaViewModel = viewModel()
    val mangaList by viewModel.mangaList.collectAsState(initial = emptyList())
    val manga = mangaList.find { it.id == mangaId }

    // Zustände für Haupt-Buchdetails
    var currentVolume by remember { mutableStateOf("0") }
    var ownedVolumes by remember { mutableStateOf("0") }
    var menuExpanded by remember { mutableStateOf(false) }

    // Bild-Auswahl
    val context = LocalContext.current
    val coverPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val newCover = viewModel.saveImage(context, it)
            manga?.let { original ->
                viewModel.updateManga(original.copy(coverUri = newCover))
            }
        }
    }

    // Initiale Werte übernehmen
    LaunchedEffect(manga) {
        if (manga != null) {
            currentVolume = manga.aktuellerBand.toString()
            ownedVolumes = manga.gekaufteBände.toString()
        }
    }

    // Zurücknavigieren, wenn Update fertig
    LaunchedEffect(viewModel.updateSuccess.value) {
        if (viewModel.updateSuccess.value) {
            navController.popBackStack()
            viewModel.resetUpdateStatus()
        }
    }

    // Sonderreihen
    var showAddSeriesDialog by remember { mutableStateOf(false) }
    var newSeriesName by remember { mutableStateOf("") }

    var specialSeriesList by remember { mutableStateOf<List<SpecialSeriesEntity>>(emptyList()) }
    LaunchedEffect(manga) {
        manga?.let {
            viewModel.getSpecialSeriesForManga(it.id).collect { series ->
                specialSeriesList = series
            }
        }
    }

    //Audio An Aus
    var showDisableAudioConfirm by remember { mutableStateOf(false) } // NEU


    // Umbenennen einer Sonderreihe
    var editingSeriesId by remember { mutableStateOf<String?>(null) }
    var editingSeriesName by remember { mutableStateOf("") }
    var showEditSeriesDialog by remember { mutableStateOf(false) }

    //Umbennen der hauptreihe
    var showRenameDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf(manga?.titel ?: "") }


    // --- Veröffentlichungstatus-Dialog ---
    var showStatusDialog by remember { mutableStateOf(false) }

    // Temporäre States für den Dialog:
    //   - ob abgeschlossen
    //   - ob Datum bekannt (falls nicht abgeschlossen)
    //   - ob Datum unbekannt
    var tempIsCompleted by remember { mutableStateOf(false) }
    var tempNextVolumeDate by remember { mutableStateOf("") }
    var tempIsUnknown by remember { mutableStateOf(false) }


    // Beim Öffnen des Screens übernehmen wir die DB-Werte in die Temp-Felder
    LaunchedEffect(manga) {
        manga?.let { currentManga ->
            tempIsCompleted = currentManga.isCompleted
            val dbDate = currentManga.nextVolumeDate
            when {
                currentManga.isCompleted -> {
                    // abgeschlossen
                    tempNextVolumeDate = ""
                    tempIsUnknown = false
                }
                dbDate == null -> {
                    // gar kein Status gesetzt
                    tempNextVolumeDate = ""
                    tempIsUnknown = false
                }
                dbDate == "UNKNOWN" -> {
                    // Laufend (Datum unbekannt)
                    tempNextVolumeDate = ""
                    tempIsUnknown = true
                }
                else -> {
                    // Laufend (Datum bekannt)
                    tempNextVolumeDate = dbDate
                    tempIsUnknown = false
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        backgroundColor = MaterialTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text(manga?.titel ?: "Buchdetails") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menü")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(onClick = {
                            menuExpanded = false
                            coverPicker.launch("image/*")
                        }) {
                            Text("Cover ändern")
                        }
                        DropdownMenuItem(onClick = {
                            menuExpanded = false
                            newTitle = manga?.titel ?: ""
                            showRenameDialog = true
                        }) {
                            Text("Titel anpassen")
                        }
                        DropdownMenuItem(onClick = {
                            menuExpanded = false
                            manga?.let { toDelete ->
                                viewModel.deleteManga(toDelete)
                            }
                        }) {
                            Text("Löschen")
                        }

                        Divider()

                        // NEU: Audio-Notiz aktiv (Checkbox)
                        DropdownMenuItem(onClick = {
                            // Toggle per Zeilenklick
                            val current = manga?.audioNoteEnabled ?: false
                            if (current) {
                                // Ausschalten -> ggf. Warn-Dialog
                                showDisableAudioConfirm = true
                            } else {
                                manga?.let { viewModel.setAudioNoteEnabled(it, true) }
                            }
                            menuExpanded = false
                        }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = manga?.audioNoteEnabled ?: false,
                                    onCheckedChange = { checked ->
                                        if (!checked) {
                                            showDisableAudioConfirm = true
                                        } else {
                                            manga?.let { viewModel.setAudioNoteEnabled(it, true) }
                                        }
                                        menuExpanded = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Audio-Notiz aktiv")
                            }
                        }
                    }

                    // Direkt unterhalb des DropdownMenus (nachdem die TopAppBar fertig ist) füge den Dialog ein:
                    if (showRenameDialog) {
                        AlertDialog(
                            onDismissRequest = { showRenameDialog = false },
                            title = { Text("Titel anpassen") },
                            text = {
                                OutlinedTextField(
                                    value = newTitle,
                                    onValueChange = { newTitle = it },
                                    label = { Text("Neuer Titel") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            confirmButton = {
                                Button(onClick = {
                                    manga?.let { currentManga ->
                                        viewModel.updateManga(currentManga.copy(titel = newTitle))
                                    }
                                    showRenameDialog = false
                                }) {
                                    Text("Speichern")
                                }
                            },
                            dismissButton = {
                                Button(onClick = { showRenameDialog = false }) {
                                    Text("Abbrechen")
                                }
                            }
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.primary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            manga?.let { currentManga ->

                // Cover
                val coverModel = if (currentManga.coverUri.isNullOrBlank()) null else currentManga.coverUri
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(coverModel)
                        .fallback(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .build(),
                    contentDescription = "Cover",
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Eingabefelder für aktuellen/g ekaufte Bände
                NumberInputField(
                    value = currentVolume,
                    onValueChange = { currentVolume = it },
                    label = "Gelesen"
                )
                Spacer(modifier = Modifier.height(16.dp))
                NumberInputField(
                    value = ownedVolumes,
                    onValueChange = { ownedVolumes = it },
                    label = "Gekaufte Bände"
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Status-Anzeige (nur, wenn tatsächlich etwas gesetzt wurde)
                when {
                    currentManga.isCompleted -> {
                        MangaStatusBadge(statusText = "Status: Abgeschlossen")
                    }
                    currentManga.nextVolumeDate == "UNKNOWN" -> {
                        MangaStatusBadge(statusText = "Status: Laufend (Datum unbekannt)")
                    }
                    !currentManga.nextVolumeDate.isNullOrBlank() -> {
                        MangaStatusBadge(statusText = "Nächster Band am ${currentManga.nextVolumeDate}")
                    }
                    // Falls isCompleted=false und nextVolumeDate=null => gar kein Status -> zeige nichts
                }

                // Audio-Notiz (nur wenn aktiviert)
                if (currentManga.audioNoteEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    AudioNoteCard(
                        manga = currentManga,
                        onSave = { path -> viewModel.setAudioNote(currentManga, path) },
                        onDelete = { viewModel.clearAudioNote(currentManga) }
                    )
                }


                // Sonderreihen nur anzeigen, wenn welche existieren
                if (specialSeriesList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    DividerWithText(text = "Sonderreihen")
                    Spacer(modifier = Modifier.height(24.dp))

                    specialSeriesList.forEach { series ->
                        SpecialSeriesItem(
                            series = series,
                            onUpdate = { updatedSeries ->
                                viewModel.updateSpecialSeries(updatedSeries)
                            },
                            onDelete = {
                                viewModel.deleteSpecialSeries(it)
                            },
                            onRename = { currentName ->
                                editingSeriesId = series.id
                                editingSeriesName = currentName
                                showEditSeriesDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Button "Aktualisieren"
                Button(
                    onClick = {
                        viewModel.updateManga(
                            currentManga.copy(
                                aktuellerBand = currentVolume.toInt(),
                                gekaufteBände = ownedVolumes.toInt()
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Aktualisieren")
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Button "Sonderreihen anlegen"
                Button(
                    onClick = { showAddSeriesDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sonderreihen anlegen")
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Button "Veröffentlichungsstatus"
                Button(
                    onClick = {
                        // Wenn der Nutzer den Dialog öffnet, übernehmen wir die aktuellen Werte
                        // (siehe LaunchedEffect weiter oben, der das ebenfalls tut).
                        showStatusDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Veröffentlichungsstatus")
                }

            } ?: Text(
                "Buch nicht gefunden",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    // --------------------------------------
    // DIALOGE
    // --------------------------------------

    // Dialog: Neue Sonderreihe
    if (showAddSeriesDialog) {
        AlertDialog(
            onDismissRequest = { showAddSeriesDialog = false },
            title = { Text("Neue Sonderreihe anlegen") },
            text = {
                OutlinedTextField(
                    value = newSeriesName,
                    onValueChange = { newSeriesName = it },
                    label = { Text("Name der Sonderreihe") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    manga?.let {
                        val newSeries = SpecialSeriesEntity(
                            parentMangaId = it.id,
                            name = newSeriesName,
                            aktuellerBand = 0,
                            gekaufteBände = 0
                        )
                        viewModel.addSpecialSeries(newSeries)
                    }
                    newSeriesName = ""
                    showAddSeriesDialog = false
                }) {
                    Text("Anlegen")
                }
            },
            dismissButton = {
                Button(onClick = { showAddSeriesDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Dialog: Sonderreihe umbenennen
    if (showEditSeriesDialog && editingSeriesId != null) {
        AlertDialog(
            onDismissRequest = { showEditSeriesDialog = false },
            title = { Text("Sonderreihe umbenennen") },
            text = {
                OutlinedTextField(
                    value = editingSeriesName,
                    onValueChange = { editingSeriesName = it },
                    label = { Text("Neuer Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    val updatedSeries = specialSeriesList.find { it.id == editingSeriesId }
                        ?.copy(name = editingSeriesName)
                    if (updatedSeries != null) {
                        viewModel.updateSpecialSeries(updatedSeries)
                    }
                    editingSeriesId = null
                    editingSeriesName = ""
                    showEditSeriesDialog = false
                }) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                Button(onClick = { showEditSeriesDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Dialog: Veröffentlichungsstatus
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Veröffentlichungsstatus festlegen") },
            text = {
                Column {
                    // Abgeschlossen
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = tempIsCompleted,
                            onClick = {
                                tempIsCompleted = true
                                tempNextVolumeDate = ""
                                tempIsUnknown = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Abgeschlossen")
                    }

                    // Laufend (Datum bekannt)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (!tempIsCompleted && !tempIsUnknown),
                            onClick = {
                                tempIsCompleted = false
                                tempIsUnknown = false
                                // tempNextVolumeDate bleibt erhalten
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Laufend (Datum bekannt)")
                    }

                    if (!tempIsCompleted && !tempIsUnknown) {
                        OutlinedTextField(
                            value = tempNextVolumeDate,
                            onValueChange = { tempNextVolumeDate = it },
                            label = { Text("Nächster Band am (z.B. 2025-08-01)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Laufend (Datum unbekannt)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (!tempIsCompleted && tempIsUnknown),
                            onClick = {
                                tempIsCompleted = false
                                tempIsUnknown = true
                                tempNextVolumeDate = ""
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Laufend (Datum unbekannt)")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val currentManga = manga ?: return@Button

                    // Bauen wir den finalen Zustand
                    val finalIsCompleted = tempIsCompleted
                    val finalNextVolumeDate: String? = when {
                        finalIsCompleted -> null
                        !finalIsCompleted && tempIsUnknown -> "UNKNOWN"
                        else -> {
                            // falls der Nutzer nichts eingegeben hat, kann es auch "" sein
                            tempNextVolumeDate.ifBlank { null }
                        }
                    }

                    viewModel.updateMangaStatus(
                        manga = currentManga,
                        isCompleted = finalIsCompleted,
                        nextVolumeDate = finalNextVolumeDate
                    )
                    showStatusDialog = false
                }) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                Button(onClick = { showStatusDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // NEU: Bestätigen, dass Audio-Notiz deaktiviert wird (löscht Aufnahme)
    if (showDisableAudioConfirm) {
        AlertDialog(
            onDismissRequest = { showDisableAudioConfirm = false },
            title = { Text("Audio-Notiz deaktivieren?") },
            text = { Text("Wenn du die Audio-Notiz deaktivierst, wird die gespeicherte Aufnahme falls vorhanden gelöscht. Fortfahren?") },
            confirmButton = {
                Button(onClick = {
                    val current = manga ?: return@Button
                    // ggf. Wiedergabe stoppen und Datei löschen übernimmt ViewModel
                    viewModel.setAudioNoteEnabled(current, false)
                    showDisableAudioConfirm = false
                }) {
                    Text("Ja, löschen")
                }
            },
            dismissButton = {
                Button(onClick = { showDisableAudioConfirm = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

}



@Composable
fun WishlistScreen(navController: NavController) {
    val viewModel: MangaViewModel = viewModel()
    val items by viewModel.wishlist.collectAsState(initial = emptyList())
    var input by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        backgroundColor = MaterialTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Wunschliste") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                backgroundColor = MaterialTheme.colors.primary
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (input.isNotBlank()) {
                        viewModel.addWishlistItem(input)
                        input = ""
                    }
                },
                backgroundColor = MaterialTheme.colors.secondary,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Hinzufügen")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(12.dp)) {

            // Eingabe
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Manga-Titel (Freitext)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            // Liste
            LazyColumn {
                items(items) { itx ->
                    Card(
                        elevation = 4.dp,
                        backgroundColor = MaterialTheme.colors.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                itx.title,
                                style = MaterialTheme.typography.h6,
                                color = MaterialTheme.colors.primary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.deleteWishlistItem(itx) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Löschen")
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun SpecialSeriesItem(
    series: SpecialSeriesEntity,
    onUpdate: (SpecialSeriesEntity) -> Unit,
    onDelete: (SpecialSeriesEntity) -> Unit,
    onRename: (String) -> Unit
) {
    var currentVolume by remember { mutableStateOf(series.aktuellerBand.toString()) }
    var ownedVolumes by remember { mutableStateOf(series.gekaufteBände.toString()) }
    var menuExpanded by remember { mutableStateOf(false) }


    Card(
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(series.name, style = MaterialTheme.typography.h6, modifier = Modifier.weight(1f))
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Optionen")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(onClick = {
                        menuExpanded = false
                        onRename(series.name)
                    }) {
                        Text("Umbenennen")
                    }
                    DropdownMenuItem(onClick = {
                        menuExpanded = false
                        onDelete(series)
                    }) {
                        Text("Löschen")
                    }
                }
            }
            // Eingabefelder für "gelesen" und "gekaufte Bände"
            NumberInputField(
                value = currentVolume,
                onValueChange = {
                    currentVolume = it
                    onUpdate(series.copy(aktuellerBand = it.toIntOrNull() ?: 0))
                },
                label = "Gelesen"
            )
            Spacer(modifier = Modifier.height(8.dp))
            NumberInputField(
                value = ownedVolumes,
                onValueChange = {
                    ownedVolumes = it
                    onUpdate(series.copy(gekaufteBände = it.toIntOrNull() ?: 0))
                },
                label = "Gekaufte Bände"
            )
        }
    }
}

@Composable
fun DividerWithText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
    thickness: Dp = 1.dp,
    horizontalPadding: Dp = 8.dp,
    textPadding: Dp = 16.dp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
    ) {
        // Linie links
        Divider(
            modifier = Modifier.weight(1f),
            color = color,
            thickness = thickness
        )

        // Text in der Mitte
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = textPadding),
            color = color, // oder eigenes Color-Objekt
            style = MaterialTheme.typography.body2
        )

        // Linie rechts
        Divider(
            modifier = Modifier.weight(1f),
            color = color,
            thickness = thickness
        )
    }
}

@Composable
fun MangaStatusBadge(
    statusText: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Info,
    backgroundColor: Color = MaterialTheme.colors.primary.copy(alpha = 0.15f)
) {
    // Eine kleine "Chip"-ähnliche Oberfläche
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small, // abgerundete Ecken
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colors.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface
            )
        }
    }
}

@Composable
fun AudioNoteCard(
    manga: MangaEntity,
    onSave: (String) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }

    // Runtime-Permission
    var hasMicPermission by remember { mutableStateOf(false) }
    val requestMicPermission = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        hasMicPermission = granted
    }
    LaunchedEffect(Unit) {
        // simpler Versuch; wenn verweigert, fragen wir beim ersten Klick
        hasMicPermission = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    // Ziel-Datei für diese Reihe
    fun audioFilePath(): String {
        val file = File(context.filesDir, "audio_${manga.id}.m4a")
        return file.absolutePath
    }

    fun startRecording() {
        if (!hasMicPermission) {
            requestMicPermission.launch(android.Manifest.permission.RECORD_AUDIO)
            return
        }
        val path = audioFilePath()
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOutputFile(path)
            prepare()
            start()
        }
        isRecording = true
    }

    fun stopRecording(save: Boolean) {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {}
        mediaRecorder = null
        isRecording = false
        if (save) onSave(audioFilePath())
    }

    fun startPlayback() {
        val path = manga.audioNoteUri ?: return
        mediaPlayer = MediaPlayer().apply {
            setDataSource(path)
            prepare()
            start()
            setOnCompletionListener {
                isPlaying = false
                it.release()
                mediaPlayer = null
            }
        }
        isPlaying = true
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        isPlaying = false
    }

    // UI
    Card(
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colors.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Audio-Notiz", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1) Aufnehmen / Stopp (Speichern)
                Button(
                    onClick = {
                        if (!isRecording) startRecording() else stopRecording(save = true)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = MaterialTheme.colors.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (!isRecording) Icons.Filled.Mic else Icons.Filled.Stop,
                        contentDescription = if (!isRecording) "Aufnehmen" else "Stopp"
                    )
                }

                // 2) Abspielen / Stopp
                Button(
                    onClick = {
                        if (!isPlaying) startPlayback() else stopPlayback()
                    },
                    enabled = manga.audioNoteUri != null,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = MaterialTheme.colors.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (!isPlaying) Icons.Filled.PlayArrow else Icons.Filled.Stop,
                        contentDescription = if (!isPlaying) "Abspielen" else "Stopp"
                    )
                }

                // 3) Löschen
                Button(
                    onClick = {
                        stopPlayback()
                        onDelete()
                        manga.audioNoteUri?.let { runCatching { File(it).delete() } }
                    },
                    enabled = manga.audioNoteUri != null,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.error,
                        contentColor = MaterialTheme.colors.onError
                    )
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Löschen")
                }
            }


            if (manga.audioNoteUpdatedAt != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Zuletzt aktualisiert: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date(manga.audioNoteUpdatedAt))}",
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}