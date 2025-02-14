@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mybooksheelf

import android.app.Application
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
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

// --- Domain Model & Mapping ---
data class Manga(
    val id: String = UUID.randomUUID().toString(),
    val titel: String,
    val coverUri: String? = null,
    val aktuellerBand: Int = 0,
    val gekaufteBände: Int = 0
)

fun Manga.toEntity() = MangaEntity(id, titel, coverUri, aktuellerBand, gekaufteBände)
fun MangaEntity.toDomain() = Manga(id, titel, coverUri, aktuellerBand, gekaufteBände)

// --- Repository ---
class MangaRepository(private val dao: MangaDao) {
    val allManga: Flow<List<MangaEntity>> = dao.getAllManga()
    suspend fun insert(manga: MangaEntity) = dao.insert(manga)
    suspend fun update(manga: MangaEntity) = dao.update(manga)
}

// --- ViewModel ---
class MangaViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = MangaRepository(MangaDatabase.getDatabase(app).mangaDao())
    val mangaList = repository.allManga

    fun addManga(manga: Manga) = viewModelScope.launch {
        repository.insert(manga.toEntity())
    }

    fun updateManga(manga: Manga) = viewModelScope.launch {
        repository.update(manga.toEntity())
    }
}

// --- MainActivity ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyBookSheelfApp()
        }
    }
}

// --- Composables ---
@Composable
fun MyBookSheelfApp() {
    val navController = rememberNavController()
    Scaffold(
        topBar = { TopAppBar(title = { Text("MyBookSheelf") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("mangaAdd") }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Buch hinzufügen")
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "mangaListe",
            modifier = Modifier.padding(padding)
        ) {
            composable("mangaListe") { MangaListeScreen(navController) }
            composable("mangaAdd") { MangaAddScreen(navController) }
            composable("mangaDetail/{mangaId}") { backStackEntry ->
                val mangaId = backStackEntry.arguments?.getString("mangaId") ?: ""
                MangaDetailScreen(mangaId, navController)
            }
        }
    }
}

@Composable
fun MangaListeScreen(
    navController: NavController,
    viewModel: MangaViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val mangaEntities by viewModel.mangaList.collectAsState(initial = emptyList())
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(mangaEntities) { entity ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { navController.navigate("mangaDetail/${entity.id}") }
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    if (!entity.coverUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = entity.coverUri,
                            contentDescription = "Cover Image",
                            modifier = Modifier.size(64.dp)
                        )
                    } else {
                        AsyncImage(
                            model = "https://via.placeholder.com/64",
                            contentDescription = "Placeholder",
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = entity.titel, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Gelesen: ${entity.aktuellerBand} / ${entity.gekaufteBände}")
                    }
                }
            }
        }
    }
}

@Composable
fun MangaAddScreen(
    navController: NavController,
    viewModel: MangaViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    var titel by remember { mutableStateOf("") }
    var aktuellerBandText by remember { mutableStateOf("") }
    var gekaufteBändeText by remember { mutableStateOf("") }
    var coverUri by remember { mutableStateOf("") }

    // Image Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        coverUri = uri?.toString() ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = titel,
            onValueChange = { titel = it },
            label = { Text("Buchtitel") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (coverUri.isEmpty()) "Cover hinzufügen" else "Cover ändern")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = aktuellerBandText,
            onValueChange = { aktuellerBandText = it },
            label = { Text("Aktueller Band (gelesen)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = gekaufteBändeText,
            onValueChange = { gekaufteBändeText = it },
            label = { Text("Erworbene Bände") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val aktuellerBand = aktuellerBandText.toIntOrNull() ?: 0
                val gekaufteBände = gekaufteBändeText.toIntOrNull() ?: 0
                if (titel.isNotBlank()) {
                    viewModel.addManga(
                        Manga(
                            titel = titel,
                            coverUri = coverUri,
                            aktuellerBand = aktuellerBand,
                            gekaufteBände = gekaufteBände
                        )
                    )
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Buch speichern")
        }
    }
}

@Composable
fun MangaDetailScreen(
    mangaId: String,
    navController: NavController,
    viewModel: MangaViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val manga = viewModel.mangaList.collectAsState(initial = emptyList()).value.find { it.id == mangaId }
    if (manga == null) {
        Text("Buch nicht gefunden")
        return
    }

    var aktuellerBand by remember { mutableStateOf(manga.aktuellerBand.toString()) }
    var gekaufteBände by remember { mutableStateOf(manga.gekaufteBände.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = manga.titel, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Gelesen: ${manga.aktuellerBand} / ${manga.gekaufteBände} Bände")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = aktuellerBand,
            onValueChange = { aktuellerBand = it },
            label = { Text("Neuer Stand (gelesen)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = gekaufteBände,
            onValueChange = { gekaufteBände = it },
            label = { Text("Neue Anzahl erworbener Bände") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val neuerGelesen = aktuellerBand.toIntOrNull() ?: manga.aktuellerBand
                val neueGekaufte = gekaufteBände.toIntOrNull() ?: manga.gekaufteBände
                viewModel.updateManga(
                    Manga(
                        id = manga.id,
                        titel = manga.titel,
                        coverUri = manga.coverUri,
                        aktuellerBand = neuerGelesen,
                        gekaufteBände = neueGekaufte
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Fortschritt aktualisieren")
        }
    }
}
