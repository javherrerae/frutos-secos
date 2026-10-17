package com.oyl.frutossecos.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO que representa un usuario tal como lo envía la API REST.
 */
data class UsuarioDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("alias")
    val alias: String,
    @SerializedName("correo")
    val correo: String,
    @SerializedName("telefono")
    val telefono: String,
    @SerializedName("direccion")
    val direccion: String,
    @SerializedName("rol")
    val rol: String,
    @SerializedName("activo")
    val activo: Boolean
)
