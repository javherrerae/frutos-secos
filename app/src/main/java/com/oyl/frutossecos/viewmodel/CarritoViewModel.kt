package com.oyl.frutossecos.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oyl.frutossecos.AppDatabase
import com.oyl.frutossecos.data.CarritoRepository
import com.oyl.frutossecos.dto.PedidoItemDto
import com.oyl.frutossecos.model.ItemCarrito
import com.oyl.frutossecos.model.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona los productos agregados al carrito de compras.
 * Guarda los artículos en Room (SQLite) a través de CarritoRepository,
 * permitiendo persistencia y separación por cada cliente.
 */
class CarritoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.crear(application)
    private val repository = CarritoRepository(db.carritoItemDao())
    private var idUsuarioActual: Int = 1

    private val _items = mutableStateListOf<ItemCarrito>()
    val items: List<ItemCarrito> get() = _items.toList()

    val cantidadTotal: Int get() = _items.sumOf { it.cantidad }
    val subtotal: Int get() = _items.sumOf { it.subtotal }

    /**
     * Establece el usuario activo y recarga su carrito persistente desde SQLite.
     */
    fun configurarUsuario(idUsuario: Int) {
        idUsuarioActual = idUsuario
        cargarCarritoDesdeBase()
    }

    private fun cargarCarritoDesdeBase() {
        viewModelScope.launch(Dispatchers.IO) {
            val itemsLocal = repository.obtenerCarrito(idUsuarioActual)
            val productoDao = db.productoDao()
            val lista = itemsLocal.mapNotNull { itemEntity ->
                val pEntity = productoDao.obtenerPorId(itemEntity.idProducto)
                if (pEntity != null) {
                    ItemCarrito(
                        producto = pEntity.toUiModel(),
                        cantidad = itemEntity.cantidad
                    )
                } else null
            }
            launch(Dispatchers.Main) {
                if (lista.isNotEmpty()) {
                    _items.clear()
                    _items.addAll(lista)
                }
            }
        }
    }

    fun agregarProducto(producto: Producto, cantidad: Int) {
        if (cantidad <= 0 || producto.stock <= 0) return

        val indice = _items.indexOfFirst { it.producto.id == producto.id }

        if (indice >= 0) {
            val itemActual = _items[indice]
            val nuevaCantidad = itemActual.cantidad + cantidad
            if (nuevaCantidad <= producto.stock) {
                _items[indice] = itemActual.copy(cantidad = nuevaCantidad)
                persistirItem(producto.id, nuevaCantidad, producto.precio.toDouble())
            }
        } else if (cantidad <= producto.stock) {
            _items.add(ItemCarrito(producto = producto, cantidad = cantidad))
            persistirItem(producto.id, cantidad, producto.precio.toDouble())
        }
    }

    fun aumentarCantidad(productoId: Int) {
        val indice = _items.indexOfFirst { it.producto.id == productoId }
        if (indice >= 0) {
            val item = _items[indice]
            if (item.cantidad < item.producto.stock) {
                val nuevaCantidad = item.cantidad + 1
                _items[indice] = item.copy(cantidad = nuevaCantidad)
                persistirItem(productoId, nuevaCantidad, item.producto.precio.toDouble())
            }
        }
    }

    fun disminuirCantidad(productoId: Int) {
        val indice = _items.indexOfFirst { it.producto.id == productoId }
        if (indice >= 0) {
            val item = _items[indice]
            if (item.cantidad > 1) {
                val nuevaCantidad = item.cantidad - 1
                _items[indice] = item.copy(cantidad = nuevaCantidad)
                persistirItem(productoId, nuevaCantidad, item.producto.precio.toDouble())
            } else {
                _items.removeAt(indice)
                eliminarDeBase(productoId)
            }
        }
    }

    fun eliminarProducto(productoId: Int) {
        _items.removeAll { it.producto.id == productoId }
        eliminarDeBase(productoId)
    }

    fun vaciarCarrito() {
        _items.clear()
        viewModelScope.launch(Dispatchers.IO) {
            repository.vaciarCarrito(idUsuarioActual)
        }
    }

    private fun persistirItem(idProducto: Int, cantidad: Int, precioUnitario: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.guardarOActualizar(idUsuarioActual, idProducto, cantidad, precioUnitario)
        }
    }

    private fun eliminarDeBase(idProducto: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.eliminarProducto(idUsuarioActual, idProducto)
        }
    }

    /**
     * Convierte los ítems del carrito al formato DTO para enviar a la API REST.
     */
    fun toPedidoItemDtos(): List<PedidoItemDto> {
        return _items.map {
            PedidoItemDto(
                idProducto = it.producto.id,
                nombreProducto = it.producto.nombre,
                cantidad = it.cantidad,
                precioUnitario = it.producto.precio.toDouble()
            )
        }
    }
}
