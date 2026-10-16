package com.oyl.frutossecos.model

// Las direcciones y coordenadas representan puntos de prueba, no locales reales.
data class TiendaDemo(
    val id: String,
    val nombre: String,
    val comuna: String,
    val direccion: String,
    val horario: String,
    val latitud: Double,
    val longitud: Double
)

object TiendasDemo {
    val todas = listOf(
        TiendaDemo("providencia", "OYL Providencia", "Providencia",
            "Av. del Almendro 120", "Lunes a sábado · 10:00–19:00", -33.426, -70.617),
        TiendaDemo("nunoa", "OYL Ñuñoa", "Ñuñoa",
            "Pasaje Semilla 48", "Lunes a viernes · 10:00–18:00", -33.456, -70.597),
        TiendaDemo("santiago", "Retiro Santiago Centro", "Santiago",
            "Paseo Fruto 310", "Lunes a viernes · 09:00–18:00", -33.443, -70.650)
    )

    fun buscar(id: String): TiendaDemo = todas.firstOrNull { it.id == id } ?: todas.first()
}
