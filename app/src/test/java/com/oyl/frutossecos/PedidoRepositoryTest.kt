package com.oyl.frutossecos

import com.oyl.frutossecos.api.PedidoApi
import com.oyl.frutossecos.dto.*
import com.oyl.frutossecos.repository.PedidoRepository
import com.oyl.frutossecos.repository.ResultadoSync
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class PedidoRepositoryTest {
    private val item = PedidoItemDto(3, 8, 5, "Nuez", 2, 1000.0)
    private val pedido = PedidoDto(8, 4, "2026-01-01", 2000.0, "Retiro", "Confirmado", "P8", listOf(item))
    private var respuesta: Any = Response.success(pedido)
    private val guardados = mutableListOf<Pedido>()
    private val detalles = mutableListOf<PedidoItem>()
    private var cambio = ""
    private var operacion = ""
    private val dao = Proxy.newProxyInstance(PedidoDao::class.java.classLoader, arrayOf(PedidoDao::class.java)) { _, metodo, args ->
        when (metodo.name) {
            "insertar" -> { guardados.add(args[0] as Pedido); 1L }
            "obtenerTodos", "obtenerPorUsuario" -> guardados.toList()
            "actualizarEstado" -> { cambio = "${args[0]}:${args[1]}"; null }
            else -> error("DAO inesperado: ${metodo.name}")
        }
    } as PedidoDao
    private val detalleDao = Proxy.newProxyInstance(PedidoItemDao::class.java.classLoader, arrayOf(PedidoItemDao::class.java)) { _, metodo, args ->
        if (metodo.name == "insertarSiNoExisten") {
            @Suppress("UNCHECKED_CAST")
            detalles.addAll(args[0] as List<PedidoItem>); null
        } else error("DAO inesperado: ${metodo.name}")
    } as PedidoItemDao
    private val api = Proxy.newProxyInstance(PedidoApi::class.java.classLoader, arrayOf(PedidoApi::class.java)) { _, metodo, args ->
        if (metodo.name == "crearPedido") operacion = args[1] as String
        if (respuesta is IllegalStateException) throw respuesta as IllegalStateException
        respuesta
    } as PedidoApi
    private val repo = PedidoRepository(dao, detalleDao, api)

    @Test fun crearGuardaPedidoYDetalleConClaveDeOperacion() = runBlocking {
        val request = CrearPedidoRequest(4, "Retiro", listOf(item))
        val resultado = repo.crearPedido(request, "clave-1") as ResultadoSync.Exito
        assertEquals("clave-1", operacion)
        assertEquals("P8", resultado.datos.numeroPedido)
        assertEquals(resultado.datos, guardados.single())
        assertEquals(2, detalles.single().cantidad)
    }

    @Test fun lecturasActualizanCacheYRecuperanAnteError() = runBlocking {
        respuesta = Response.success(listOf(pedido))
        assertEquals(1, (repo.obtenerTodos() as ResultadoSync.Exito).datos.size)
        assertEquals(1, (repo.obtenerPorUsuario(4) as ResultadoSync.Exito).datos.size)
        assertEquals(2, detalles.size)
        respuesta = Response.error<List<PedidoDto>>(503, "error".toResponseBody())
        assertEquals(2, (repo.obtenerTodos() as ResultadoSync.DesdeCacheConError).datos.size)
        respuesta = IllegalStateException("sin red")
        assertTrue((repo.obtenerPorUsuario(4) as ResultadoSync.DesdeCacheConError).mensajeError.contains("sin red"))
    }

    @Test fun actualizarSoloPersisteTrasConfirmacion() = runBlocking {
        assertTrue(repo.actualizarEstado(8, "Entregado") is ResultadoSync.Exito)
        assertEquals("8:Entregado", cambio)
        respuesta = Response.error<PedidoDto>(409, "error".toResponseBody())
        assertTrue((repo.actualizarEstado(8, "Cancelado") as ResultadoSync.Error).mensajeError.contains("409"))
        assertEquals("8:Entregado", cambio)
        respuesta = IllegalStateException("sin red")
        assertTrue((repo.crearPedido(CrearPedidoRequest(4, "Retiro", emptyList()), "clave") as ResultadoSync.Error).mensajeError.contains("sin red"))
    }
}
