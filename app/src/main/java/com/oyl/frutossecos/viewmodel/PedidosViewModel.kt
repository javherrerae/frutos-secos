package com.oyl.frutossecos.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oyl.frutossecos.AppDatabase
import com.oyl.frutossecos.Pedido
import com.oyl.frutossecos.dto.*
import com.oyl.frutossecos.model.*
import com.oyl.frutossecos.repository.PedidoRepository
import com.oyl.frutossecos.repository.ResultadoSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class PedidosViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.crear(application)
    private val repository = PedidoRepository(db.pedidoDao(), db.pedidoItemDao())
    private val _pedidos = mutableStateListOf<PedidoDemo>()
    val pedidos: List<PedidoDemo> get() = _pedidos.toList()
    var cargando by mutableStateOf(false)
        private set
    var mensajeUltimoPedido by mutableStateOf<String?>(null)
        private set
    private var idUsuarioActivo: Int = 0
    private var ultimaSolicitud: CrearPedidoRequest? = null
    private var claveOperacion: String = UUID.randomUUID().toString()

    fun configurarUsuario(idUsuario: Int) {
        idUsuarioActivo = idUsuario
        cargarPedidos()
    }

    fun cargarPedidos(idUsuario: Int = idUsuarioActivo) {
        if (idUsuario <= 0) return
        viewModelScope.launch {
            val listado = withContext(Dispatchers.IO) {
                when (val resultado = repository.obtenerPorUsuario(idUsuario)) {
                    is ResultadoSync.Exito -> resultado.datos.map { convertir(it) }
                    is ResultadoSync.DesdeCacheConError -> resultado.datos.map { convertir(it) }
                    is ResultadoSync.Error -> emptyList()
                }
            }
            _pedidos.clear()
            _pedidos.addAll(listado)
        }
    }

    fun confirmar(items: List<ItemCarrito>, tienda: TiendaDemo = TiendasDemo.todas.first(),
            onCompletado: (PedidoDemo?) -> Unit) {
        if (cargando) return
        if (idUsuarioActivo <= 0 || items.isEmpty() || items.any { it.cantidad <= 0 }) {
            mensajeUltimoPedido = "Inicia sesión y revisa el carrito."
            onCompletado(null)
            return
        }
        val request = CrearPedidoRequest(
            idUsuarioActivo, "Retiro en ${tienda.comuna}",
            items.map { PedidoItemDto(idProducto = it.producto.id, nombreProducto = it.producto.nombre,
                cantidad = it.cantidad, precioUnitario = it.producto.precio.toDouble()) }
        )
        if (ultimaSolicitud != request) {
            ultimaSolicitud = request
            claveOperacion = UUID.randomUUID().toString()
        }
        cargando = true
        viewModelScope.launch {
            try {
                val pedido = withContext(Dispatchers.IO) {
                    when (val resultado = repository.crearPedido(request, claveOperacion)) {
                        is ResultadoSync.Exito -> convertir(resultado.datos)
                        is ResultadoSync.Error -> {
                            mensajeUltimoPedido = resultado.mensajeError
                            null
                        }
                        is ResultadoSync.DesdeCacheConError -> null
                    }
                }
                if (pedido != null) {
                    mensajeUltimoPedido = "Pedido confirmado por el servidor."
                    _pedidos.removeAll { it.codigo == pedido.codigo }
                    _pedidos.add(0, pedido)
                    ultimaSolicitud = null
                }
                onCompletado(pedido)
            } finally { cargando = false }
        }
    }

    fun buscar(codigo: String?): PedidoDemo? = _pedidos.find { it.codigo == codigo }

    fun limpiarSesion() {
        _pedidos.clear()
        idUsuarioActivo = 0
        ultimaSolicitud = null
        mensajeUltimoPedido = null
    }

    private fun convertir(pedido: Pedido): PedidoDemo {
        val fecha = runCatching {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(pedido.fecha)?.time
        }.getOrNull() ?: System.currentTimeMillis()
        val lineas = db.pedidoItemDao().obtenerPorPedido(pedido.id).map {
            LineaPedidoDemo(it.nombreProducto, "", it.precioUnitario.toInt(), it.cantidad)
        }
        return PedidoDemo(pedido.numeroPedido, fecha, lineas, pedido.estado,
            pedido.modalidadEntrega.removePrefix("Retiro en "), pedido.modalidadEntrega,
            totalDirecto = pedido.total.toInt())
    }
}
