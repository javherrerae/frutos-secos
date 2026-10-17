package com.oyl.frutossecos.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO que el administrador envía para actualizar el stock de un producto.
 * También permite modificar el precio si es necesario.
 */
data class ActualizarStockRequest(
    @SerializedName("stock")
    val stock: Int,
    @SerializedName("precio")
    val precio: Double? = null
)
