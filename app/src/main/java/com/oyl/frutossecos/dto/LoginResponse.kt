package com.oyl.frutossecos.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO de respuesta del endpoint de autenticación.
 * Devuelve un token simulado y los datos básicos del usuario autenticado.
 */
data class LoginResponse(
    @SerializedName("token")
    val token: String,
    @SerializedName("usuario")
    val usuario: UsuarioDto
)
