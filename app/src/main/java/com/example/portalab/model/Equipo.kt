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
/*data class Equipo(
    val id: String? = null,              // ID único (útil para eliminar/actualizar)
    val inventario: String = "",         // Código o número de inventario
    val nombre: String = "",             // Ejemplo: "LABCBT-C01"
    val descripcion: String = "",        // Ejemplo: "DESKTOP"
    val marca: String = "",              // Ejemplo: "LENOVO"
    val estado: String = "",             // Ejemplo: "BUENO", "EXCELENTE"
    val procesador: String = "",         // Ejemplo: "i5-4TH"
    val ram: Int = 0,                    // Ejemplo: 8
    val laboratorioId: String = "",      // ID o nombre del laboratorio al que pertenece
)*/