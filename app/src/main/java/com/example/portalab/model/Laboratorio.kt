package com.example.portalab.model

data class Laboratorio(
    val id: String = "",           // ID único (puede ser generado por Firestore o nombre tipo "Laboratorio A")
    val nombre: String = "",       // Nombre visible: "Laboratorio A"
    val pabellon: String = "",     // Ejemplo: "Pabellón 16"
    val descripcion: String = ""   // (opcional) Información adicional del laboratorio
)