package com.oyl.frutossecos

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa la tabla SQLite `sesiones`.
 * Cada objeto equivale a una fila y conserva la sesión activa del usuario
 * en el dispositivo para no requerir inicio de sesión en cada apertura.
 */
@Entity(tableName = "sesiones")
data class Sesion(
    @PrimaryKey
    val id: Int,
    val idUsuario: Int,
    val token: String,
    val fechaInicio: String
)
