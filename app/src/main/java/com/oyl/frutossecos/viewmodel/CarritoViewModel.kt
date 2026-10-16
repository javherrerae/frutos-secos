
package com.oyl.frutossecos.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.oyl.frutossecos.model.ItemCarrito
import com.oyl.frutossecos.model.Producto

class CarritoViewModel : ViewModel() {

    // Lista compartida de productos del carrito
    private val _items = mutableStateListOf<ItemCarrito>()

    val items: List<ItemCarrito>
        get() = _items.toList()

    // Cantidad total de bolsas
    val cantidadTotal: Int
        get() = _items.sumOf { it.cantidad }

    // Suma de los subtotales
    val subtotal: Int
        get() = _items.sumOf { it.subtotal }

    // Agregar producto o aumentar su cantidad
    fun agregarProducto(producto: Producto, cantidad: Int) {
        if (cantidad <= 0 || producto.stock <= 0) return

        val indice = _items.indexOfFirst {
            it.producto.id == producto.id
        }

        if (indice >= 0) {
            val itemActual = _items[indice]
            val nuevaCantidad = itemActual.cantidad + cantidad

            if (nuevaCantidad <= producto.stock) {
                _items[indice] = itemActual.copy(
                    cantidad = nuevaCantidad
                )
            }
        } else if (cantidad <= producto.stock) {
            _items.add(
                ItemCarrito(
                    producto = producto,
                    cantidad = cantidad
                )
            )
        }
    }

    // Aumentar una unidad
    fun aumentarCantidad(productoId: Int) {
        val indice = _items.indexOfFirst {
            it.producto.id == productoId
        }

        if (indice >= 0) {
            val item = _items[indice]

            if (item.cantidad < item.producto.stock) {
                _items[indice] = item.copy(
                    cantidad = item.cantidad + 1
                )
            }
        }
    }

    // Disminuir una unidad o eliminar el producto
    fun disminuirCantidad(productoId: Int) {
        val indice = _items.indexOfFirst {
            it.producto.id == productoId
        }

        if (indice >= 0) {
            val item = _items[indice]

            if (item.cantidad > 1) {
                _items[indice] = item.copy(
                    cantidad = item.cantidad - 1
                )
            } else {
                _items.removeAt(indice)
            }
        }
    }

    // Eliminar un producto completo
    fun eliminarProducto(productoId: Int) {
        _items.removeAll {
            it.producto.id == productoId
        }
    }

    // Vaciar el carrito después de confirmar el pedido
    fun vaciarCarrito() {
        _items.clear()
    }
}
