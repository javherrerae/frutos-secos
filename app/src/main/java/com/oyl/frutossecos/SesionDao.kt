package com.oyl.frutossecos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO de la tabla `sesiones`.
 * Conserva la sesión del usuario activo en el dispositivo.
 */
@Dao
interface SesionDao {

    /** Guarda o reemplaza la sesión activa actual. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarSesion(sesion: Sesion)

    /** Devuelve la última sesión registrada en el dispositivo. */
    @Query("SELECT * FROM sesiones ORDER BY id DESC LIMIT 1")
    fun obtenerSesionActiva(): Sesion?

    /** Elimina todas las sesiones activas al cerrar sesión. */
    @Query("DELETE FROM sesiones")
    fun cerrarSesiones()
}
