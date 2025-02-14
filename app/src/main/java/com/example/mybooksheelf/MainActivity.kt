@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mybooksheelf

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.*
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*
import com.example.mybooksheelf.MangaEntity
import com.example.mybooksheelf.MangaDatabase
import com.example.mybooksheelf.MangaDao

// --- Theme Import ---
import com.example.mybooksheelf.ui.theme.MyBookSheelfTheme


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

    fun resetUpdateStatus() {
        _updateSuccess.value = false
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Korrekte Theme-Referenz
            MyBookSheelfTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "mangaList") {
                    composable("mangaList") { MangaListScreen(navController) }
                    composable("mangaAdd") { MangaAddScreen(navController) }
                    composable("mangaDetail/{mangaId}") { backStackEntry ->
                        MangaDetailScreen(
                            mangaId = backStackEntry.arguments?.getString("mangaId") ?: "",
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MangaListScreen(navController: NavController) {
    val viewModel: MangaViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application
        )
    )
    val mangaList by viewModel.mangaList.collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Bookshelf") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("mangaAdd") }) {
                Icon(Icons.Default.Add, "Add Book")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(mangaList) { manga ->
                MangaListItem(manga, navController)
            }
        }
    }
}

@Composable
fun MangaListItem(manga: MangaEntity, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { navController.navigate("mangaDetail/${manga.id}") }
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = manga.coverUri ?: R.drawable.placeholder,
                contentDescription = "Cover",
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.placeholder)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = manga.titel,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = manga.aktuellerBand.toFloat() / manga.gekaufteBände.toFloat().coerceAtLeast(1f),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer
                )
                Text(
                    text = "Gelesen: ${manga.aktuellerBand}/${manga.gekaufteBände}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun MangaAddScreen(navController: NavController) {
    val viewModel: MangaViewModel = viewModel()
    var title by remember { mutableStateOf("") }
    var currentVolume by remember { mutableStateOf("0") }
    var ownedVolumes by remember { mutableStateOf("0") }
    var coverUri by remember { mutableStateOf<String?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        coverUri = uri?.toString()
    }

    val isValid = title.isNotBlank() && currentVolume.toIntOrNull() != null && ownedVolumes.toIntOrNull() != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Neues Buch hinzufügen") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Zurück")
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
                Text(if (coverUri != null) "Cover ändern" else "Cover auswählen")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = currentVolume,
                onValueChange = { if (it.toIntOrNull() != null) currentVolume = it },
                label = { Text("Aktueller Band*") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = ownedVolumes,
                onValueChange = { if (it.toIntOrNull() != null) ownedVolumes = it },
                label = { Text("Gekaufte Bände*") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.addManga(
                        MangaEntity(
                            titel = title,
                            coverUri = coverUri,
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
                        Icon(Icons.Default.ArrowBack, "Zurück")
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
            manga?.let {
                AsyncImage(
                    model = it.coverUri ?: R.drawable.placeholder,
                    contentDescription = "Cover",
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.placeholder)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = currentVolume,
                    onValueChange = { if (it.toIntOrNull() != null) currentVolume = it },
                    label = { Text("Aktueller Band") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ownedVolumes,
                    onValueChange = { if (it.toIntOrNull() != null) ownedVolumes = it },
                    label = { Text("Gekaufte Bände") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.updateManga(
                            it.copy(
                                aktuellerBand = currentVolume.toInt(),
                                gekaufteBände = ownedVolumes.toInt()
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Aktualisieren")
                }
            } ?: Text("Buch nicht gefunden", modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}