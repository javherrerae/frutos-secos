package com.oyl.frutossecos.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oyl.frutossecos.AppDatabase
import com.oyl.frutossecos.Pedido
import com.oyl.frutossecos.dto.CrearPedidoRequest
import com.oyl.frutossecos.dto.PedidoItemDto
import com.oyl.frutossecos.model.ItemCarrito
import com.oyl.frutossecos.model.LineaPedidoDemo
import com.oyl.frutossecos.model.PedidoDemo
import com.oyl.frutossecos.model.TiendaDemo
import com.oyl.frutossecos.model.TiendasDemo
import com.oyl.frutossecos.repository.PedidoRepository
import com.oyl.frutossecos.repository.ResultadoSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * ViewModel que gestiona el ciclo de vida de los pedidos.
 * Conecta la confirmación y consulta de pedidos con la API REST de Spring Boot
 * a través de PedidoRepository, guardando una copia en Room (SQLite).
 */
class PedidosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.crear(application)
    private val repository = PedidoRepository(db.pedidoDao(), db.pedidoItemDao())

    private val _pedidos = mutableStateListOf<PedidoDemo>()
    val pedidos: List<PedidoDemo> get() = _pedidos.toList()

    var cargando by mutableStateOf(false)
        private set

    var mensajeUltimoPedido by mutableStateOf<String?>(null)
        private set

    var idUsuarioActivo: Int = 1

    fun configurarUsuario(idUsuario: Int) {
        idUsuarioActivo = idUsuario
        cargarPedidos(idUsuario)
    }

    /**
     * Consulta los pedidos del usuario en el servidor backend (o Room si offline).
     */
    fun cargarPedidos(idUsuario: Int = idUsuarioActivo) {
        viewModelScope.launch(Dispatchers.IO) {
            val resultado = repository.obtenerPorUsuario(idUsuario)
            withContext(Dispatchers.Main) {
                when (resultado) {
                    is ResultadoSync.Exito -> {
                        _pedidos.clear()
                        _pedidos.addAll(resultado.datos.map { it.toDemoModel() })
                    }
                    is ResultadoSync.DesdeCacheConError -> {
                        _pedidos.clear()
                        _pedidos.addAll(resultado.datos.map { it.toDemoModel() })
                    }
                    is ResultadoSync.Error -> {
                        // Mantener los que ya estén en memoria
                    }
                }
            }
        }
    }

    /**
     * Confirma el pedido enviándolo a la API REST de Spring Boot.
     * Si el servidor no responde, lo guarda localmente en Room con estado inicial.
     */
    fun confirmar(
        items: List<ItemCarrito>,
        tienda: TiendaDemo = TiendasDemo.todas.first(),
        onCompletado: (PedidoDemo?) -> Unit
    ) {
        if (items.isEmpty() || items.any {
                it.cantidad <= 0 || it.cantidad > it.producto.stock || it.producto.precio < 0
            }) {
            onCompletado(null)
            return
        }

        cargando = true
        val totalCalculado = items.sumOf { it.subtotal }.toDouble()
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fechaActual = formatoFecha.format(Date())

        val request = CrearPedidoRequest(
            idUsuario = idUsuarioActivo,
            modalidadEntrega = "Retiro en ${tienda.comuna}",
            items = items.map {
                PedidoItemDto(
                    idProducto = it.producto.id,
                    nombreProducto = it.producto.nombre,
                    cantidad = it.cantidad,
                    precioUnitario = it.producto.precio.toDouble()
                )
            }
        )

        viewModelScope.launch(Dispatchers.IO) {
            val resultado = repository.crearPedido(request)

            val (pedidoCreado, mensaje) = when (resultado) {
                is ResultadoSync.Exito -> {
                    val entity = resultado.datos
                    val demo = PedidoDemo(
                        codigo = entity.numeroPedido,
                        fechaMillis = System.currentTimeMillis(),
                        puntoRetiro = tienda.comuna,
                        direccionRetiro = "${tienda.direccion} · ${tienda.comuna}",
                        estado = entity.estado,
                        productos = items.map {
                            LineaPedidoDemo(
                                nombre = it.producto.nombre,
                                formato = it.producto.peso,
                                precioUnitario = it.producto.precio,
                                cantidad = it.cantidad
                            )
                        }
                    )
                    Pair(demo, "¡Pedido enviado y registrado en el servidor central!")
                }
                else -> {
                    // Fallback offline a Room ejecutado de forma segura en Dispatchers.IO
                    val numLocal = "OYL-PED-" + UUID.randomUUID().toString().take(6).uppercase()
                    runCatching {
                        val idLocal = (db.pedidoDao().obtenerTodos().maxOfOrNull { it.id } ?: 0) + 1
                        val pedidoLocal = Pedido(
                            id = idLocal,
                            idUsuario = idUsuarioActivo,
                            fecha = fechaActual,
                            total = totalCalculado,
                            modalidadEntrega = "Retiro en ${tienda.comuna}",
                            estado = "Pendiente de Validación",
                            numeroPedido = numLocal
                        )
                        db.pedidoDao().insertar(pedidoLocal)
                    }

                    val demo = PedidoDemo(
                        codigo = numLocal,
                        fechaMillis = System.currentTimeMillis(),
                        puntoRetiro = tienda.comuna,
                        direccionRetiro = "${tienda.direccion} · ${tienda.comuna}",
                        estado = "Pendiente de Validación",
                        productos = items.map {
                            LineaPedidoDemo(
                                nombre = it.producto.nombre,
                                formato = it.producto.peso,
                                precioUnitario = it.producto.precio,
                                cantidad = it.cantidad
                            )
                        },
                        totalDirecto = totalCalculado.toInt()
                    )
                    Pair(demo, "Pedido guardado localmente (servidor no disponible)")
                }
            }

            withContext(Dispatchers.Main) {
                cargando = false
                mensajeUltimoPedido = mensaje
                _pedidos.add(0, pedidoCreado)
                onCompletado(pedidoCreado)
            }
        }
    }

    fun buscar(codigo: String?): PedidoDemo? = _pedidos.find { it.codigo == codigo }

    fun limpiarSesion() {
        _pedidos.clear()
        mensajeUltimoPedido = null
    }

    private fun Pedido.toDemoModel(): PedidoDemo {
        val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fechaMillis = runCatching { formato.parse(fecha)?.time }.getOrNull() ?: System.currentTimeMillis()
        return PedidoDemo(
            codigo = numeroPedido,
            fechaMillis = fechaMillis,
            puntoRetiro = modalidadEntrega.removePrefix("Retiro en "),
            direccionRetiro = modalidadEntrega,
            estado = estado,
            productos = emptyList(),
            totalDirecto = total.toInt()
        )
    }
}
