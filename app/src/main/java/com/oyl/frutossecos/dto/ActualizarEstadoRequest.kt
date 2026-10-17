package com.oyl.frutossecos.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO que el administrador envía para cambiar el estado de un pedido.
 * Estados válidos: "Pendiente de Validación", "Confirmado", "Preparado", "Entregado".
 */
data class ActualizarEstadoRequest(
    @SerializedName("estado")
    val estado: String
)
