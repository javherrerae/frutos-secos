package com.oyl.frutossecos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO de la tabla `pedido_items`.
 * Almacena el detalle de los artículos que componen un pedido.
 */
@Dao
interface PedidoItemDao {

    /** Inserta los ítems de un pedido que no existan. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertarSiNoExisten(items: List<PedidoItem>)

    /** Obtiene todos los artículos pertenecientes a una orden específica. */
    @Query("SELECT * FROM pedido_items WHERE idPedido = :idPedido")
    fun obtenerPorPedido(idPedido: Int): List<PedidoItem>
}
