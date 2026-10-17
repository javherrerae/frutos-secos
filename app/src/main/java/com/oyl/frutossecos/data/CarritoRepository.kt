package com.oyl.frutossecos.data

import com.oyl.frutossecos.CarritoItem
import com.oyl.frutossecos.CarritoItemDao

/**
 * Repositorio que gestiona la persistencia del carrito de compras en Room (SQLite).
 * Permite separar los carritos de distintos clientes en el mismo dispositivo.
 */
class CarritoRepository(private val carritoItemDao: CarritoItemDao) {

    /**
     * Obtiene los ítems guardados en SQLite para el cliente especificado.
     */
    fun obtenerCarrito(idUsuario: Int): List<CarritoItem> {
        return carritoItemDao.obtenerPorUsuario(idUsuario)
    }

    /**
     * Guarda o actualiza un ítem en la base local.
     */
    fun guardarOActualizar(idUsuario: Int, idProducto: Int, cantidad: Int, precioUnitario: Double) {
        val existente = carritoItemDao.obtenerItem(idUsuario, idProducto)
        val idItem = existente?.id ?: ((idUsuario * 1000) + idProducto)
        val item = CarritoItem(
            id = idItem,
            idUsuario = idUsuario,
            idProducto = idProducto,
            cantidad = cantidad,
            precioUnitario = precioUnitario
        )
        carritoItemDao.agregarOActualizar(item)
    }

    /**
     * Elimina un producto del carrito local del usuario.
     */
    fun eliminarProducto(idUsuario: Int, idProducto: Int) {
        carritoItemDao.eliminarPorProducto(idUsuario, idProducto)
    }

    /**
     * Vacía el carrito del cliente tras confirmar la orden o cerrar sesión.
     */
    fun vaciarCarrito(idUsuario: Int) {
        carritoItemDao.vaciarCarrito(idUsuario)
    }
}
