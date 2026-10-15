package com.oyl.frutossecos

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa la tabla SQLite `pedidos`.
 * Almacena una copia local de los pedidos para consulta sin conexión.
 * La fuente oficial de pedidos e inventario reside en el backend Spring Boot.
 * Los estados válidos son: "Pendiente de Validación", "Confirmado", "Preparado" y "Entregado".
 */
@Entity(tableName = "pedidos")
data class Pedido(
    @PrimaryKey
    val id: Int,
    val idUsuario: Int,
    val fecha: String,
    val total: Double,
    val modalidadEntrega: String,
    val estado: String,
    val numeroPedido: String
)
