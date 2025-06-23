package com.example.portalab.uii

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.portalab.model.HorarioClase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.absoluteValue
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.RoundedCornerShape


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorarioScreen(drawerState: DrawerState, scope: CoroutineScope) {
    var showDialog by remember { mutableStateOf(false) }
    var horario by remember { mutableStateOf(listOf<HorarioClase>()) }
    val laboratorioId = remember { mutableStateOf("lab01") }
    var laboratorioNombre by remember { mutableStateOf("Cargando...") }

    val db = FirebaseFirestore.getInstance()

    // Cargar nombre del laboratorio y su horario
    LaunchedEffect(Unit) {
        // Obtener nombre del laboratorio
        val labDoc = db.collection("laboratorios").document(laboratorioId.value).get().await()
        laboratorioNombre = labDoc.getString("nombre") ?: "Laboratorio"

        // Cargar horarios
        val snapshot = db.collection("horarios")
            .whereEqualTo("laboratorioId", laboratorioId.value)
            .get()
            .await()

        horario = snapshot.documents.mapNotNull { it.toObject(HorarioClase::class.java) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Horario: $laboratorioNombre", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Add, contentDescription = "Menú")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar clase")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(8.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            HorarioGrid(horario = horario)
        }
    }

    if (showDialog) {
        FormularioAgregarHorario(
            laboratorioId = laboratorioId.value,
            nombreLaboratorio = laboratorioNombre, // 👈 se pasa aquí también
            onAgregar = {
                db.collection("horarios").add(it).addOnSuccessListener {
                    // Recargar horarios
                    db.collection("horarios")
                        .whereEqualTo("laboratorioId", laboratorioId.value)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            horario = snapshot.documents.mapNotNull { doc ->
                                doc.toObject(HorarioClase::class.java)
                            }
                        }
                }
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}


@Composable
fun HorarioGrid(horario: List<HorarioClase>) {
    val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
    val horas = listOf("7:00", "8:00", "9:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00")

    Column {
        Row {
            Celda("Hora", header = true)
            dias.forEach { Celda(it, header = true) }
        }

        horas.forEach { hora ->
            Row {
                Celda(hora, header = true)
                dias.forEach { dia ->
                    val clases = horario.filter {
                        it.dia.equals(dia, ignoreCase = true) && horaEnRango(hora, it.horaInicio, it.horaFin)
                    }
                    val asunto = clases.firstOrNull()?.asunto ?: ""
                    CeldaClase(asunto, asunto)
                }
            }
        }
    }
}

fun horaEnRango(hora: String, inicio: String, fin: String): Boolean {
    val horaInt = hora.replace(":", "").toIntOrNull() ?: return false
    val inicioInt = inicio.replace(":", "").toIntOrNull() ?: return false
    val finInt = fin.replace(":", "").toIntOrNull() ?: return false
    return horaInt in inicioInt until finInt
}

@Composable
fun Celda(text: String, header: Boolean = false) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(60.dp)
            .border(1.dp, Color.Gray)
            .background(if (header) Color(0xFF1976D2) else Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (header) Color.White else Color.Black,
            fontSize = 12.sp
        )
    }
}

@Composable
fun CeldaClase(text: String, key: String?) {
    val colores = listOf(
        Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFF81C784),
        Color(0xFFFFB74D), Color(0xFFBA68C8), Color(0xFFFF8A65)
    )
    val color = remember(key) {
        if (key == null) Color.Transparent
        else colores[key.hashCode().absoluteValue % colores.size]
    }

    Box(
        modifier = Modifier
            .width(80.dp)
            .height(60.dp)
            .border(1.dp, Color.LightGray)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontSize = 10.sp, maxLines = 2)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioAgregarHorario(
    laboratorioId: String,
    nombreLaboratorio: String, // <-- Se recibe el nombre
    onAgregar: (HorarioClase) -> Unit,
    onDismiss: () -> Unit
) {
    val horasDisponibles = listOf(
        "7:00", "8:00", "9:00", "10:00", "11:00",
        "12:00", "13:00", "14:00", "15:00", "16:00", "17:00"
    )
    val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")

    var dia by remember { mutableStateOf("") }
    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }
    var asunto by remember { mutableStateOf("") }

    var diaExpanded by remember { mutableStateOf(false) }
    var inicioExpanded by remember { mutableStateOf(false) }
    var finExpanded by remember { mutableStateOf(false) }
    var errorHora by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Nueva Actividad", style = MaterialTheme.typography.titleLarge)

                Spacer(Modifier.height(12.dp))

                // 🔷 Campo laboratorio solo lectura
                OutlinedTextField(
                    value = nombreLaboratorio,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Laboratorio") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Día
                ExposedDropdownMenuBox(
                    expanded = diaExpanded,
                    onExpandedChange = { diaExpanded = !diaExpanded }
                ) {
                    OutlinedTextField(
                        value = dia,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Día de la semana") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = diaExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = diaExpanded,
                        onDismissRequest = { diaExpanded = false }
                    ) {
                        diasSemana.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = {
                                    dia = opcion
                                    diaExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Hora inicio y fin
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = inicioExpanded,
                        onExpandedChange = { inicioExpanded = !inicioExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = horaInicio,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hora inicio") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = inicioExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = inicioExpanded,
                            onDismissRequest = { inicioExpanded = false }
                        ) {
                            horasDisponibles.forEach { hora ->
                                DropdownMenuItem(
                                    text = { Text(hora) },
                                    onClick = {
                                        horaInicio = hora
                                        inicioExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = finExpanded,
                        onExpandedChange = { finExpanded = !finExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = horaFin,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hora fin") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = finExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = finExpanded,
                            onDismissRequest = { finExpanded = false }
                        ) {
                            horasDisponibles.forEach { hora ->
                                DropdownMenuItem(
                                    text = { Text(hora) },
                                    onClick = {
                                        horaFin = hora
                                        finExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (errorHora) {
                    Text(
                        "La hora de fin debe ser mayor que la de inicio.",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Asunto
                OutlinedTextField(
                    value = asunto,
                    onValueChange = { asunto = it },
                    label = { Text("Nombre de la clase o actividad") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val indexInicio = horasDisponibles.indexOf(horaInicio)
                        val indexFin = horasDisponibles.indexOf(horaFin)

                        if (dia.isNotBlank() && horaInicio.isNotBlank() && horaFin.isNotBlank() && asunto.isNotBlank()) {
                            if (indexFin > indexInicio) {
                                errorHora = false
                                onAgregar(
                                    HorarioClase(
                                        laboratorioId = laboratorioId,
                                        dia = dia.trim(),
                                        horaInicio = horaInicio.trim(),
                                        horaFin = horaFin.trim(),
                                        asunto = asunto.trim()
                                    )
                                )
                            } else {
                                errorHora = true
                            }
                        }
                    }) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

