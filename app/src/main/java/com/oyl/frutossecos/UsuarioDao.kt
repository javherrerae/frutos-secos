package com.oyl.frutossecos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * DAO (Data Access Object) de la tabla `usuarios`.
 * Permite registrar clientes, autenticar y consultar perfiles locales.
 */
@Dao
interface UsuarioDao {

    /** Inserta los usuarios iniciales que no existan previamente. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertarSiNoExisten(usuarios: List<Usuario>)

    /** Registra un nuevo cliente en la base local. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun registrar(usuario: Usuario): Long

    /** Devuelve todos los usuarios registrados ordenados por identificador. */
    @Query("SELECT * FROM usuarios ORDER BY id ASC")
    fun obtenerTodos(): List<Usuario>

    /** Valida credenciales locales según alias o correo y contraseña. */
    @Query("SELECT * FROM usuarios WHERE (alias = :credencial OR correo = :credencial) AND contrasena = :contrasena LIMIT 1")
    fun autenticar(credencial: String, contrasena: String): Usuario?

    /** Busca un usuario por alias o correo. */
    @Query("SELECT * FROM usuarios WHERE alias = :credencial OR correo = :credencial LIMIT 1")
    fun buscarPorAliasOCorreo(credencial: String): Usuario?

    /** Obtiene el perfil de un usuario por su identificador. */
    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    fun obtenerPorId(id: Int): Usuario?

    /** Actualiza los datos de un usuario en su perfil. */
    @Update
    fun actualizar(usuario: Usuario)
}
