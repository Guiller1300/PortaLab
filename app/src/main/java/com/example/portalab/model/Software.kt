package com.example.portalab.model

data class Software(
    val id: String? = null,            // ID único (generado por Firestore)
    val nombre: String = "",           // Ejemplo: "Microsoft Office"
    val version: String = "",          // Ejemplo: "2021"
    val fabricante: String = ""        // Ejemplo: "Microsoft"
)
