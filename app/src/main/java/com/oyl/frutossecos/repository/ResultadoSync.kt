package com.oyl.frutossecos.repository

/**
 * Clase sellada que representa los posibles resultados de una operación
 * de sincronización con el backend de Spring Boot.
 *
 * Permite a la capa de presentación distinguir entre:
 * - Datos frescos del servidor.
 * - Datos de la caché local (cuando el servidor no está disponible).
 * - Errores sin datos disponibles.
 */
sealed class ResultadoSync<out T> {

    /**
     * Operación exitosa: datos obtenidos directamente del servidor.
     */
    data class Exito<T>(val datos: T) : ResultadoSync<T>()

    /**
     * El servidor no respondió, pero se encontraron datos en la caché local.
     * Incluye un mensaje explicativo del error de red.
     */
    data class DesdeCacheConError<T>(
        val datos: T,
        val mensajeError: String
    ) : ResultadoSync<T>()

    /**
     * Error total: el servidor no respondió y no hay caché disponible.
     */
    data class Error(val mensajeError: String) : ResultadoSync<Nothing>()
}
