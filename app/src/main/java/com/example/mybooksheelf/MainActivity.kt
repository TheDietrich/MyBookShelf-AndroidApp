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
import androidx.compose.material.icons.Icons
// Wichtig: Diese Imports müssen exakt übereinstimmen mit dem,
// was im Code verwendet wird.
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberDismissState
import androidx.compose.material.SwipeToDismiss
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
// Hier das KeyboardOptions-Import, um das Problem zu beheben:
//import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.gson.Gson
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore

/** Einfaches M2-Theme, damit keine Konflikte mit den Imports entstehen. */
@Composable
fun MyBookSheelfTheme(content: @Composable () -> Unit) {
    MaterialTheme {
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

    MyBookSheelfTheme {
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
            composable("backup") { BackupScreen(viewModel()) }
        }
    }
}

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
        topBar = {
            TopAppBar(
                title = { Text("Einstellungen") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BackupScreen(viewModel: MangaViewModel) {
    val context = LocalContext.current

    // 1. Liste aus dem Flow als State holen
    val mangaList by viewModel.mangaList.collectAsState(emptyList())

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        // 2. Hier nicht mehr "viewModel.mangaList.value", sondern "mangaList"
        uri?.let { documentUri ->
            context.contentResolver.openOutputStream(documentUri)?.use { stream ->
                val json = Gson().toJson(mangaList) // Liste direkt
                stream.write(json.toByteArray())
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = {
            backupLauncher.launch("backup_${System.currentTimeMillis()}.json")
        }) {
            Text("Backup erstellen")
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MangaListScreen(navController: NavController) {
    val viewModel: MangaViewModel = viewModel()
    val mangaList by viewModel.mangaList.collectAsState(initial = emptyList())

    Scaffold(
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
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("mangaAdd") }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Book")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(mangaList) { manga ->
                MangaListItem(manga, navController, viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MangaListItem(
    manga: MangaEntity,
    navController: NavController,
    viewModel: MangaViewModel
) {
    // Wichtig: In M2 existiert der Parameter "confirmStateChange", nicht "confirmValueChange".
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red)
                    .padding(8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Löschen", tint = Color.White)
            }
        },
        dismissContent = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable {
                        navController.navigate("mangaDetail/${manga.id}")
                    }
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    // Placeholder muss existieren (R.drawable.placeholder)
                    AsyncImage(
                        model = manga.coverUri ?: R.drawable.placeholder,
                        contentDescription = "Cover",
                        modifier = Modifier.size(120.dp),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.placeholder)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = manga.titel,
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = manga.aktuellerBand.toFloat() /
                                    manga.gekaufteBände.toFloat().coerceAtLeast(1f),
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
        topBar = {
            TopAppBar(
                title = { Text("Neues Buch hinzufügen") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
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
            // Import: import androidx.compose.material.icons.filled.KeyboardArrowDown
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "-")
        }
        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                // Nur Zahlen erlauben
                if (input.toIntOrNull() != null) {
                    onValueChange(input)
                }
            },
            // Damit KeyboardOptions funktioniert, brauchst du:
            // implementation "androidx.compose.ui:ui"
            // und den Import:
            // import androidx.compose.ui.text.input.KeyboardOptions
            //keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            label = { Text(label) }
        )
        IconButton(
            onClick = {
                val newValue = (value.toIntOrNull() ?: 0) + 1
                onValueChange(newValue.toString())
            }
        ) {
            // Import: import androidx.compose.material.icons.filled.KeyboardArrowUp
            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "+")
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MangaDetailScreen(mangaId: String, navController: NavController) {
    val viewModel: MangaViewModel = viewModel()
    val mangaList by viewModel.mangaList.collectAsState(initial = emptyList())
    val manga = mangaList.find { it.id == mangaId }

    var currentVolume by remember { mutableStateOf(manga?.aktuellerBand?.toString() ?: "0") }
    var ownedVolumes by remember { mutableStateOf(manga?.gekaufteBände?.toString() ?: "0") }

    LaunchedEffect(viewModel.updateSuccess.value) {
        if (viewModel.updateSuccess.value) {
            navController.popBackStack()
            viewModel.resetUpdateStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(manga?.titel ?: "Buchdetails") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            manga?.let { currentManga ->
                AsyncImage(
                    model = currentManga.coverUri ?: R.drawable.placeholder,
                    contentDescription = "Cover",
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.placeholder)
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
