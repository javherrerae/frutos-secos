package com.oyl.frutossecos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO de la tabla `puntos_retiro`.
 * Proporciona las ubicaciones y horarios de la tienda física y puntos de retiro.
 */
@Dao
interface PuntoRetiroDao {

    /** Inserta los puntos de retiro que no existan previamente. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertarSiNoExisten(puntos: List<PuntoRetiro>)

    /** Obtiene todos los puntos de retiro disponibles para el mapa. */
    @Query("SELECT * FROM puntos_retiro ORDER BY id ASC")
    fun obtenerTodos(): List<PuntoRetiro>
}
