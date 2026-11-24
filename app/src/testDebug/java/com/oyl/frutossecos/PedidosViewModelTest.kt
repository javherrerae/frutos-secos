package com.oyl.frutossecos

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.oyl.frutossecos.model.ItemCarrito
import com.oyl.frutossecos.model.Producto
import com.oyl.frutossecos.viewmodel.PedidosViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PedidosViewModelTest {
    @Test fun rechazaPedidoSinSesionYLimpiaElEstado() {
        val vm = PedidosViewModel(ApplicationProvider.getApplicationContext<Application>())
        val producto = Producto(1, "Nuez", "Frutos", "", "250 g", 1000, 2, "")
        var completado = false
        vm.confirmar(listOf(ItemCarrito(producto, 1))) { pedido ->
            assertNull(pedido)
            completado = true
        }
        assertTrue(completado)
        assertEquals("Inicia sesión y revisa el carrito.", vm.mensajeUltimoPedido)
        assertNull(vm.buscar("inexistente"))
        vm.limpiarSesion()
        assertNull(vm.mensajeUltimoPedido)
        assertTrue(vm.pedidos.isEmpty())
    }
}
