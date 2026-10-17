package com.oyl.frutossecos.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO que representa un producto tal como lo envía/recibe la API REST de Spring Boot.
 * Los campos coinciden con la entidad JPA del backend.
 * Se usa @SerializedName para mapear nombres JSON con propiedades Kotlin.
 */
data class ProductoDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("categoria")
    val categoria: String,
    @SerializedName("descripcion")
    val descripcion: String,
    @SerializedName("precio")
    val precio: Double,
    @SerializedName("presentacion")
    val presentacion: String,
    @SerializedName("stock")
    val stock: Int,
    @SerializedName("rutaImagen")
    val rutaImagen: String,
    @SerializedName("activo")
    val activo: Boolean
)
