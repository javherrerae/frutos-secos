package com.oyl.frutossecos.api

import com.oyl.frutossecos.dto.ActualizarEstadoRequest
import com.oyl.frutossecos.dto.CrearPedidoRequest
import com.oyl.frutossecos.dto.PedidoDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz Retrofit que define los endpoints REST para la gestión de pedidos.
 * Base URL: {servidor}/api/pedidos
 */
interface PedidoApi {

    /**
     * Crea un nuevo pedido con su detalle de artículos.
     * El backend calcula el total, asigna estado "Pendiente de Validación" y rebaja stock.
     * POST /api/pedidos
     */
    @POST("api/pedidos")
    suspend fun crearPedido(
        @Body request: CrearPedidoRequest
    ): Response<PedidoDto>

    /**
     * Obtiene todos los pedidos (vista Administrador).
     * GET /api/pedidos
     */
    @GET("api/pedidos")
    suspend fun obtenerTodos(): Response<List<PedidoDto>>

    /**
     * Obtiene los pedidos de un cliente específico.
     * GET /api/pedidos?idUsuario=2
     */
    @GET("api/pedidos")
    suspend fun obtenerPorUsuario(
        @Query("idUsuario") idUsuario: Int
    ): Response<List<PedidoDto>>

    /**
     * Obtiene un pedido específico con su detalle.
     * GET /api/pedidos/{id}
     */
    @GET("api/pedidos/{id}")
    suspend fun obtenerPorId(
        @Path("id") id: Int
    ): Response<PedidoDto>

    /**
     * Actualiza el estado de un pedido (solo Administrador).
     * PATCH /api/pedidos/{id}/estado
     */
    @PATCH("api/pedidos/{id}/estado")
    suspend fun actualizarEstado(
        @Path("id") id: Int,
        @Body request: ActualizarEstadoRequest
    ): Response<PedidoDto>
}
