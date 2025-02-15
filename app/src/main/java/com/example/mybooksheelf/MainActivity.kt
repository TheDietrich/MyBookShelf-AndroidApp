package com.example.mybooksheelf

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberDismissState
import androidx.compose.material.SwipeToDismiss
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
// KeyboardOptions entfernt
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import android.util.Base64
import java.io.FileInputStream

@Composable
fun MyBookSheelfTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColors(
            primary = MaterialTheme.colors.primary,
            error = MaterialTheme.colors.error,
            background = MaterialTheme.colors.background,
            surface = MaterialTheme.colors.surface
        )
    } else {
        lightColors(
            primary = MaterialTheme.colors.primary,
            error = MaterialTheme.colors.error,
            background = MaterialTheme.colors.background,
            surface = MaterialTheme.colors.surface
        )
    }

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
}

@OptIn(ExperimentalMaterialApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainApp()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
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

    // MaterialTheme-Ansatz
    // Falls du ein eigenes Theme hast, nutze dort "darkColors()" / "lightColors()".
    MyBookSheelfTheme(darkTheme = darkTheme) {
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
            composable("backup") {
                BackupScreen(navController, viewModel())
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

    LaunchedEffect(Unit) {
        context.dataStore.data.collect { prefs ->
            darkTheme = prefs[booleanPreferencesKey("darkTheme")] ?: false
        }
    }

    Scaffold(
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
            Switch(
                checked = darkTheme,
                onCheckedChange = { enabled ->
                    darkTheme = enabled
                    scope.launch {
                        context.dataStore.edit { settings ->
                            settings[booleanPreferencesKey("darkTheme")] = enabled
                        }
                    }
                },
                modifier = Modifier.padding(16.dp)
            )
            Text("Dark Mode", modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

/** Backup-Screen. */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BackupScreen(navController: NavController, viewModel: MangaViewModel) {
    val context = LocalContext.current

    // Flow -> State
    val mangaList by viewModel.mangaList.collectAsState(emptyList())

    // Launcher, um eine JSON-Datei zu erstellen (Export)
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { documentUri ->
            context.contentResolver.openOutputStream(documentUri)?.use { stream ->
                // Hier bauen wir ein List<MangaExportDto>
                val dtoList = mangaList.map { manga ->
                    val coverB64 = if (!manga.coverUri.isNullOrBlank()) {
                        readFileAsBase64(manga.coverUri!!)
                    } else null

                    MangaExportDto(
                        id = manga.id,
                        titel = manga.titel,
                        coverBase64 = coverB64,
                        aktuellerBand = manga.aktuellerBand,
                        gekaufteBände = manga.gekaufteBände
                    )
                }
                val json = Gson().toJson(dtoList)
                stream.write(json.toByteArray())
            }
        }
    }

    // Launcher, um eine JSON-Datei (Backup) zu öffnen (Import)
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { documentUri ->
            context.contentResolver.openInputStream(documentUri)?.use { stream ->
                val json = stream.bufferedReader().use { it.readText() }
                // Hier parsen wir List<MangaExportDto>
                val dtoList = Gson().fromJson(json, Array<MangaExportDto>::class.java).toList()

                // Für jeden MangaExportDto decodieren wir das Cover
                // und fügen das in die DB ein
                dtoList.forEach { dto ->
                    val realCoverPath = dto.coverBase64?.let { b64 ->
                        writeFileFromBase64(context, b64)
                    }

                    val entity = MangaEntity(
                        id = dto.id,
                        titel = dto.titel,
                        coverUri = realCoverPath,
                        aktuellerBand = dto.aktuellerBand,
                        gekaufteBände = dto.gekaufteBände
                    )
                    // Einfügen oder updaten
                    viewModel.addManga(entity)
                }
            }
        }
    }

    Scaffold(
        backgroundColor = MaterialTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Backup") },
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
            modifier = Modifier.padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                backupLauncher.launch("backup_${System.currentTimeMillis()}.json")
            }) {
                Text("Backup erstellen")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                importLauncher.launch(arrayOf("application/json"))
            }) {
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

    Scaffold(
        backgroundColor = MaterialTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text("My Bookshelf") },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Einstellungen")
                    }
                    IconButton(onClick = { navController.navigate("backup") }) {
                        Icon(Icons.Filled.Done, contentDescription = "Backup")
                    }
                },
                backgroundColor = MaterialTheme.colors.primary
            )
        },
        floatingActionButton = {
            FloatingActionButton(
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
            items(mangaList) { manga ->
                MangaListItem(manga, navController, viewModel)
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
    viewModel: MangaViewModel
) {
    val dismissState = rememberDismissState(
        confirmStateChange = { newDismissValue ->
            if (newDismissValue == DismissValue.DismissedToEnd) {
                viewModel.deleteManga(manga)
                true
            } else false
        }
    )

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        background = {
            // Rote Card mit passender Größe (damit nichts übersteht)
            Card(
                elevation = 6.dp,
                backgroundColor = MaterialTheme.colors.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Löschen",
                        tint = Color.White
                    )
                }
            }
        },
        dismissContent = {
            Card(
                elevation = 6.dp,
                backgroundColor = MaterialTheme.colors.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        navController.navigate("mangaDetail/${manga.id}")
                    }
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
                    }
                }
            }
        }
    )
}

/**
 * Datentransfer-Objekt für Backup/Restore, enthält das Cover als Base64
 */
data class MangaExportDto(
    val id: String,
    val titel: String,
    val coverBase64: String?, // null falls kein Bild
    val aktuellerBand: Int,
    val gekaufteBände: Int
)

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
                label = "Aktueller Band"
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

    var currentVolume by remember { mutableStateOf("0") }
    var ownedVolumes by remember { mutableStateOf("0") }

    // Zeigt/hide das Menü
    var menuExpanded by remember { mutableStateOf(false) }

    // Picker für Cover
    val context = LocalContext.current
    val coverPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val newCover = viewModel.saveImage(context, it)
            // Danach wird man zurücknavigiert, wenn updateSuccess ankommt
            manga?.let { original ->
                viewModel.updateManga(original.copy(coverUri = newCover))
            }
        }
    }

    /**
     * Wenn "manga" sich ändert (z.B. neu geladen aus DB),
     * aktualisieren wir die Felder. So stehen sofort 3,4 etc. da.
     */
    LaunchedEffect(manga) {
        if (manga != null) {
            currentVolume = manga.aktuellerBand.toString()
            ownedVolumes = manga.gekaufteBände.toString()
        }
    }

    // Wenn Update fertig -> Zurück
    LaunchedEffect(viewModel.updateSuccess.value) {
        if (viewModel.updateSuccess.value) {
            navController.popBackStack()
            viewModel.resetUpdateStatus()
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
                    // Drei-Punkte-Menü
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
                            manga?.let { toDelete ->
                                viewModel.deleteManga(toDelete)
                            }
                        }) {
                            Text("Löschen")
                        }
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
            manga?.let { currentManga ->
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

                NumberInputField(
                    value = currentVolume,
                    onValueChange = { currentVolume = it },
                    label = "Aktueller Band"
                )
                Spacer(modifier = Modifier.height(16.dp))
                NumberInputField(
                    value = ownedVolumes,
                    onValueChange = { ownedVolumes = it },
                    label = "Gekaufte Bände"
                )
                Spacer(modifier = Modifier.height(16.dp))

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
            } ?: Text(
                "Buch nicht gefunden",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
