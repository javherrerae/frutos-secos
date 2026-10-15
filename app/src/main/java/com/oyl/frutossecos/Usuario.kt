package com.oyl.frutossecos

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa la tabla SQLite `usuarios`.
 * Cada objeto equivale a una fila; el índice UNIQUE evita alias repetidos.
 * El rol puede ser "cliente" o "administrador".
 * El campo activo usa 1 (activo) y 0 (inactivo) porque SQLite no tiene BOOLEAN.
 */
@Entity(tableName = "usuarios", indices = [Index(value = ["alias"], unique = true)])
data class Usuario(
    @PrimaryKey
    val id: Int,
    val nombre: String,
    val alias: String,
    val contrasena: String,
    val correo: String,
    val telefono: String,
    val direccion: String,
    val rol: String,
    val activo: Int
)
