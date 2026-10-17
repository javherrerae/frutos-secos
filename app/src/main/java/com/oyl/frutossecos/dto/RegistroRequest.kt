package com.oyl.frutossecos.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO de solicitud para registrar un nuevo cliente en el backend.
 */
data class RegistroRequest(
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("alias")
    val alias: String,
    @SerializedName("contrasena")
    val contrasena: String,
    @SerializedName("correo")
    val correo: String,
    @SerializedName("telefono")
    val telefono: String,
    @SerializedName("direccion")
    val direccion: String
)
