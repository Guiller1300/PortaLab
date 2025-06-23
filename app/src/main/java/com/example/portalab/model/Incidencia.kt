package com.example.portalab.model

data class Incidencia(
    val id: String = "",              // ID único generado por Firestore
    val equipoId: String = "",        // ID del equipo asociado (puede ser vacío si es por laboratorio)
    val laboratorioId: String = "",   // ID del laboratorio afectado
    val descripcion: String = "",     // Descripción de la incidencia
    val fechaReporte: String = "",    // Fecha en formato "dd/MM/yyyy HH:mm"
    val estado: String = "", // Estado inicial: "Pendiente", "En proceso", "Resuelto"
    val reportadoPor: String = ""     // Nombre o ID del usuario que reporta
)
