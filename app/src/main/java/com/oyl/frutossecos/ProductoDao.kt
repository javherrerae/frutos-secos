package com.oyl.frutossecos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * DAO de la tabla `productos`.
 * Administra el catálogo de frutos secos, semillas y deshidratados en caché local.
 */
@Dao
interface ProductoDao {

    /** Inserta los productos que no existan previamente. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertarSiNoExisten(productos: List<Producto>)

    /** Obtiene el catálogo completo de productos activos. */
    @Query("SELECT * FROM productos WHERE activo = 1 ORDER BY nombre ASC")
    fun obtenerCatalogo(): List<Producto>

    /** Filtra los productos activos por categoría. */
    @Query("SELECT * FROM productos WHERE categoria = :categoria AND activo = 1 ORDER BY nombre ASC")
    fun obtenerPorCategoria(categoria: String): List<Producto>

    /** Obtiene un producto por su identificador. */
    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    fun obtenerPorId(id: Int): Producto?

    /** Devuelve productos cuya existencia es menor o igual al límite indicado. */
    @Query("SELECT * FROM productos WHERE stock <= :limiteMinimo")
    fun obtenerProductosStockBajo(limiteMinimo: Int): List<Producto>

    /** Actualiza la información o existencia en la copia local del producto. */
    @Update
    fun actualizar(producto: Producto)

    /** Rebaja el stock de un producto tras confirmar un pedido. No permite stock negativo. */
    @Query("UPDATE productos SET stock = MAX(stock - :cantidad, 0) WHERE id = :idProducto")
    fun rebajarStock(idProducto: Int, cantidad: Int)
}
