package com.oyl.frutossecos

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa la tabla SQLite `productos`.
 * Actúa como caché local del catálogo obtenido desde el backend.
 * El campo activo usa 1 (disponible) y 0 (no disponible) porque SQLite no tiene BOOLEAN.
 * El precio y el stock son de solo consulta; el inventario oficial reside en el servidor.
 */
@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey
    val id: Int,
    val nombre: String,
    val categoria: String,
    val descripcion: String,
    val precio: Double,
    val presentacion: String,
    val stock: Int,
    val rutaImagen: String,
    val activo: Int
)
