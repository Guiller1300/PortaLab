@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.portalab.uii

import android.app.DatePickerDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.portalab.model.InstalacionSoftware
import com.example.portalab.model.Software
import com.example.portalab.model.Equipo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun InstalacionSoftwareScreen(drawerState: DrawerState, scope: CoroutineScope) {
    val db = FirebaseFirestore.getInstance()
    val instalaciones = remember { mutableStateListOf<InstalacionSoftware>() }
    val softwares = remember { mutableStateListOf<Software>() }
    val equipos = remember { mutableStateListOf<Equipo>() }
    var showDialog by remember { mutableStateOf(false) }
    var instalacionAEliminar by remember { mutableStateOf<InstalacionSoftware?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val localScope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var buscarActivo by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("Software") }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var instalacionAEditar by remember { mutableStateOf<InstalacionSoftware?>(null) }

    LaunchedEffect(Unit) {
        db.collection("instalaciones").get().addOnSuccessListener { result ->
            instalaciones.clear()
            instalaciones.addAll(result.mapNotNull { it.toObject(InstalacionSoftware::class.java) })
        }
        db.collection("software").get().addOnSuccessListener { result ->
            softwares.clear()
            softwares.addAll(result.mapNotNull { it.toObject(Software::class.java) })
        }
        db.collection("equipos").get().addOnSuccessListener { result ->
            equipos.clear()
            equipos.addAll(result.mapNotNull { it.toObject(Equipo::class.java) })
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
                            placeholder = { Text("Buscar instalación...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        Text("Instalaciones registradas")
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
                            text = { Text("Ordenar por Software") },
                            onClick = {
                                sortOption = "Software"
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ordenar por Equipo") },
                            onClick = {
                                sortOption = "Equipo"
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
                Icon(Icons.Default.Add, contentDescription = "Registrar instalación")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val filtrados = instalaciones
            .filter { it.softwareId.contains(query, ignoreCase = true) || it.equipoId.contains(query, ignoreCase = true) }
            .sortedBy {
                when (sortOption) {
                    "Software" -> it.softwareId
                    "Equipo" -> it.equipoId
                    else -> it.softwareId
                }
            }

        LazyColumn(modifier = Modifier.padding(padding)) {
            items(filtrados) { item ->
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
                            Text("${item.softwareId} → ${item.equipoId}", style = MaterialTheme.typography.titleMedium)
                            Text("${item.fechaInstalacion} • ${item.licencia}", style = MaterialTheme.typography.bodySmall)
                        }
                        Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("Editar") },
                                    onClick = {
                                        expanded = false
                                        instalacionAEditar = item
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar") },
                                    onClick = {
                                        expanded = false
                                        instalacionAEliminar = item
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
        DialogInstalacionSoftware(
            softwares = softwares,
            equipos = equipos,
            onDismiss = { showDialog = false },
            onSave = { nueva ->
                db.collection("instalaciones").add(nueva).addOnSuccessListener {
                    instalaciones.add(nueva)
                    showDialog = false
                    localScope.launch {
                        snackbarHostState.showSnackbar("Instalación registrada")
                    }
                }
            }
        )
    }

    instalacionAEditar?.let { item ->
        DialogInstalacionSoftware(
            softwares = softwares,
            equipos = equipos,
            onDismiss = { instalacionAEditar = null },
            onSave = { actualizada ->
                db.collection("instalaciones")
                    .whereEqualTo("softwareId", item.softwareId)
                    .whereEqualTo("equipoId", item.equipoId)
                    .get()
                    .addOnSuccessListener { result ->
                        result.documents.firstOrNull()?.reference?.set(actualizada)
                        val index = instalaciones.indexOf(item)
                        if (index != -1) instalaciones[index] = actualizada
                        instalacionAEditar = null
                        localScope.launch {
                            snackbarHostState.showSnackbar("Instalación actualizada")
                        }
                    }
            },
            instalacion = item
        )
    }

    instalacionAEliminar?.let { item ->
        AlertDialog(
            onDismissRequest = { instalacionAEliminar = null },
            title = { Text("Eliminar instalación") },
            text = { Text("¿Deseas eliminar la instalación de ${item.softwareId} en ${item.equipoId}?") },
            confirmButton = {
                TextButton(onClick = {
                    db.collection("instalaciones")
                        .whereEqualTo("softwareId", item.softwareId)
                        .whereEqualTo("equipoId", item.equipoId)
                        .get()
                        .addOnSuccessListener { result ->
                            result.documents.firstOrNull()?.reference?.delete()
                            instalaciones.remove(item)
                            instalacionAEliminar = null
                            localScope.launch {
                                snackbarHostState.showSnackbar("Instalación eliminada")
                            }
                        }
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { instalacionAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DialogInstalacionSoftware(
    softwares: List<Software>,
    equipos: List<Equipo>,
    onDismiss: () -> Unit,
    onSave: (InstalacionSoftware) -> Unit,
    instalacion: InstalacionSoftware? = null
) {
    val context = LocalContext.current

    var softwareId by remember { mutableStateOf(instalacion?.softwareId ?: "") }
    var equipoId by remember { mutableStateOf(instalacion?.equipoId ?: "") }
    var fecha by remember { mutableStateOf(instalacion?.fechaInstalacion ?: "") }
    var licencia by remember { mutableStateOf(instalacion?.licencia ?: "") }

    var selectedSoftware by remember { mutableStateOf(instalacion?.softwareId ?: "Selecciona un software") }
    var expandedSoftware by remember { mutableStateOf(false) }

    var selectedEquipo by remember { mutableStateOf(instalacion?.equipoId ?: "Selecciona un equipo") }
    var expandedEquipo by remember { mutableStateOf(false) }

    var expandedLicencia by remember { mutableStateOf(false) }
    val licenciaOpciones = listOf("Activada", "Libre", "Trial")

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 6.dp) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(if (instalacion == null) "Registrar instalación" else "Editar instalación", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedSoftware,
                    onExpandedChange = { expandedSoftware = !expandedSoftware }
                ) {
                    OutlinedTextField(
                        value = selectedSoftware,
                        onValueChange = {},
                        label = { Text("Software") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSoftware)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSoftware,
                        onDismissRequest = { expandedSoftware = false }
                    ) {
                        softwares.forEach {
                            DropdownMenuItem(
                                text = { Text(it.nombre) },
                                onClick = {
                                    selectedSoftware = it.nombre
                                    softwareId = it.nombre
                                    expandedSoftware = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedEquipo,
                    onExpandedChange = { expandedEquipo = !expandedEquipo }
                ) {
                    OutlinedTextField(
                        value = selectedEquipo,
                        onValueChange = {},
                        label = { Text("Equipo") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEquipo)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedEquipo,
                        onDismissRequest = { expandedEquipo = false }
                    ) {
                        equipos.forEach {
                            DropdownMenuItem(
                                text = { Text(it.nombre) },
                                onClick = {
                                    selectedEquipo = it.nombre
                                    equipoId = it.nombre
                                    expandedEquipo = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = fecha,
                    onValueChange = {},
                    label = { Text("Fecha de instalación") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    fecha = "%04d-%02d-%02d".format(year, month + 1, day)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                        }
                    }
                )

                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedLicencia,
                    onExpandedChange = { expandedLicencia = !expandedLicencia }
                ) {
                    OutlinedTextField(
                        value = licencia,
                        onValueChange = {},
                        label = { Text("Tipo de licencia") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLicencia)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedLicencia,
                        onDismissRequest = { expandedLicencia = false }
                    ) {
                        licenciaOpciones.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    licencia = it
                                    expandedLicencia = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        onSave(
                            InstalacionSoftware(
                                softwareId = softwareId.trim(),
                                equipoId = equipoId.trim(),
                                fechaInstalacion = fecha.trim(),
                                licencia = licencia.trim()
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
