package com.oyl.frutossecos.repository

import com.oyl.frutossecos.Pedido
import com.oyl.frutossecos.PedidoDao
import com.oyl.frutossecos.PedidoItem
import com.oyl.frutossecos.PedidoItemDao
import com.oyl.frutossecos.dto.ActualizarEstadoRequest
import com.oyl.frutossecos.dto.CrearPedidoRequest
import com.oyl.frutossecos.dto.PedidoDto
import com.oyl.frutossecos.dto.PedidoItemDto
import com.oyl.frutossecos.network.RetrofitClient

/**
 * Repositorio que coordina la gestión de pedidos entre la API REST
 * (Spring Boot) y la base local (Room/SQLite).
 *
 * Las operaciones de escritura (crear pedido, actualizar estado) siempre
 * requieren conexión al servidor, ya que este es la fuente oficial.
 * Las lecturas utilizan fallback a caché local.
 */
class PedidoRepository(
    private val pedidoDao: PedidoDao,
    private val pedidoItemDao: PedidoItemDao
) {

    private val api = RetrofitClient.pedidoApi

    /**
     * Crea un nuevo pedido en el servidor.
     * El backend calcula totales, asigna estado inicial y rebaja el stock.
     * Si tiene éxito, almacena una copia local del pedido.
     */
    suspend fun crearPedido(request: CrearPedidoRequest): ResultadoSync<Pedido> {
        return try {
            val respuesta = api.crearPedido(request)
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val pedidoDto = respuesta.body()!!
                val pedidoLocal = pedidoDto.toEntity()
                pedidoDao.insertar(pedidoLocal)
                // Guardar detalle de ítems en caché local
                pedidoDto.items?.let { items ->
                    pedidoItemDao.insertarSiNoExisten(items.map { it.toEntity() })
                }
                ResultadoSync.Exito(pedidoLocal)
            } else {
                ResultadoSync.Error("No se pudo crear el pedido: ${respuesta.code()} ${respuesta.message()}")
            }
        } catch (e: Exception) {
            ResultadoSync.Error("Sin conexión al servidor: ${e.localizedMessage}")
        }
    }

    /**
     * Obtiene todos los pedidos (vista Administrador) desde el servidor.
     * Actualiza la caché local y hace fallback si no hay conexión.
     */
    suspend fun obtenerTodos(): ResultadoSync<List<Pedido>> {
        return try {
            val respuesta = api.obtenerTodos()
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val pedidos = respuesta.body()!!.map { it.toEntity() }
                pedidoDao.insertarSiNoExisten(pedidos)
                ResultadoSync.Exito(pedidos)
            } else {
                val cache = pedidoDao.obtenerTodos()
                ResultadoSync.DesdeCacheConError(cache, "Error del servidor: ${respuesta.code()}")
            }
        } catch (e: Exception) {
            val cache = pedidoDao.obtenerTodos()
            ResultadoSync.DesdeCacheConError(cache, "Sin conexión: ${e.localizedMessage}")
        }
    }

    /**
     * Obtiene los pedidos de un cliente específico.
     */
    suspend fun obtenerPorUsuario(idUsuario: Int): ResultadoSync<List<Pedido>> {
        return try {
            val respuesta = api.obtenerPorUsuario(idUsuario)
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val pedidos = respuesta.body()!!.map { it.toEntity() }
                pedidoDao.insertarSiNoExisten(pedidos)
                ResultadoSync.Exito(pedidos)
            } else {
                val cache = pedidoDao.obtenerPorUsuario(idUsuario)
                ResultadoSync.DesdeCacheConError(cache, "Error del servidor: ${respuesta.code()}")
            }
        } catch (e: Exception) {
            val cache = pedidoDao.obtenerPorUsuario(idUsuario)
            ResultadoSync.DesdeCacheConError(cache, "Sin conexión: ${e.localizedMessage}")
        }
    }

    /**
     * Actualiza el estado de un pedido (solo Administrador).
     * Actualiza la caché local tras confirmación del servidor.
     */
    suspend fun actualizarEstado(idPedido: Int, nuevoEstado: String): ResultadoSync<Pedido> {
        return try {
            val request = ActualizarEstadoRequest(estado = nuevoEstado)
            val respuesta = api.actualizarEstado(idPedido, request)
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val pedidoActualizado = respuesta.body()!!.toEntity()
                pedidoDao.actualizarEstado(idPedido, nuevoEstado)
                ResultadoSync.Exito(pedidoActualizado)
            } else {
                ResultadoSync.Error("No se pudo actualizar: ${respuesta.code()} ${respuesta.message()}")
            }
        } catch (e: Exception) {
            ResultadoSync.Error("Sin conexión al servidor: ${e.localizedMessage}")
        }
    }
}

/**
 * Convierte un PedidoDto (formato API) a Pedido (entidad Room).
 */
fun PedidoDto.toEntity(): Pedido = Pedido(
    id = id,
    idUsuario = idUsuario,
    fecha = fecha,
    total = total,
    modalidadEntrega = modalidadEntrega,
    estado = estado,
    numeroPedido = numeroPedido
)

/**
 * Convierte un PedidoItemDto (formato API) a PedidoItem (entidad Room).
 */
fun PedidoItemDto.toEntity(): PedidoItem = PedidoItem(
    id = id,
    idPedido = idPedido,
    idProducto = idProducto,
    nombreProducto = nombreProducto,
    cantidad = cantidad,
    precioUnitario = precioUnitario
)
