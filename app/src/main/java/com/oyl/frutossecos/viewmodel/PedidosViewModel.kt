package com.oyl.frutossecos.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.oyl.frutossecos.model.ItemCarrito
import com.oyl.frutossecos.model.LineaPedidoDemo
import com.oyl.frutossecos.model.PedidoDemo
import com.oyl.frutossecos.model.TiendaDemo
import com.oyl.frutossecos.model.TiendasDemo
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID

/**
 * Historial LOCAL de demostración. No realiza compras ni utiliza la tabla Room
 * de pedidos. Sobrevive al cierre de la aplicación, pero se borra al cerrar sesión.
 */
class PedidosViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencias = application.getSharedPreferences(
        "oyl_historial_pedidos_demo", Context.MODE_PRIVATE
    )

    private val _pedidos = mutableStateListOf<PedidoDemo>()
    val pedidos: List<PedidoDemo> get() = _pedidos.toList()

    init {
        recuperarPedidos()
    }

    fun confirmar(
        items: List<ItemCarrito>,
        tienda: TiendaDemo = TiendasDemo.todas.first()
    ): PedidoDemo? {
        if (items.isEmpty() || items.any {
                it.cantidad <= 0 || it.cantidad > it.producto.stock || it.producto.precio < 0
            }) return null

        val pedido = PedidoDemo(
            codigo = "OYL-" + UUID.randomUUID().toString().take(8).uppercase(),
            fechaMillis = System.currentTimeMillis(),
            puntoRetiro = tienda.comuna,
            direccionRetiro = "${tienda.direccion} · ${tienda.comuna}",
            productos = items.map {
                LineaPedidoDemo(
                    nombre = it.producto.nombre,
                    formato = it.producto.peso,
                    precioUnitario = it.producto.precio,
                    cantidad = it.cantidad
                )
            }
        )
        _pedidos.add(0, pedido)
        guardarPedidos()
        return pedido
    }

    fun buscar(codigo: String?): PedidoDemo? = _pedidos.find { it.codigo == codigo }

    fun limpiarSesion() {
        _pedidos.clear()
        preferencias.edit().remove("pedidos").apply()
    }

    private fun guardarPedidos() {
        val lista = JSONArray()
        _pedidos.forEach { pedido ->
            val productosJson = JSONArray()
            pedido.productos.forEach { producto ->
                productosJson.put(
                    JSONObject()
                        .put("nombre", producto.nombre)
                        .put("formato", producto.formato)
                        .put("precioUnitario", producto.precioUnitario)
                        .put("cantidad", producto.cantidad)
                )
            }
            lista.put(
                JSONObject()
                    .put("codigo", pedido.codigo)
                    .put("fechaMillis", pedido.fechaMillis)
                    .put("estado", pedido.estado)
                    .put("puntoRetiro", pedido.puntoRetiro)
                    .put("direccionRetiro", pedido.direccionRetiro)
                    .put("productos", productosJson)
            )
        }
        preferencias.edit().putString("pedidos", lista.toString()).apply()
    }

    private fun recuperarPedidos() {
        val json = preferencias.getString("pedidos", "[]") ?: "[]"
        try {
            val lista = JSONArray(json)
            val recuperados = mutableListOf<PedidoDemo>()

            for (i in 0 until lista.length()) {
                val pedidoJson = lista.getJSONObject(i)
                val productosJson = pedidoJson.getJSONArray("productos")
                val productos = mutableListOf<LineaPedidoDemo>()

                for (j in 0 until productosJson.length()) {
                    val productoJson = productosJson.getJSONObject(j)
                    productos.add(
                        LineaPedidoDemo(
                            nombre = productoJson.getString("nombre"),
                            formato = productoJson.getString("formato"),
                            precioUnitario = productoJson.getInt("precioUnitario"),
                            cantidad = productoJson.getInt("cantidad")
                        )
                    )
                }

                recuperados.add(
                    PedidoDemo(
                        codigo = pedidoJson.getString("codigo"),
                        fechaMillis = pedidoJson.getLong("fechaMillis"),
                        productos = productos,
                        estado = pedidoJson.optString("estado", "Confirmado"),
                        puntoRetiro = pedidoJson.optString("puntoRetiro", "Providencia"),
                        direccionRetiro = pedidoJson.optString("direccionRetiro", "")
                    )
                )
            }
            _pedidos.addAll(recuperados)
        } catch (_: JSONException) {
            // Si los datos locales están dañados, la app sigue funcionando
            // con la lista vacía; no alteramos la base Room de otros módulos.
        }
    }
}
