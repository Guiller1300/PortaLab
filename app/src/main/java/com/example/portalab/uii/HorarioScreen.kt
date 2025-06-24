package com.example.portalab.uii

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.portalab.model.HorarioClase
import kotlin.math.absoluteValue



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleLaboratorioConHorario(
    laboratorioId: String,
    laboratorioNombre: String,
    horario: List<HorarioClase>,
    onAgregarHorario: (HorarioClase) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        SugerirLandscape()
        Text(
            text = "Horario de $laboratorioNombre",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, Color.Gray)
                .horizontalScroll(rememberScrollState())
                .verticalScroll(rememberScrollState())
        ) {
            HorarioDinamico(horario = horario)
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar clase")
            Spacer(Modifier.width(4.dp))
            Text("Agregar")
        }
    }

    if (showDialog) {
        FormularioAgregarHorario(
            laboratorioId = laboratorioId,
            onAgregar = {
                onAgregarHorario(it)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioAgregarHorario(
    laboratorioId: String,
    onAgregar: (HorarioClase) -> Unit,
    onDismiss: () -> Unit
) {
    var dia by remember { mutableStateOf("") }
    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }
    var asunto by remember { mutableStateOf("") }

    val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Agregar Actividad", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))

                // Día (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = dia,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Día de la semana") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        diasSemana.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = {
                                    dia = opcion
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Hora inicio y fin en una fila
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = horaInicio,
                        onValueChange = { horaInicio = it },
                        label = { Text("Inicio") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = horaFin,
                        onValueChange = { horaFin = it },
                        label = { Text("Fin") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = asunto,
                    onValueChange = { asunto = it },
                    label = { Text("Actividad") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        onAgregar(
                            HorarioClase(
                                laboratorioId = laboratorioId,
                                dia = dia.trim(),
                                horaInicio = horaInicio.trim(),
                                horaFin = horaFin.trim(),
                                asunto = asunto.trim()
                            )
                        )
                    }) {
                        Text("Agregar")
                    }
                }
            }
        }
    }
}
@Composable
fun HorarioDinamico(horario: List<HorarioClase>) {
    val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")
    val diasFinde = listOf("Sábado", "Domingo")

    // Extraer rangos únicos para semana
    val rangosSemana = horario
        .filter { it.dia in diasSemana }
        .map { "${it.horaInicio} - ${it.horaFin}" }
        .distinct()
        .sortedWith(compareBy { HoraRango(it) })

    // Extraer rangos únicos para finde
    val rangosFinde = horario
        .filter { it.dia in diasFinde }
        .map { "${it.horaInicio} - ${it.horaFin}" }
        .distinct()
        .sortedWith(compareBy { HoraRango(it) })

    Column {
        // Tabla semana
        Text("Semana", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(4.dp))
        Row {
            Celda("Hora", header = true)
            diasSemana.forEach { Celda(it, header = true) }
        }

        rangosSemana.forEach { rango ->
            Row {
                Celda(rango, header = true)
                diasSemana.forEach { dia ->
                    val clase = horario.find {
                        it.dia.equals(dia, ignoreCase = true) &&
                                "${it.horaInicio} - ${it.horaFin}" == rango
                    }
                    CeldaClase(clase?.asunto ?: "", clase?.asunto)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Tabla finde
        Text("Fin de semana", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(4.dp))
        Row {
            Celda("Hora", header = true)
            diasFinde.forEach { Celda(it, header = true) }
        }

        rangosFinde.forEach { rango ->
            Row {
                Celda(rango, header = true)
                diasFinde.forEach { dia ->
                    val clase = horario.find {
                        it.dia.equals(dia, ignoreCase = true) &&
                                "${it.horaInicio} - ${it.horaFin}" == rango
                    }
                    CeldaClase(clase?.asunto ?: "", clase?.asunto)
                }
            }
        }
    }
}
fun HoraRango(rango: String): Int {
    // Espera formato "8:00 - 10:00"
    val inicio = rango.split(" - ").firstOrNull() ?: "0:00"
    val partes = inicio.split(":").map { it.toIntOrNull() ?: 0 }
    return (partes.getOrElse(0) { 0 }) * 60 + (partes.getOrElse(1) { 0 })
}
@Composable
fun Celda(text: String, header: Boolean = false) {
    Box(
        modifier = Modifier
            .width(100.dp) // Aumenté el ancho
            .height(60.dp)
            .border(1.dp, Color.Gray)
            .background(if (header) Color(0xFF1976D2) else Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (header) Color.White else Color.Black,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
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

    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(100.dp)
            .height(60.dp)
            .border(1.dp, Color.LightGray)
            .background(color)
            .clickable { if (text.isNotBlank()) expanded = true }, // Abre el menú solo si hay texto
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Popup o menú desplegable con el contenido completo
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(8.dp),
                color = Color.Black,
                fontSize = 12.sp
            )
        }
    }
}
@Composable
fun SugerirLandscape() {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current

    LaunchedEffect(configuration.orientation) {
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(
                context,
                "Para una mejor visualización, gire su dispositivo",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
