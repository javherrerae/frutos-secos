
package com.oyl.frutossecos.model

data class Producto(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val descripcion: String,
    val peso: String,
    val precio: Int,
    val stock: Int,
    val emoji: String
)
