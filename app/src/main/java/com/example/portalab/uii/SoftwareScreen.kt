package com.example.portalab.uii

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.portalab.model.Software
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoftwareScreen(drawerState: DrawerState, scope: CoroutineScope) {
    val db = FirebaseFirestore.getInstance()
    val softwares = remember { mutableStateListOf<Software>() }
    var showDialog by remember { mutableStateOf(false) }
    var softwareAEditar by remember { mutableStateOf<Software?>(null) }
    var softwareAEliminar by remember { mutableStateOf<Software?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val localScope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var buscarActivo by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("Nombre") }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        db.collection("software").get()
            .addOnSuccessListener { result ->
                softwares.clear()
                softwares.addAll(result.mapNotNull { it.toObject(Software::class.java) })
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
                            placeholder = { Text("Buscar software...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        Text("Software Registrado")
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
                            text = { Text("Ordenar por Fabricante") },
                            onClick = {
                                sortOption = "Fabricante"
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
                Icon(Icons.Default.Add, contentDescription = "Agregar software")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val filtrados = softwares
            .filter { it.nombre.contains(query, ignoreCase = true) }
            .sortedBy {
                when (sortOption) {
                    "Nombre" -> it.nombre
                    "Fabricante" -> it.fabricante
                    else -> it.nombre
                }
            }

        LazyColumn(modifier = Modifier.padding(padding)) {
            items(filtrados) { sw ->
                var expanded by remember { mutableStateOf(false) }

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
                            Text(sw.nombre, style = MaterialTheme.typography.titleMedium)
                            Text("Versión: ${sw.version} • ${sw.fabricante}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Editar") },
                                    onClick = {
                                        expanded = false
                                        softwareAEditar = sw
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar") },
                                    onClick = {
                                        expanded = false
                                        softwareAEliminar = sw
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        DialogSoftware(onDismiss = { showDialog = false }) { nuevo ->
            db.collection("software").add(nuevo)
                .addOnSuccessListener {
                    softwares.add(nuevo)
                    showDialog = false
                    localScope.launch {
                        snackbarHostState.showSnackbar("Software agregado")
                    }
                }
        }
    }

    softwareAEditar?.let { sw ->
        DialogSoftware(
            software = sw,
            onDismiss = { softwareAEditar = null },
            onSave = { actualizado ->
                db.collection("software").whereEqualTo("nombre", sw.nombre).get()
                    .addOnSuccessListener { result ->
                        result.documents.firstOrNull()?.reference?.set(actualizado)
                        val index = softwares.indexOfFirst { it.nombre == sw.nombre }
                        if (index != -1) softwares[index] = actualizado
                        softwareAEditar = null
                        localScope.launch {
                            snackbarHostState.showSnackbar("Software actualizado")
                        }
                    }
            }
        )
    }

    softwareAEliminar?.let { sw ->
        AlertDialog(
            onDismissRequest = { softwareAEliminar = null },
            title = { Text("Eliminar software") },
            text = { Text("¿Estás seguro de eliminar '${sw.nombre}'?") },
            confirmButton = {
                TextButton(onClick = {
                    db.collection("software")
                        .whereEqualTo("nombre", sw.nombre)
                        .get()
                        .addOnSuccessListener { result ->
                            result.documents.firstOrNull()?.reference?.delete()
                            softwares.remove(sw)
                            softwareAEliminar = null
                            localScope.launch {
                                snackbarHostState.showSnackbar("Software eliminado")
                            }
                        }
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { softwareAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DialogSoftware(
    software: Software = Software(),
    onDismiss: () -> Unit,
    onSave: (Software) -> Unit
) {
    var nombre by remember { mutableStateOf(software.nombre) }
    var version by remember { mutableStateOf(software.version) }
    var fabricante by remember { mutableStateOf(software.fabricante) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Información del software", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = version,
                    onValueChange = { version = it },
                    label = { Text("Versión") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = fabricante,
                    onValueChange = { fabricante = it },
                    label = { Text("Fabricante") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        onSave(
                            Software(
                                nombre = nombre.trim(),
                                version = version.trim(),
                                fabricante = fabricante.trim()
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
