package com.oyl.frutossecos.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO que representa un pedido completo recibido desde el backend.
 * Incluye la lista de ítems detallados dentro del pedido.
 */
data class PedidoDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("idUsuario")
    val idUsuario: Int,
    @SerializedName("fecha")
    val fecha: String,
    @SerializedName("total")
    val total: Double,
    @SerializedName("modalidadEntrega")
    val modalidadEntrega: String,
    @SerializedName("estado")
    val estado: String,
    @SerializedName("numeroPedido")
    val numeroPedido: String,
    @SerializedName("items")
    val items: List<PedidoItemDto>? = null
)
