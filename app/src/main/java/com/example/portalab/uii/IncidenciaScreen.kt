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
import com.google.firebase.firestore.ktx.toObject
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import com.google.firebase.firestore.QuerySnapshot
import com.example.portalab.uii.*
import com.example.portalab.model.Incidencia
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.unit.sp
import com.example.portalab.model.Laboratorio
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.auth.FirebaseAuth





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidenciaScreen(drawerState: DrawerState, scope: CoroutineScope) {
    val db = FirebaseFirestore.getInstance()
    var incidencias by remember { mutableStateOf(listOf<Incidencia>()) }
    var showDialog by remember { mutableStateOf(false) }

    // Cargar incidencias al iniciar
    LaunchedEffect(Unit) {
        val snapshot = db.collection("incidencias").get().await()
        incidencias = snapshot.documents.mapNotNull { it.toObject(Incidencia::class.java) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Incidencias") },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(8.dp)) {

            incidencias.forEach {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Laboratorio: ${it.laboratorioId}", style = MaterialTheme.typography.titleMedium)
                        if (it.equipoId.isNotBlank()) {
                            Text("Equipo ID: ${it.equipoId}")
                        }
                        Text("Estado: ${it.estado}", color = Color.Gray)
                        Text("Fecha: ${it.fechaReporte}", fontSize = 12.sp)
                        Text("Descripción: ${it.descripcion}", fontSize = 14.sp)
                        Text("Reportado por: ${it.reportadoPor}", fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }

    if (showDialog) {
        DialogAgregarIncidencia(
            onDismiss = { showDialog = false },
            onAgregar = { nuevaIncidencia ->
                db.collection("incidencias").add(nuevaIncidencia).addOnSuccessListener {
                    incidencias = incidencias + nuevaIncidencia
                    showDialog = false
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogAgregarIncidencia(
    onDismiss: () -> Unit,
    onAgregar: (Incidencia) -> Unit
) {
    var descripcion by remember { mutableStateOf("") }
    var laboratorioSeleccionado by remember { mutableStateOf("") }
    val estado = "Pendiente"
    val fechaReporte = remember {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    }

    //val reportadoPor = "admin" // puedes sustituirlo luego por el usuario logueado
    val usuarioActual = FirebaseAuth.getInstance().currentUser
    val reportadoPor = usuarioActual?.email ?: "anónimo"
    val db = FirebaseFirestore.getInstance()

    var laboratorios by remember { mutableStateOf<List<Laboratorio>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }

    // Cargar laboratorios desde Firestore
    LaunchedEffect(Unit) {
        val snapshot = db.collection("laboratorios").get().await()
        laboratorios = snapshot.documents.mapNotNull { it.toObject(Laboratorio::class.java) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Nueva Incidencia", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = laboratorioSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Laboratorio") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        laboratorios.forEach {
                            DropdownMenuItem(
                                text = { Text(it.nombre) },
                                onClick = {
                                    laboratorioSeleccionado = it.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        if (descripcion.isNotBlank() && laboratorioSeleccionado.isNotBlank()) {
                            onAgregar(
                                Incidencia(
                                    descripcion = descripcion.trim(),
                                    estado = estado,
                                    laboratorioId = laboratorioSeleccionado,
                                    fechaReporte = fechaReporte,
                                    reportadoPor = reportadoPor
                                )
                            )
                        }
                    }) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

