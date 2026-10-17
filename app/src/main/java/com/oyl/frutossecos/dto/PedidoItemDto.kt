package com.oyl.frutossecos.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO que representa un artículo dentro de un pedido.
 */
data class PedidoItemDto(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("idPedido")
    val idPedido: Int = 0,
    @SerializedName("idProducto")
    val idProducto: Int,
    @SerializedName("nombreProducto")
    val nombreProducto: String,
    @SerializedName("cantidad")
    val cantidad: Int,
    @SerializedName("precioUnitario")
    val precioUnitario: Double
)
