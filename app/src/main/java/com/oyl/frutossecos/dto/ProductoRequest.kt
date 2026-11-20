package com.oyl.frutossecos.dto

data class ProductoRequest(
    val nombre: String,
    val categoria: String,
    val descripcion: String,
    val precio: Double,
    val presentacion: String,
    val stock: Int,
    val rutaImagen: String
)

data class EstadoProductoRequest(val activo: Boolean)
data class MovimientoStockRequest(val entrada: Boolean, val cantidad: Int)
data class PerfilRequest(val nombre: String, val correo: String, val telefono: String)
