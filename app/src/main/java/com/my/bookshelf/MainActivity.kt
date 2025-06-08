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
import android.os.Build
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
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material.TextFieldDefaults
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb


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
        surface = Color(0xFF1E1E1E),       // Etwas helleres Schwarz für Oberflächen
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

    private val _updateSuccess = mutableStateOf(false)
    val updateSuccess: State<Boolean> = _updateSuccess

    fun addManga(manga: MangaEntity) = viewModelScope.launch {
        dao.insert(manga)
    }

    fun updateManga(manga: MangaEntity) = viewModelScope.launch {
        dao.update(manga)
        _updateSuccess.value = true
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

}

@OptIn(ExperimentalMaterialApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        // Nach setContent oder davor:
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        // Optional: Navigation bar divider entfernen (bei manchen Herstellern nötig)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = android.graphics.Color.TRANSPARENT
        }
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
            WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = false
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
            }
        }
    }
}



/** Settings-Screen (Dark Mode). */
@OptIn(ExperimentalMaterialApi::class)
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
    // Launcher zum Erstellen eines Backups (Export)
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { documentUri ->
            context.contentResolver.openOutputStream(documentUri)?.use { stream ->

                // 1) Alle Manga aus der DB laden
                val allManga = mangaList // => already collectedAsState in your code

                // 2) Für jedes Manga zusätzlich Sonderreihen laden
                val backupMangaList = allManga.map { mangaEntity ->

                    // Sonderreihen laden
                    val specialList: List<SpecialSeriesEntity> = runBlocking {
                        try {
                            MangaDatabase.getDatabase(context)
                                .specialSeriesDao()
                                .getSpecialSeriesForManga(mangaEntity.id)
                                .first()
                        } catch(e: NoSuchElementException) {
                            emptyList()
                        }
                    }

                    // In ExportDto konvertieren
                    val coverB64 = if (!mangaEntity.coverUri.isNullOrBlank()) {
                        readFileAsBase64(mangaEntity.coverUri)
                    } else null

                    // Sonderreihen in DTO-Objekte umwandeln
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
                        isCompleted = mangaEntity.isCompleted,       // Neu
                        nextVolumeDate = mangaEntity.nextVolumeDate, // Neu
                        specialSeries = specialSeriesDto             // Neu
                    )
                }

                // 3) In eine BackupData-Struktur stecken
                val backupData = BackupData(
                    version = 2, // Wir sagen, das ist unser "Format v2"
                    mangaList = backupMangaList
                )

                // 4) Als JSON serialisieren und schreiben
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
                    // Versuche zuerst, das Backup als komplettes Objekt zu parsen.
                    val backupData = try {
                        Gson().fromJson(json, BackupData::class.java)
                    } catch (e: Exception) {
                        // Falls das fehlschlägt, versuche es als Array von MangaExportDto zu parsen
                        try {
                            val mangaList: List<MangaExportDto> =
                                Gson().fromJson(json, Array<MangaExportDto>::class.java).toList()
                            // Erstelle BackupData mit einer alten Version (z.B. Version 1)
                            BackupData(version = 1, mangaList = mangaList)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (backupData == null) {
                        // Hier könntest du eine Fehlermeldung anzeigen oder anderweitig reagieren
                        return@launch
                    }
                    // Für jedes Backup-Manga-DTO
                    backupData.mangaList.forEach { dto ->
                        // Alte Backups haben eventuell die Felder nicht – hier Defaultwerte:
                        val isCompleted = dto.isCompleted ?: false
                        val nextVolumeDate = dto.nextVolumeDate // bleibt null, wenn nicht gesetzt
                        val specialSeriesList = dto.specialSeries ?: emptyList()
                        // Cover decodieren
                        val realCoverPath = dto.coverBase64?.let { b64 ->
                            writeFileFromBase64(context, b64)
                        }
                        // Manga in DB einfügen
                        val mangaEntity = MangaEntity(
                            id = dto.id,
                            titel = dto.titel,
                            coverUri = realCoverPath,
                            aktuellerBand = dto.aktuellerBand,
                            gekaufteBände = dto.gekaufteBaende,
                            isCompleted = isCompleted,
                            nextVolumeDate = nextVolumeDate
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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
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


/** Liste aller Mangas mit hellem Design. */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MangaListScreen(navController: NavController) {
    val viewModel: MangaViewModel = viewModel()
    val mangaList by viewModel.mangaList.collectAsState(initial = emptyList())

    // Suchzustände
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Fokus-Requester für das Suchfeld
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    // Gefilterte Liste
    val filteredManga = if (searchQuery.isBlank()) {
        mangaList
    } else {
        mangaList.filter { it.titel.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        modifier = Modifier.systemBarsPadding(), // <- hinzufügen
        backgroundColor = MaterialTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        // Statt das TextField voll breit zu machen, in eine kleinere Surface fassen und zentrieren
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colors.primary.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)   // ca. 80% der Breite
                                    .height(IntrinsicSize.Min)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colors.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        placeholder = { Text("Suche nach Titel...") },
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colors.onPrimary
                                        ),
                                        colors = TextFieldDefaults.outlinedTextFieldColors(
                                            backgroundColor = Color.Transparent,
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent,
                                            textColor = MaterialTheme.colors.onPrimary,
                                            cursorColor = MaterialTheme.colors.onPrimary,
                                            placeholderColor = MaterialTheme.colors.onPrimary.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .focusRequester(focusRequester)
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
                        // "Close"-Button
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Suche beenden")
                        }
                    } else {
                        // "Search"-Button
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Filled.Search, contentDescription = "Suche")
                        }
                        // "Settings"-Button
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
            modifier = Modifier
                .padding(padding)
                .padding(8.dp)
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

/** Einzelner Eintrag mit SwipeToDismiss (grau als Background). */
@OptIn(ExperimentalMaterialApi::class)
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
@OptIn(ExperimentalMaterialApi::class)
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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
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
@OptIn(ExperimentalMaterialApi::class)
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
@OptIn(ExperimentalMaterialApi::class)
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
        backgroundColor = MaterialTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text(manga?.titel ?: "Buchdetails") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
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
                            // Neuer Menüeintrag: Titel anpassen
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
                            if (tempNextVolumeDate.isBlank()) null else tempNextVolumeDate
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




