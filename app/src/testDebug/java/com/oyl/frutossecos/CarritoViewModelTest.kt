package com.oyl.frutossecos

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.oyl.frutossecos.model.Producto
import com.oyl.frutossecos.data.CarritoRepository
import com.oyl.frutossecos.viewmodel.CarritoViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CarritoViewModelTest {
    private val producto = Producto(27, "Nueces", "Frutos", "Natural", "500 g", 3000, 3, "🌰")
    private fun crearViewModel(): CarritoViewModel {
        val dao = object : CarritoItemDao {
            override fun agregarOActualizar(item: CarritoItem) = Unit
            override fun obtenerPorUsuario(idUsuario: Int) = emptyList<CarritoItem>()
            override fun eliminarItem(id: Int) = Unit
            override fun eliminarPorProducto(idUsuario: Int, idProducto: Int) = Unit
            override fun obtenerItem(idUsuario: Int, idProducto: Int): CarritoItem? = null
            override fun vaciarCarrito(idUsuario: Int) = Unit
        }
        return CarritoViewModel(ApplicationProvider.getApplicationContext<Application>(), CarritoRepository(dao))
    }

    @Test fun limitaStockCalculaTotalesYEliminaAlReducirAUno() {
        val vm = crearViewModel()
        vm.agregarProducto(producto, 0)
        vm.agregarProducto(producto, 4)
        assertTrue(vm.items.isEmpty())
        vm.agregarProducto(producto, 2)
        vm.aumentarCantidad(producto.id)
        vm.aumentarCantidad(producto.id)
        assertEquals(3, vm.cantidadTotal)
        assertEquals(9000, vm.subtotal)
        assertEquals(3, vm.toPedidoItemDtos().single().cantidad)
        vm.disminuirCantidad(producto.id)
        vm.disminuirCantidad(producto.id)
        assertEquals(1, vm.cantidadTotal)
        vm.disminuirCantidad(producto.id)
        assertTrue(vm.items.isEmpty())
    }

    @Test fun agregarUnProductoExistenteYVaciar() {
        val vm = crearViewModel()
        vm.agregarProducto(producto, 1)
        vm.agregarProducto(producto, 2)
        vm.agregarProducto(producto, 1)
        assertEquals(3, vm.items.single().cantidad)
        vm.eliminarProducto(producto.id)
        assertTrue(vm.items.isEmpty())
        vm.agregarProducto(producto.copy(stock = 0), 1)
        assertTrue(vm.items.isEmpty())
        vm.vaciarCarrito()
        assertEquals(0, vm.subtotal)
    }
}
