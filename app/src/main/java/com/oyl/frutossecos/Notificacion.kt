package com.oyl.frutossecos

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa la tabla SQLite `notificaciones`.
 * Almacena la lista local de avisos recibidos por cada usuario.
 * El campo leida usa 1 (leída) y 0 (no leída) porque SQLite no tiene BOOLEAN.
 * El campo pantallaDest indica la pantalla destino al seleccionar el aviso.
 */
@Entity(tableName = "notificaciones")
data class Notificacion(
    @PrimaryKey
    val id: Int,
    val idUsuario: Int,
    val titulo: String,
    val mensaje: String,
    val fecha: String,
    val leida: Int,
    val pantallaDest: String
)
