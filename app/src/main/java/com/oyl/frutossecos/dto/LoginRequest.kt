package com.oyl.frutossecos.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO de solicitud de inicio de sesión contra el backend.
 */
data class LoginRequest(
    @SerializedName("alias")
    val alias: String,
    @SerializedName("contrasena")
    val contrasena: String
)
