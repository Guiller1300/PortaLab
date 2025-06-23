package com.example.portalab.uii

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import com.google.firebase.firestore.QuerySnapshot
import com.example.portalab.ui.InventarioScreen


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Badge
import androidx.compose.ui.res.painterResource
import com.example.portalab.R // Esto es necesario para acceder a tus recursos de drawable

// Íconos
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.NavigationDrawerItem
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Schedule

@Composable
fun PantallaConMenu(
    navController: NavController? = null,
    contenido: @Composable (DrawerState, CoroutineScope) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val usuario = FirebaseAuth.getInstance().currentUser
    val nombre = usuario?.displayName ?: "Usuario"
    val correo = usuario?.email ?: "correo@ejemplo.com"
    val imagenUrl = usuario?.photoUrl

    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 8.dp)
            ) {
                // Encabezado
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (imagenUrl != null) {
                        AsyncImage(
                            model = imagenUrl,
                            contentDescription = "Foto perfil",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Icono perfil",
                            modifier = Modifier
                                .size(72.dp)
                                .background(Color.Gray, CircleShape)
                                .padding(16.dp),
                            tint = Color.White
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(nombre, style = MaterialTheme.typography.titleMedium)
                    Text(correo, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                Spacer(Modifier.height(16.dp))

                // Ítems del menú
                val items = listOf(
                    Triple("Dashboard", Icons.Default.Home, "dashboard"),
                    Triple("Laboratorios", Icons.Default.Lan, "laboratorios"),
                    Triple("Inventario", Icons.Default.Inventory, "inventario"),
                    Triple("Software", Icons.Default.Star, "software"),
                    Triple("Instalaciones", Icons.Default.Star, "instalacionSoftware"),
                    Triple("Horarios", Icons.Default.Schedule, "horarios"),
                    Triple("Incidencias", Icons.Default.ReportProblem, "incidencias") // ← Agregado aquí
                )

                items.forEach { (label, icon, route) ->
                    NavigationDrawerItem(
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, contentDescription = null)
                                Spacer(Modifier.width(12.dp))
                                Text(label)
                            }
                        },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navController?.navigate(route)
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                NavigationDrawerItem(
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Text("Cerrar sesión")
                        }
                    },
                    selected = false,
                    onClick = {
                        mostrarDialogoCerrarSesion = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                if (mostrarDialogoCerrarSesion) {
                    AlertDialog(
                        onDismissRequest = { mostrarDialogoCerrarSesion = false },
                        title = { Text("Confirmar cierre de sesión") },
                        text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
                        confirmButton = {
                            TextButton(onClick = {
                                FirebaseAuth.getInstance().signOut()
                                navController?.navigate("login") {
                                    popUpTo("inventario") { inclusive = true }
                                }
                                mostrarDialogoCerrarSesion = false
                            }) {
                                Text("Sí")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarDialogoCerrarSesion = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            }
        }
    ) {
        contenido(drawerState, coroutineScope)
    }
}

