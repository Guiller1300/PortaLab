package com.example.portalab.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.portalab.model.Equipo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import androidx.compose.material3.Button
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen(
    drawerState: DrawerState, coroutineScope: CoroutineScope
) {
    val equipos = remember { mutableStateListOf<Equipo>() }
    var showDialog by remember { mutableStateOf(false) }
    var equipoAEliminar by remember { mutableStateOf<Equipo?>(null) }
    var equipoAEditar by remember { mutableStateOf<Equipo?>(null) }
    var loading by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }

    var buscarActivo by remember { mutableStateOf(false) } // Controla si el campo de búsqueda está activo


    val snackbarHostState = remember { SnackbarHostState() } // Estado para la barra de notificaciones
    val coroutineScope = rememberCoroutineScope() // CoroutineScope para manejar corutinas

    var sortOption by remember { mutableStateOf("Nombre") } // Opción de ordenación inicial
    var sortMenuExpanded by remember { mutableStateOf(false) } // Controla si el menú de ordenación está abierto


    // 🔄 Cargar datos
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("equipos")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.mapNotNull { it.toObject<Equipo>() }
                equipos.clear()
                equipos.addAll(lista)
                loading = false
            }
            .addOnFailureListener {
                loading = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Error al cargar equipos")
                }
            }
    }

    Scaffold(
        topBar = { // Barra de navegación superior
            TopAppBar(
                title = {
                    if (buscarActivo) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text("Buscar equipo...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        Text("Inventarios")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        coroutineScope.launch { drawerState.open() }
                    }) {
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

                    Box {
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
                                text = { Text("Ordenar por Estado") },
                                onClick = {
                                    sortOption = "Estado"
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Ordenar por Inventario") },
                                onClick = {
                                    sortOption = "Inventario"
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }

                    IconButton(onClick = {
                        // Aquí va tu lógica de menú ⋮
                    }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más")
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

        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar equipo")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Cargando equipos...")
                    }
                }

                equipos.isEmpty() -> {
                    Text("No hay equipos registrados", modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    val filtrados = equipos.filter {
                        it.nombre.contains(query, ignoreCase = true) ||
                                it.inventario.contains(query, ignoreCase = true) ||
                                it.estado.contains(query, ignoreCase = true) ||
                                it.marca.contains(query, ignoreCase = true) ||
                                it.procesador.contains(query, ignoreCase = true) ||
                                it.laboratorioId.contains(query, ignoreCase = true) ||
                                it.ram.toString().contains(query)
                    }.sortedWith(
                        when (sortOption) {
                            "Nombre" -> compareBy { it.nombre }
                            "Estado" -> compareBy { it.estado }
                            "Inventario" -> compareBy { it.inventario }
                            else -> compareBy { it.nombre }
                        }
                    )
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        LazyColumn {
                            items(filtrados) { equipo ->
                                EquipoCard(
                                    equipo = equipo,
                                    onDelete = { equipoAEliminar = it },
                                    onEdit = { equipoAEditar = it }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo Agregar
    if (showDialog) {
        AgregarEquipoDialog(
            onDismiss = { showDialog = false },
            onCreate = { nuevo ->
                FirebaseFirestore.getInstance().collection("equipos")
                    .add(nuevo)
                    .addOnSuccessListener {
                        equipos.add(nuevo)
                        showDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Equipo agregado con éxito")
                        }
                    }
                    .addOnFailureListener {
                        showDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Error al agregar equipo")
                        }
                    }
            }
        )
    }

    // Diálogo Editar
    equipoAEditar?.let { equipo ->
        EditarEquipoDialog(
            equipo = equipo,
            onDismiss = { equipoAEditar = null },
            onSave = { actualizado ->
                FirebaseFirestore.getInstance().collection("equipos")
                    .whereEqualTo("inventario", actualizado.inventario)
                    .get()
                    .addOnSuccessListener { result ->
                        result.forEach { doc ->
                            doc.reference.set(actualizado)
                        }
                        val index = equipos.indexOfFirst { it.inventario == actualizado.inventario }
                        if (index != -1) equipos[index] = actualizado
                        equipoAEditar = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Equipo actualizado correctamente")
                        }
                    }
                    .addOnFailureListener {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Error al editar equipo")
                        }
                    }
            }
        )
    }

    // Diálogo Eliminar
    equipoAEliminar?.let { equipo ->
        AlertDialog(
            onDismissRequest = { equipoAEliminar = null },
            title = { Text("Eliminar equipo") },
            text = { Text("¿Estás seguro que deseas eliminar '${equipo.nombre}'?") },
            confirmButton = {
                TextButton(onClick = {
                    FirebaseFirestore.getInstance().collection("equipos")
                        .whereEqualTo("inventario", equipo.inventario)
                        .get()
                        .addOnSuccessListener { result ->
                            for (doc in result) {
                                doc.reference.delete()
                            }
                            equipos.remove(equipo)
                            equipoAEliminar = null
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Equipo eliminado")
                            }
                        }
                        .addOnFailureListener {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Error al eliminar equipo")
                            }
                        }
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { equipoAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}



@Composable
fun EquipoCard(
    equipo: Equipo,
    onDelete: (Equipo) -> Unit,
    onEdit: (Equipo) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = equipo.nombre,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "${equipo.inventario} • ${equipo.estado} • ${equipo.descripcion}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Marca: ${equipo.marca} • CPU: ${equipo.procesador} • RAM: ${equipo.ram}GB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Lab: ${equipo.laboratorioId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            var expanded by remember { mutableStateOf(false) }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            expanded = false
                            onEdit(equipo)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        onClick = {
                            expanded = false
                            onDelete(equipo)
                        }
                    )
                }
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarEquipoDialog(
    onDismiss: () -> Unit,
    onCreate: (Equipo) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var inventario by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var procesador by remember { mutableStateOf("") }
    var ram by remember { mutableStateOf("") }
    var laboratorioId by remember { mutableStateOf("") }
    var estadoExpanded by remember { mutableStateOf(false) }
    var estado by remember { mutableStateOf("Excelente") }

    val estados = listOf("Excelente", "Bueno", "Malo", "Dañado", "Inservible")

    var laboratorioExpanded by remember { mutableStateOf(false) }
    var laboratorios by remember { mutableStateOf(listOf<String>()) }

    // 🔄 Carga de laboratorios desde Firestore
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("laboratorios")
            .get()
            .addOnSuccessListener { snapshot ->
                laboratorios = snapshot.documents.mapNotNull { it.getString("nombre") }
            }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text("Agregar nuevo equipo", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del equipo") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = inventario,
                    onValueChange = { inventario = it },
                    label = { Text("Inventario") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = marca,
                    onValueChange = { marca = it },
                    label = { Text("Marca") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = procesador,
                    onValueChange = { procesador = it },
                    label = { Text("Procesador") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = ram,
                    onValueChange = { ram = it },
                    label = { Text("RAM (GB)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = estadoExpanded,
                    onExpandedChange = { estadoExpanded = !estadoExpanded }
                ) {
                    OutlinedTextField(
                        value = estado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(estadoExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = estadoExpanded,
                        onDismissRequest = { estadoExpanded = false }
                    ) {
                        estados.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    estado = it
                                    estadoExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = laboratorioExpanded,
                    onExpandedChange = { laboratorioExpanded = !laboratorioExpanded }
                ) {
                    OutlinedTextField(
                        value = laboratorioId,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Laboratorio") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(laboratorioExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = laboratorioExpanded,
                        onDismissRequest = { laboratorioExpanded = false }
                    ) {
                        laboratorios.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    laboratorioId = it
                                    laboratorioExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            onCreate(
                                Equipo(
                                    nombre = nombre.trim(),
                                    inventario = inventario.trim(),
                                    descripcion = descripcion.trim(),
                                    marca = marca.trim(),
                                    procesador = procesador.trim(),
                                    ram = ram.toIntOrNull() ?: 0,
                                    estado = estado,
                                    laboratorioId = laboratorioId.trim()
                                )
                            )
                        },
                        enabled = nombre.isNotBlank() && inventario.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarEquipoDialog(
    equipo: Equipo,
    onDismiss: () -> Unit,
    onSave: (Equipo) -> Unit
) {
    var nombre by remember { mutableStateOf(equipo.nombre) }
    var inventario by remember { mutableStateOf(equipo.inventario) }
    var descripcion by remember { mutableStateOf(equipo.descripcion) }
    var marca by remember { mutableStateOf(equipo.marca) }
    var procesador by remember { mutableStateOf(equipo.procesador) }
    var ram by remember { mutableStateOf(equipo.ram.toString()) }
    var laboratorioId by remember { mutableStateOf(equipo.laboratorioId) }
    var estadoExpanded by remember { mutableStateOf(false) }
    var estado by remember { mutableStateOf(equipo.estado) }

    val estados = listOf("Excelente", "Bueno", "Regular", "Malo", "Dañado", "Inservible")

    val laboratorioList = remember { mutableStateListOf<String>() }
    var labExpanded by remember { mutableStateOf(false) }

    // 🔄 Cargar nombres de laboratorios desde Firestore
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("laboratorios")
            .get()
            .addOnSuccessListener { result ->
                laboratorioList.clear()
                laboratorioList.addAll(result.mapNotNull { it.getString("nombre") })
            }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text("Editar equipo", style = MaterialTheme.typography.headlineSmall)

                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del equipo") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = inventario,
                    onValueChange = { inventario = it },
                    label = { Text("Inventario") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = marca,
                    onValueChange = { marca = it },
                    label = { Text("Marca") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = procesador,
                    onValueChange = { procesador = it },
                    label = { Text("Procesador") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = ram,
                    onValueChange = { ram = it },
                    label = { Text("RAM (GB)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = estadoExpanded,
                    onExpandedChange = { estadoExpanded = !estadoExpanded }
                ) {
                    OutlinedTextField(
                        value = estado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(estadoExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = estadoExpanded,
                        onDismissRequest = { estadoExpanded = false }
                    ) {
                        estados.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    estado = it
                                    estadoExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = labExpanded,
                    onExpandedChange = { labExpanded = !labExpanded }
                ) {
                    OutlinedTextField(
                        value = laboratorioId,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Laboratorio") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = labExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = labExpanded,
                        onDismissRequest = { labExpanded = false }
                    ) {
                        laboratorioList.forEach { lab ->
                            DropdownMenuItem(
                                text = { Text(lab) },
                                onClick = {
                                    laboratorioId = lab
                                    labExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            onSave(
                                equipo.copy(
                                    nombre = nombre.trim(),
                                    descripcion = descripcion.trim(),
                                    marca = marca.trim(),
                                    procesador = procesador.trim(),
                                    ram = ram.toIntOrNull() ?: 0,
                                    estado = estado,
                                    laboratorioId = laboratorioId.trim()
                                )
                            )
                        },
                        enabled = nombre.isNotBlank()
                    ) {
                        Text("Guardar cambios")
                    }
                }
            }
        }
    }
}
