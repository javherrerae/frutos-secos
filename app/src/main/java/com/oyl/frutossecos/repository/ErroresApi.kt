package com.oyl.frutossecos.repository

import org.json.JSONObject
import retrofit2.Response

fun Response<*>.mensajeError(): String {
    val mensaje = runCatching {
        JSONObject(errorBody()?.string().orEmpty()).optString("mensaje").takeIf { it.isNotBlank() }
    }.getOrNull()
    return mensaje ?: when (code()) {
        401 -> "Sesión inválida o vencida. Inicia sesión nuevamente."
        403 -> "Tu usuario no tiene permiso para realizar esta operación."
        409 -> "Los datos cambiaron o no hay stock suficiente. Actualiza e intenta de nuevo."
        else -> "No se pudo completar la operación (HTTP ${code()})."
    }
}
