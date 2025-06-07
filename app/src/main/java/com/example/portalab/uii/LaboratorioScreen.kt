package com.example.portalab.uii

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.portalab.model.Laboratorio
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaboratorioScreen(drawerState: DrawerState, scope: CoroutineScope) {
    val db = FirebaseFirestore.getInstance()
    val laboratorios = remember { mutableStateListOf<Laboratorio>() }
    var showDialog by remember { mutableStateOf(false) }
    var laboratorioAEditar by remember { mutableStateOf<Laboratorio?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val localScope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var buscarActivo by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("Nombre") }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        db.collection("laboratorios").get()
            .addOnSuccessListener { result ->
                val lista = result.mapNotNull { it.toObject(Laboratorio::class.java) }
                laboratorios.clear()
                laboratorios.addAll(lista)
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (buscarActivo) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text("Buscar laboratorio...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        Text("Laboratorios")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        buscarActivo = !buscarActivo
                        if (!buscarActivo) query = ""
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }

                    IconButton(onClick = { sortMenuExpanded = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Ordenar")
                    }

                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ordenar por Nombre") },
                            onClick = {
                                sortOption = "Nombre"
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ordenar por Pabellón") },
                            onClick = {
                                sortOption = "Pabellón"
                                sortMenuExpanded = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF26C6DA),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val filtrados = laboratorios
            .filter { it.nombre.contains(query, ignoreCase = true) }
            .sortedBy {
                when (sortOption) {
                    "Nombre" -> it.nombre
                    "Pabellón" -> it.pabellon
                    else -> it.nombre
                }
            }

        LazyColumn(modifier = Modifier.padding(padding)) {
            items(filtrados) { lab ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(lab.nombre, style = MaterialTheme.typography.titleMedium)
                            Text("${lab.pabellon} • ${lab.descripcion}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { laboratorioAEditar = lab }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        DialogLaboratorio(onDismiss = { showDialog = false }) { nuevo ->
            db.collection("laboratorios").add(nuevo)
                .addOnSuccessListener {
                    laboratorios.add(nuevo)
                    showDialog = false
                    localScope.launch {
                        snackbarHostState.showSnackbar("Laboratorio agregado")
                    }
                }
        }
    }

    laboratorioAEditar?.let { lab ->
        DialogLaboratorio(
            laboratorio = lab,
            onDismiss = { laboratorioAEditar = null },
            onSave = { actualizado ->
                db.collection("laboratorios").whereEqualTo("id", lab.id).get()
                    .addOnSuccessListener { result ->
                        result.documents.firstOrNull()?.reference?.set(actualizado)
                        val index = laboratorios.indexOfFirst { it.id == lab.id }
                        if (index != -1) laboratorios[index] = actualizado
                        laboratorioAEditar = null
                        localScope.launch {
                            snackbarHostState.showSnackbar("Laboratorio actualizado")
                        }
                    }
            }
        )
    }
}

@Composable
fun DialogLaboratorio(
    laboratorio: Laboratorio = Laboratorio(),
    onDismiss: () -> Unit,
    onSave: (Laboratorio) -> Unit
) {
    var nombre by remember { mutableStateOf(laboratorio.nombre) }
    var pabellon by remember { mutableStateOf(laboratorio.pabellon) }
    var descripcion by remember { mutableStateOf(laboratorio.descripcion) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Información del laboratorio", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = pabellon,
                    onValueChange = { pabellon = it },
                    label = { Text("Pabellón") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        onSave(
                            Laboratorio(
                                id = laboratorio.id.ifEmpty { nombre },
                                nombre = nombre.trim(),
                                pabellon = pabellon.trim(),
                                descripcion = descripcion.trim()
                            )
                        )
                    }) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
