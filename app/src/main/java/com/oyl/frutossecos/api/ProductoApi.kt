package com.oyl.frutossecos.api

import com.oyl.frutossecos.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ProductoApi {
    @POST("api/productos/{id}/movimientos")
    suspend fun registrarMovimiento(@Path("id") id: Int, @Body request: MovimientoStockRequest): Response<ProductoDto>
    @GET("api/productos")
    suspend fun obtenerCatalogo(): Response<List<ProductoDto>>
    @GET("api/productos")
    suspend fun obtenerPorCategoria(@Query("categoria") categoria: String): Response<List<ProductoDto>>
    @GET("api/productos/{id}")
    suspend fun obtenerPorId(@Path("id") id: Int): Response<ProductoDto>
    @PUT("api/productos/{id}/stock")
    suspend fun actualizarStock(@Path("id") id: Int, @Body request: ActualizarStockRequest): Response<ProductoDto>
    @GET("api/productos/admin")
    suspend fun obtenerAdministracion(): Response<List<ProductoDto>>
    @POST("api/productos")
    suspend fun crear(@Body request: ProductoRequest): Response<ProductoDto>
    @PUT("api/productos/{id}")
    suspend fun editar(@Path("id") id: Int, @Body request: ProductoRequest): Response<ProductoDto>
    @PATCH("api/productos/{id}/activo")
    suspend fun cambiarActivo(@Path("id") id: Int, @Body request: EstadoProductoRequest): Response<ProductoDto>
}
