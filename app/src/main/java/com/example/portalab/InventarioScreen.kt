// InventarioScreen.kt

package com.example.portalab.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*  // Cambiado a Material3
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.portalab.model.Equipo
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.example.portalab.ui.theme.PortaLabTheme
import com.example.portalab.ui.theme.Purple40


@Composable
fun InventarioScreen() {
    val equipos = remember { mutableStateListOf<Equipo>() }
    val loading = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }

    // Obtener los equipos desde Firestore
    LaunchedEffect(true) {
        obtenerEquipos(
            onSuccess = { fetchedEquipos ->
                equipos.clear()
                equipos.addAll(fetchedEquipos) // Actualizamos la lista de equipos
                loading.value = false
            },
            onFailure = { exception ->
                error.value = "Error al obtener los equipos: ${exception.localizedMessage}"
                loading.value = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario de Equipos") }
            )
        },
        content = {
            if (loading.value) {
                CircularProgressIndicator(modifier = Modifier.fillMaxSize()) // Muestra un cargador
            } else if (error.value != null) {
                Text(text = error.value!!, color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(equipos) { equipo ->
                        EquipoCard(equipo)
                    }
                }
            }
        }
    )

}

@Composable
fun EquipoCard(equipo: Equipo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Inventario: ${equipo.inventario}", style = MaterialTheme.typography.h6)
            Text("Nombre: ${equipo.nombre}")
            Text("Descripción: ${equipo.descripcion}")
            Text("Estado: ${equipo.estado}")
            Text("Pabellón: ${equipo.pabellon}")
            Text("Laboratorio: ${equipo.laboratorio}")
        }
    }
}

fun obtenerEquipos(onSuccess: (List<Equipo>) -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("equipos")
        .get()
        .addOnSuccessListener { result ->
            val equipos = mutableListOf<Equipo>()
            for (document in result) {
                val equipo = document.toObject(Equipo::class.java)  // Convertir el documento en un objeto Equipo
                equipos.add(equipo)
            }
            onSuccess(equipos)  // Llama al callback con la lista de equipos obtenidos
        }
        .addOnFailureListener { exception ->
            onFailure(exception)  // Llama al callback si hay un error
        }
}

