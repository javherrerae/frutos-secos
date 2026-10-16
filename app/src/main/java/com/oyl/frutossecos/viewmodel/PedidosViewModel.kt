package com.oyl.frutossecos.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.oyl.frutossecos.model.ItemCarrito
import com.oyl.frutossecos.model.LineaPedidoDemo
import com.oyl.frutossecos.model.PedidoDemo
import java.util.UUID
import com.oyl.frutossecos.model.TiendaDemo
import com.oyl.frutossecos.model.TiendasDemo

class PedidosViewModel : ViewModel() {
    private val _pedidos = mutableStateListOf<PedidoDemo>()
    val pedidos: List<PedidoDemo> get() = _pedidos.toList()

    fun confirmar(items: List<ItemCarrito>, tienda: TiendaDemo = TiendasDemo.todas.first()): PedidoDemo? {
        if (items.isEmpty() || items.any {
                it.cantidad <= 0 || it.cantidad > it.producto.stock || it.producto.precio < 0
            }) return null
        val pedido = PedidoDemo(
            codigo = "OYL-" + UUID.randomUUID().toString().take(8).uppercase(),
            fechaMillis = System.currentTimeMillis(),
            puntoRetiro = tienda.comuna,
            direccionRetiro = "${tienda.direccion} · ${tienda.comuna}",
            productos = items.map {
                LineaPedidoDemo(it.producto.nombre, it.producto.peso,
                    it.producto.precio, it.cantidad)
            }
        )
        _pedidos.add(0, pedido)
        return pedido
    }

    fun buscar(codigo: String?): PedidoDemo? = _pedidos.find { it.codigo == codigo }
    fun limpiarSesion() { _pedidos.clear() }
}
