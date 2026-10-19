package com.oyl.frutossecos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/** DAO del catálogo local. Mantiene intactas las consultas usadas por los compañeros. */
@Dao
interface ProductoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertarSiNoExisten(productos: List<Producto>)

    @Query("SELECT * FROM productos WHERE activo = 1 ORDER BY nombre ASC")
    fun obtenerCatalogo(): List<Producto>

    @Query("SELECT * FROM productos WHERE categoria = :categoria AND activo = 1 ORDER BY nombre ASC")
    fun obtenerPorCategoria(categoria: String): List<Producto>

    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    fun obtenerPorId(id: Int): Producto?

    @Query("SELECT * FROM productos WHERE stock <= :limiteMinimo")
    fun obtenerProductosStockBajo(limiteMinimo: Int): List<Producto>

    @Update
    fun actualizar(producto: Producto)

    @Query("UPDATE productos SET stock = MAX(stock - :cantidad, 0) WHERE id = :idProducto")
    fun rebajarStock(idProducto: Int, cantidad: Int)

    // --- Operaciones locales para las pantallas administrativas 11 y 12 ---

    /** Admin ve activos e inactivos para poder reactivarlos. */
    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    fun obtenerTodos(): List<Producto>

    @Query("SELECT COUNT(*) FROM productos")
    fun contarTodos(): Int

    /** ID para la tabla actual, cuya PK no es autogenerada. */
    @Query("SELECT COALESCE(MAX(id), 0) + 1 FROM productos")
    fun siguienteId(): Int

    /** ABORT evita sobreescribir accidentalmente un producto existente. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertarProducto(producto: Producto): Long

    /** No toca el stock ni la imagen durante una edición descriptiva. */
    @Query("""
        UPDATE productos SET
            nombre = :nombre,
            categoria = :categoria,
            descripcion = :descripcion,
            precio = :precio,
            presentacion = :presentacion
        WHERE id = :id
    """)
    fun editarDatos(
        id: Int,
        nombre: String,
        categoria: String,
        descripcion: String,
        precio: Double,
        presentacion: String
    ): Int

    @Query("UPDATE productos SET activo = :activo WHERE id = :id")
    fun cambiarActivo(id: Int, activo: Int): Int

    /** Incremento atómico, evitando desbordamiento del entero de stock. */
    @Query("""
        UPDATE productos SET stock = stock + :cantidad
        WHERE id = :id AND activo = 1 AND stock >= 0 AND stock <= 2147483647 - :cantidad
    """)
    fun registrarEntrada(id: Int, cantidad: Int): Int

    /** Descuento atómico: nunca deja existencias negativas. */
    @Query("""
        UPDATE productos SET stock = stock - :cantidad
        WHERE id = :id AND activo = 1 AND stock >= :cantidad
    """)
    fun registrarSalida(id: Int, cantidad: Int): Int
}
