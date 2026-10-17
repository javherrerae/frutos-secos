package com.oyl.frutossecos.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO de solicitud para crear un nuevo pedido en el backend.
 * El servidor asigna el ID, número de pedido, fecha y estado inicial.
 */
data class CrearPedidoRequest(
    @SerializedName("idUsuario")
    val idUsuario: Int,
    @SerializedName("modalidadEntrega")
    val modalidadEntrega: String,
    @SerializedName("items")
    val items: List<PedidoItemDto>
)
