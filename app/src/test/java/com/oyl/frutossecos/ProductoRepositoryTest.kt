package com.oyl.frutossecos

import com.oyl.frutossecos.api.ProductoApi
import com.oyl.frutossecos.dto.ActualizarStockRequest
import com.oyl.frutossecos.dto.ProductoDto
import com.oyl.frutossecos.repository.ProductoRepository
import com.oyl.frutossecos.repository.ResultadoSync
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

import java.lang.reflect.Proxy

class ProductoRepositoryTest {
    private val activo = ProductoDto(1, "Almendras", "Frutos", "Natural", 1500.0, "500 g", 5, "", true)
    private val inactivo = activo.copy(id = 2, activo = false)
    private val cache = Producto(3, "Nuez", "Frutos", "", 900.0, "100 g", 2, "", 1)
    private var guardados = emptyList<Producto>()
    private var actualizado: Producto? = null
    private var requestStock: ActualizarStockRequest? = null
    private var respuesta: Any = Response.success(listOf(activo, inactivo))

    private val dao = Proxy.newProxyInstance(ProductoDao::class.java.classLoader, arrayOf(ProductoDao::class.java)) { _, metodo, args ->
        when (metodo.name) {
            "reemplazarCatalogo" -> { @Suppress("UNCHECKED_CAST") guardados = args[0] as List<Producto>; null }
            "obtenerCatalogo", "obtenerPorCategoria" -> listOf(cache)
            "actualizar" -> { actualizado = args[0] as Producto; null }
            else -> error("DAO inesperado: ${metodo.name}")
        }
    } as ProductoDao
    private val api = Proxy.newProxyInstance(ProductoApi::class.java.classLoader, arrayOf(ProductoApi::class.java)) { _, metodo, args ->
        if (metodo.name == "actualizarStock") requestStock = args[1] as ActualizarStockRequest
        if (respuesta is IllegalStateException) throw respuesta as IllegalStateException
        respuesta
    } as ProductoApi
    private val repo = ProductoRepository(dao, api)

    @Test fun catalogoActualizaCacheYEntregaSoloActivos() = runBlocking {
        val resultado = repo.sincronizarCatalogo() as ResultadoSync.Exito
        assertEquals(listOf(1), resultado.datos.map { it.id })
        assertEquals(listOf(1, 0), guardados.map { it.activo })
    }

    @Test fun catalogoUsaCacheAnteErrorHttpYConexion() = runBlocking {
        respuesta = Response.error<List<ProductoDto>>(503, "error".toResponseBody())
        val http = repo.sincronizarCatalogo() as ResultadoSync.DesdeCacheConError
        assertEquals(listOf(cache), http.datos)
        assertTrue(http.mensajeError.contains("503"))
        respuesta = IllegalStateException("sin red")
        val red = repo.sincronizarCatalogo() as ResultadoSync.DesdeCacheConError
        assertEquals(listOf(cache), red.datos)
        assertTrue(red.mensajeError.contains("sin red"))
    }

    @Test fun categoriaUsaRespuestaORecuperaCache() = runBlocking {
        assertEquals(2, (repo.obtenerPorCategoria("Frutos") as ResultadoSync.Exito).datos.size)
        respuesta = Response.error<List<ProductoDto>>(500, "error".toResponseBody())
        assertEquals(listOf(cache), (repo.obtenerPorCategoria("Frutos") as ResultadoSync.DesdeCacheConError).datos)
        respuesta = IllegalStateException("sin red")
        assertTrue((repo.obtenerPorCategoria("Frutos") as ResultadoSync.DesdeCacheConError).mensajeError.contains("sin red"))
    }

    @Test fun stockActualizaCacheSoloSiServidorConfirma() = runBlocking {
        respuesta = Response.success(activo.copy(stock = 0))
        val resultado = repo.actualizarStock(1, 0, 1200.0) as ResultadoSync.Exito
        assertEquals(0, resultado.datos.stock)
        assertEquals(resultado.datos, actualizado)
        assertEquals(0, requestStock!!.stock)
        respuesta = Response.error<ProductoDto>(409, "error".toResponseBody())
        assertTrue((repo.actualizarStock(1, 2) as ResultadoSync.Error).mensajeError.contains("409"))
        respuesta = IllegalStateException("sin red")
        assertTrue((repo.actualizarStock(1, 2) as ResultadoSync.Error).mensajeError.contains("sin red"))
    }
}
