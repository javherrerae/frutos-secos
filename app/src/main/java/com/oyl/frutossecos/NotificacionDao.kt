package com.oyl.frutossecos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO de la tabla `notificaciones`.
 * Gestiona el centro de avisos del usuario y administrador en el dispositivo.
 */
@Dao
interface NotificacionDao {

    /** Inserta notificaciones que no existan. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertarSiNoExisten(notificaciones: List<Notificacion>)

    /** Devuelve las notificaciones recibidas por el usuario ordenadas cronológicamente. */
    @Query("SELECT * FROM notificaciones WHERE idUsuario = :idUsuario ORDER BY id DESC")
    fun obtenerPorUsuario(idUsuario: Int): List<Notificacion>

    /** Marca una notificación como leída (1). */
    @Query("UPDATE notificaciones SET leida = 1 WHERE id = :id")
    fun marcarComoLeida(id: Int)
}
