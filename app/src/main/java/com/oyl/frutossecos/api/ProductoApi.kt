package com.oyl.frutossecos.api

import com.oyl.frutossecos.dto.ActualizarStockRequest
import com.oyl.frutossecos.dto.ProductoDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz Retrofit que define los endpoints REST del catálogo de productos.
 * Base URL: {servidor}/api/productos
 */
interface ProductoApi {

    /**
     * Obtiene el catálogo completo de productos activos.
     * GET /api/productos
     */
    @GET("api/productos")
    suspend fun obtenerCatalogo(): Response<List<ProductoDto>>

    /**
     * Filtra productos activos por categoría.
     * GET /api/productos?categoria=Frutos+secos
     */
    @GET("api/productos")
    suspend fun obtenerPorCategoria(
        @Query("categoria") categoria: String
    ): Response<List<ProductoDto>>

    /**
     * Obtiene un producto específico por su identificador.
     * GET /api/productos/{id}
     */
    @GET("api/productos/{id}")
    suspend fun obtenerPorId(
        @Path("id") id: Int
    ): Response<ProductoDto>

    /**
     * Actualiza el stock y opcionalmente el precio de un producto (Administrador).
     * PUT /api/productos/{id}/stock
     */
    @PUT("api/productos/{id}/stock")
    suspend fun actualizarStock(
        @Path("id") id: Int,
        @Body request: ActualizarStockRequest
    ): Response<ProductoDto>
}
