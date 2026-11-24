package com.oyl.frutossecos

import com.oyl.frutossecos.data.CarritoRepository
import org.junit.Assert.*
import org.junit.Test

class CarritoRepositoryTest {
    private class DaoFalso : CarritoItemDao {
        val items = mutableListOf<CarritoItem>()
        override fun agregarOActualizar(item: CarritoItem) { items.removeAll { it.id == item.id }; items.add(item) }
        override fun obtenerPorUsuario(idUsuario: Int) = items.filter { it.idUsuario == idUsuario }
        override fun eliminarItem(id: Int) { items.removeAll { it.id == id } }
        override fun eliminarPorProducto(idUsuario: Int, idProducto: Int) { items.removeAll { it.idUsuario == idUsuario && it.idProducto == idProducto } }
        override fun obtenerItem(idUsuario: Int, idProducto: Int) = items.firstOrNull { it.idUsuario == idUsuario && it.idProducto == idProducto }
        override fun vaciarCarrito(idUsuario: Int) { items.removeAll { it.idUsuario == idUsuario } }
    }

    private val dao = DaoFalso()
    private val repo = CarritoRepository(dao)

    @Test fun guardaYActualizaConElMismoIdentificador() {
        repo.guardarOActualizar(2, 5, 1, 1200.0)
        repo.guardarOActualizar(2, 5, 3, 1500.0)
        assertEquals(listOf(CarritoItem(2005, 2, 5, 3, 1500.0)), repo.obtenerCarrito(2))
    }

    @Test fun conservaElIdentificadorExistente() {
        dao.items.add(CarritoItem(99, 2, 5, 1, 100.0))
        repo.guardarOActualizar(2, 5, 2, 200.0)
        assertEquals(99, repo.obtenerCarrito(2).single().id)
    }

    @Test fun eliminarYVaciarNoAfectaAOtroUsuario() {
        repo.guardarOActualizar(1, 1, 1, 100.0)
        repo.guardarOActualizar(1, 2, 1, 200.0)
        repo.guardarOActualizar(2, 1, 1, 100.0)
        repo.eliminarProducto(1, 1)
        assertEquals(2, repo.obtenerCarrito(1).single().idProducto)
        repo.vaciarCarrito(1)
        assertTrue(repo.obtenerCarrito(1).isEmpty())
        assertEquals(1, repo.obtenerCarrito(2).size)
    }
}
