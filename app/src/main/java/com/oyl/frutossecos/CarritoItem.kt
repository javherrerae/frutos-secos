package com.oyl.frutossecos

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa la tabla SQLite `carrito_items`.
 * Cada fila corresponde a un producto agregado al carrito por un cliente específico.
 * El campo idUsuario permite mantener carritos separados entre distintos perfiles
 * registrados en el mismo dispositivo.
 */
@Entity(tableName = "carrito_items")
data class CarritoItem(
    @PrimaryKey
    val id: Int,
    val idUsuario: Int,
    val idProducto: Int,
    val cantidad: Int,
    val precioUnitario: Double
)
