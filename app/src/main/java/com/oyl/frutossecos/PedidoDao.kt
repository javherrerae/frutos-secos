package com.oyl.frutossecos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO de la tabla `pedidos`.
 * Permite almacenar y consultar el historial local de pedidos.
 */
@Dao
interface PedidoDao {

    /** Inserta pedidos que no existan previamente. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertarSiNoExisten(pedidos: List<Pedido>)

    /** Guarda o actualiza un pedido en el registro local. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertar(pedido: Pedido): Long

    /** Obtiene el historial de pedidos de un cliente ordenados del más reciente al más antiguo. */
    @Query("SELECT * FROM pedidos WHERE idUsuario = :idUsuario ORDER BY id DESC")
    fun obtenerPorUsuario(idUsuario: Int): List<Pedido>

    /** Obtiene todos los pedidos registrados en el sistema (vista de Administrador). */
    @Query("SELECT * FROM pedidos ORDER BY id DESC")
    fun obtenerTodos(): List<Pedido>

    /** Actualiza el estado de una orden (Pendiente de Validación, Confirmado, Preparado, Entregado). */
    @Query("UPDATE pedidos SET estado = :nuevoEstado WHERE id = :id")
    fun actualizarEstado(id: Int, nuevoEstado: String)
}
