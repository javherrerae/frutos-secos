package com.oyl.frutossecos

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa la tabla SQLite `pedido_items`.
 * Cada fila corresponde a un producto incluido dentro de un pedido.
 * El campo nombreProducto se almacena para preservar el nombre en el historial
 * aunque el producto sea modificado o desactivado en el catálogo posteriormente.
 */
@Entity(tableName = "pedido_items")
data class PedidoItem(
    @PrimaryKey
    val id: Int,
    val idPedido: Int,
    val idProducto: Int,
    val nombreProducto: String,
    val cantidad: Int,
    val precioUnitario: Double
)
