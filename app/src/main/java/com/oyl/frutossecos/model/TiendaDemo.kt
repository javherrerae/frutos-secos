
package com.oyl.frutossecos.model

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

    // Lugares reales usados solo como referencias de demostración.
    // No representan sucursales ni puntos de retiro reales de OYL.
    val todas = listOf(

        TiendaDemo(
            id = "providencia",
            nombre = "Plaza de la Aviación",
            comuna = "Providencia",
            direccion = "Plaza de la Aviación, Av. Providencia",
            horario = "Referencia geográfica - Sin retiro real",
            latitud = -33.43127,
            longitud = -70.62473
        ),

        TiendaDemo(
            id = "nunoa",
            nombre = "Plaza Ñuñoa",
            comuna = "Ñuñoa",
            direccion = "Plaza Ñuñoa, Av. Irarrázaval",
            horario = "Referencia geográfica - Sin retiro real",
            latitud = -33.45474,
            longitud = -70.59365
        ),

        TiendaDemo(
            id = "santiago",
            nombre = "Plaza de Armas",
            comuna = "Santiago",
            direccion = "Plaza de Armas, Santiago Centro",
            horario = "Referencia geográfica - Sin retiro real",
            latitud = -33.43797,
            longitud = -70.65040
        )
    )

    fun buscar(id: String): TiendaDemo =
        todas.firstOrNull { it.id == id } ?: todas.first()
}
