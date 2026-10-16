package com.oyl.frutossecos.model

// Copia inmutable del carrito al confirmar; no es la entidad Room Pedido.
data class LineaPedidoDemo(
    val nombre: String,
    val formato: String,
    val precioUnitario: Int,
    val cantidad: Int
) {
    val subtotal: Int get() = precioUnitario * cantidad
}

data class PedidoDemo(
    val codigo: String,
    val fechaMillis: Long,
    val productos: List<LineaPedidoDemo>,
    val estado: String = "Confirmado",
    val puntoRetiro: String = "Providencia",
    val direccionRetiro: String = "Av. del Almendro 120 · Providencia"
) {
    val total: Int get() = productos.sumOf { it.subtotal }
    val cantidadBolsas: Int get() = productos.sumOf { it.cantidad }
}
