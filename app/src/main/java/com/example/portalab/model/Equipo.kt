package com.example.portalab.model

data class Equipo(
    val id: String? = null,  // <- necesario para eliminar
    val inventario: String = "",      // Número de inventario
    val nombre: String = "",          // Nombre del equipo (e.g., "LABCBT-A12")
    val descripcion: String = "",     // Descripción (e.g., "Laptop")
    val estado: String = "",          // Estado (e.g., "Disponible", "Excelente")
    val pabellon: String = "Pabellón 16", // Pabellón (por defecto "Pabellón 16")
    val laboratorio: String = ""      // Laboratorio dentro del pabellón (e.g., "Laboratorio 1")
)