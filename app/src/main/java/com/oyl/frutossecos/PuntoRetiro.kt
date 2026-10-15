package com.oyl.frutossecos

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa la tabla SQLite `puntos_retiro`.
 * Almacena las ubicaciones de la tienda y puntos de retiro habilitados.
 * Se utiliza como fuente de datos local cuando no se dispone de la API REST,
 * permitiendo que el mapa funcione sin conexión con datos de demostración.
 */
@Entity(tableName = "puntos_retiro")
data class PuntoRetiro(
    @PrimaryKey
    val id: Int,
    val nombre: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val horario: String
)
