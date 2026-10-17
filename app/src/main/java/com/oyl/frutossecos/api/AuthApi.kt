package com.oyl.frutossecos.api

import com.oyl.frutossecos.dto.LoginRequest
import com.oyl.frutossecos.dto.LoginResponse
import com.oyl.frutossecos.dto.RegistroRequest
import com.oyl.frutossecos.dto.UsuarioDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interfaz Retrofit que define los endpoints REST de autenticación.
 * Base URL: {servidor}/api/auth
 */
interface AuthApi {

    /**
     * Autentica al usuario y devuelve un token de sesión simulado.
     * POST /api/auth/login
     */
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    /**
     * Registra un nuevo cliente en el sistema.
     * POST /api/auth/registro
     */
    @POST("api/auth/registro")
    suspend fun registro(
        @Body request: RegistroRequest
    ): Response<UsuarioDto>
}
