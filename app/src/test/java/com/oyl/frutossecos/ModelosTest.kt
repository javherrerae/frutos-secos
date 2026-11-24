package com.oyl.frutossecos

import com.oyl.frutossecos.model.*
import org.junit.Assert.*
import org.junit.Test

class ModelosTest {
    @Test fun subtotalDelCarritoDependeDePrecioYCantidad() {
        val producto = Producto(1, "Almendras", "Frutos", "", "500 g", 2500, 10, "")
        assertEquals(7500, ItemCarrito(producto, 3).subtotal)
        assertEquals(0, ItemCarrito(producto, 0).subtotal)
    }

    @Test fun pedidoSumaLineasYAdmiteListaVacia() {
        val pedido = PedidoDemo("A", 0, listOf(LineaPedidoDemo("Nuez", "500 g", 2000, 2), LineaPedidoDemo("Mani", "1 kg", 3000, 1)))
        assertEquals(7000, pedido.total)
        assertEquals(3, pedido.cantidadBolsas)
        assertEquals(0, pedido.copy(productos = emptyList()).total)
        assertEquals(0, pedido.copy(productos = emptyList()).cantidadBolsas)
    }

    @Test fun pedidoRespetaTotalesDirectosInclusoSiSonCero() {
        val pedido = PedidoDemo("B", 0, listOf(LineaPedidoDemo("Nuez", "500 g", 2000, 2)), totalDirecto = 0, cantidadBolsasDirecto = 0)
        assertEquals(0, pedido.total)
        assertEquals(0, pedido.cantidadBolsas)
    }

    @Test fun buscaPuntoConocidoYUsaElPrimeroSiNoExiste() {
        assertEquals("nunoa", TiendasDemo.buscar("nunoa").id)
        assertEquals(TiendasDemo.todas.first(), TiendasDemo.buscar("desconocido"))
        assertEquals(TiendasDemo.todas.size, TiendasDemo.todas.map { it.id }.distinct().size)
    }
}
