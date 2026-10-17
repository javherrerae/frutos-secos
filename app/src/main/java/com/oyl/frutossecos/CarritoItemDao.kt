package com.oyl.frutossecos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO de la tabla `carrito_items`.
 * Gestiona los artículos seleccionados por cada cliente en su carrito local.
 */
@Dao
interface CarritoItemDao {

    /** Agrega o actualiza un ítem en el carrito. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun agregarOActualizar(item: CarritoItem)

    /** Devuelve los ítems del carrito pertenecientes a un usuario en específico. */
    @Query("SELECT * FROM carrito_items WHERE idUsuario = :idUsuario")
    fun obtenerPorUsuario(idUsuario: Int): List<CarritoItem>

    /** Elimina un ítem específico del carrito. */
    @Query("DELETE FROM carrito_items WHERE id = :id")
    fun eliminarItem(id: Int)

    /** Elimina un producto específico del carrito de un cliente. */
    @Query("DELETE FROM carrito_items WHERE idUsuario = :idUsuario AND idProducto = :idProducto")
    fun eliminarPorProducto(idUsuario: Int, idProducto: Int)

    /** Busca si un producto ya está en el carrito de un cliente. */
    @Query("SELECT * FROM carrito_items WHERE idUsuario = :idUsuario AND idProducto = :idProducto LIMIT 1")
    fun obtenerItem(idUsuario: Int, idProducto: Int): CarritoItem?

    /** Vacía por completo el carrito de un cliente. */
    @Query("DELETE FROM carrito_items WHERE idUsuario = :idUsuario")
    fun vaciarCarrito(idUsuario: Int)
}
