package com.example.portalab.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.portalab.model.Equipo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import androidx.compose.material.icons.filled.Edit  // <--- Este es el nuevo que te falta
import androidx.compose.material3.Button
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen() {
    val equipos = remember { mutableStateListOf<Equipo>() }
    var showDialog by remember { mutableStateOf(false) }
    var equipoAEliminar by remember { mutableStateOf<Equipo?>(null) }
    var equipoAEditar by remember { mutableStateOf<Equipo?>(null) }
    var loading by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

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
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Inventario de Equipos") }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
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
                                it.laboratorio.contains(query, ignoreCase = true)
                    }

                    Column(Modifier.padding(horizontal = 16.dp)) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = { Text("Buscar equipo...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

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
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = equipo.nombre,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text("Inventario: ${equipo.inventario}", style = MaterialTheme.typography.bodySmall)
            Text("Descripción: ${equipo.descripcion}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text("Estado: ${equipo.estado}") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (equipo.estado.lowercase()) {
                            "excelente", "activo" -> MaterialTheme.colorScheme.tertiaryContainer
                            "malo", "dañado" -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                )

                Row {
                    IconButton(onClick = { onEdit(equipo) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar"
                        )
                    }
                    IconButton(onClick = { onDelete(equipo) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar"
                        )
                    }
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
    var estadoExpanded by remember { mutableStateOf(false) }
    var estado by remember { mutableStateOf("Excelente") }
    var laboratorio by remember { mutableStateOf("") }

    val estados = listOf("Excelente", "Bueno", "Regular", "Malo", "Dañado", "Inservible")

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
                Text(
                    "Agregar nuevo equipo",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del equipo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = inventario,
                    onValueChange = { inventario = it },
                    label = { Text("Número de inventario") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(8.dp))

                // Dropdown para estado
                ExposedDropdownMenuBox(
                    expanded = estadoExpanded,
                    onExpandedChange = { estadoExpanded = !estadoExpanded }
                ) {
                    OutlinedTextField(
                        value = estado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = estadoExpanded,
                        onDismissRequest = { estadoExpanded = false }
                    ) {
                        estados.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    estado = item
                                    estadoExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = laboratorio,
                    onValueChange = { laboratorio = it },
                    label = { Text("Laboratorio") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

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
                                    estado = estado,
                                    laboratorio = laboratorio.trim()
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
    var estadoExpanded by remember { mutableStateOf(false) }
    var estado by remember { mutableStateOf(equipo.estado) }
    var laboratorio by remember { mutableStateOf(equipo.laboratorio) }

    val estados = listOf("Excelente", "Bueno", "Regular", "Malo", "Dañado", "Inservible")

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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = inventario,
                    onValueChange = { inventario = it },
                    label = { Text("Inventario") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    readOnly = true // Clave para no duplicar documentos
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
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
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
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
                OutlinedTextField(
                    value = laboratorio,
                    onValueChange = { laboratorio = it },
                    label = { Text("Laboratorio") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            onSave(
                                equipo.copy(
                                    nombre = nombre.trim(),
                                    descripcion = descripcion.trim(),
                                    estado = estado,
                                    laboratorio = laboratorio.trim()
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

