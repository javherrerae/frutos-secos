package com.oyl.frutossecos.repository

import com.oyl.frutossecos.Producto
import com.oyl.frutossecos.ProductoDao
import com.oyl.frutossecos.dto.ActualizarStockRequest
import com.oyl.frutossecos.dto.ProductoDto
import com.oyl.frutossecos.network.RetrofitClient

/**
 * Repositorio que coordina la sincronización del catálogo de productos
 * entre la API REST (Spring Boot) y la caché local (Room/SQLite).
 *
 * Estrategia: Network-first con fallback a caché local.
 * 1. Intenta obtener datos frescos del servidor.
 * 2. Si tiene éxito, actualiza la caché local y devuelve los datos.
 * 3. Si falla (sin conexión), devuelve los datos de la caché local.
 */
class ProductoRepository(private val productoDao: ProductoDao) {

    private val api = RetrofitClient.productoApi

    /**
     * Sincroniza el catálogo completo desde el servidor y actualiza la caché.
     * @return Lista de productos activos (del servidor si hay conexión, de Room si no).
     */
    suspend fun sincronizarCatalogo(): ResultadoSync<List<Producto>> {
        return try {
            val respuesta = api.obtenerCatalogo()
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val productosDto = respuesta.body()!!
                val productosLocal = productosDto.map { it.toEntity() }
                // Reemplazar caché local con datos frescos del servidor
                productoDao.insertarSiNoExisten(productosLocal)
                productosLocal.forEach { productoDao.actualizar(it) }
                ResultadoSync.Exito(productosLocal.filter { it.activo == 1 })
            } else {
                // Error del servidor: usar caché local
                val cache = productoDao.obtenerCatalogo()
                ResultadoSync.DesdeCacheConError(
                    datos = cache,
                    mensajeError = "Error del servidor: ${respuesta.code()} ${respuesta.message()}"
                )
            }
        } catch (e: Exception) {
            // Sin conexión: usar caché local
            val cache = productoDao.obtenerCatalogo()
            ResultadoSync.DesdeCacheConError(
                datos = cache,
                mensajeError = "Sin conexión al servidor: ${e.localizedMessage}"
            )
        }
    }

    /**
     * Obtiene productos filtrados por categoría desde el servidor.
     * @param categoria Categoría a filtrar ("Frutos secos", "Semillas", "Deshidratados").
     */
    suspend fun obtenerPorCategoria(categoria: String): ResultadoSync<List<Producto>> {
        return try {
            val respuesta = api.obtenerPorCategoria(categoria)
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val productosLocal = respuesta.body()!!.map { it.toEntity() }
                ResultadoSync.Exito(productosLocal)
            } else {
                val cache = productoDao.obtenerPorCategoria(categoria)
                ResultadoSync.DesdeCacheConError(cache, "Error del servidor: ${respuesta.code()}")
            }
        } catch (e: Exception) {
            val cache = productoDao.obtenerPorCategoria(categoria)
            ResultadoSync.DesdeCacheConError(cache, "Sin conexión: ${e.localizedMessage}")
        }
    }

    /**
     * Actualiza el stock de un producto en el servidor (solo Administrador).
     * Si tiene éxito, también actualiza la caché local.
     */
    suspend fun actualizarStock(
        idProducto: Int,
        nuevoStock: Int,
        nuevoPrecio: Double? = null
    ): ResultadoSync<Producto> {
        return try {
            val request = ActualizarStockRequest(stock = nuevoStock, precio = nuevoPrecio)
            val respuesta = api.actualizarStock(idProducto, request)
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val productoActualizado = respuesta.body()!!.toEntity()
                productoDao.actualizar(productoActualizado)
                ResultadoSync.Exito(productoActualizado)
            } else {
                ResultadoSync.Error("No se pudo actualizar: ${respuesta.code()} ${respuesta.message()}")
            }
        } catch (e: Exception) {
            ResultadoSync.Error("Sin conexión al servidor: ${e.localizedMessage}")
        }
    }
}

/**
 * Función de extensión que convierte un ProductoDto (formato API) a Producto (entidad Room).
 * Transforma el booleano del backend al entero que usa SQLite (1/0).
 */
fun ProductoDto.toEntity(): Producto = Producto(
    id = id,
    nombre = nombre,
    categoria = categoria,
    descripcion = descripcion,
    precio = precio,
    presentacion = presentacion,
    stock = stock,
    rutaImagen = rutaImagen,
    activo = if (activo) 1 else 0
)
