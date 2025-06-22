package com.example.portalab.model

data class InstalacionSoftware(
    val id: String? = null,                 // ID único de la instalación
    val softwareId: String = "",            // ID del software (referencia lógica)
    val equipoId: String = "",              // ID del equipo/dispositivo
    val fechaInstalacion: String = "",      // Ejemplo: "2025-06-22"
    val licencia: String = ""               // Ejemplo: "Activada", "Libre", "Trial"
)
