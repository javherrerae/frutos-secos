package com.oyl.frutossecos.repository

import com.oyl.frutossecos.Sesion
import com.oyl.frutossecos.SesionDao
import com.oyl.frutossecos.Usuario
import com.oyl.frutossecos.UsuarioDao
import com.oyl.frutossecos.dto.*
import com.oyl.frutossecos.api.AuthApi
import com.oyl.frutossecos.network.RetrofitClient

/** El servidor autentica y registra; Room conserva únicamente la caché del perfil. */
class AuthRepository(private val usuarioDao: UsuarioDao, private val sesionDao: SesionDao,
    private val apiInyectada: AuthApi? = null) {
    private val api get() = apiInyectada ?: RetrofitClient.authApi

    suspend fun login(alias: String, contrasena: String): ResultadoSync<Usuario> {
        return try {
            val respuesta = api.login(LoginRequest(alias, contrasena))
            val datos = respuesta.body()
            if (respuesta.isSuccessful && datos != null) {
                RetrofitClient.establecerToken(datos.token)
                val usuario = datos.usuario.toEntity()
                usuarioDao.guardar(usuario)
                sesionDao.guardarSesion(Sesion(1, usuario.id, datos.token, java.util.Date().toString()))
                ResultadoSync.Exito(usuario)
            } else {
                RetrofitClient.limpiarToken()
                sesionDao.cerrarSesiones()
                ResultadoSync.Error(respuesta.mensajeError())
            }
        } catch (e: java.io.IOException) {
            ResultadoSync.Error("No hay conexión con el servidor. Revisa la dirección del backend.")
        }
    }

    suspend fun registro(request: RegistroRequest): ResultadoSync<Usuario> {
        return try {
            val respuesta = api.registro(request)
            val datos = respuesta.body()
            if (respuesta.isSuccessful && datos != null) {
                val usuario = datos.toEntity()
                usuarioDao.guardar(usuario)
                ResultadoSync.Exito(usuario)
            } else ResultadoSync.Error(respuesta.mensajeError())
        } catch (e: java.io.IOException) {
            ResultadoSync.Error("No se pudo registrar: servidor no disponible.")
        }
    }

    suspend fun actualizarPerfil(request: PerfilRequest): ResultadoSync<Usuario> {
        return try {
            val respuesta = api.actualizarPerfil(request)
            val datos = respuesta.body()
            if (respuesta.isSuccessful && datos != null) {
                val usuario = datos.toEntity()
                usuarioDao.guardar(usuario)
                ResultadoSync.Exito(usuario)
            } else ResultadoSync.Error(respuesta.mensajeError())
        } catch (e: java.io.IOException) {
            ResultadoSync.Error("No se guardaron los cambios: servidor no disponible.")
        }
    }

    fun restaurarSesion(): Usuario? {
        val sesion = sesionDao.obtenerSesionActiva() ?: return null
        if (sesion.token.isBlank()) return null
        RetrofitClient.establecerToken(sesion.token)
        return usuarioDao.obtenerPorId(sesion.idUsuario)
    }

    fun cerrarSesion() {
        RetrofitClient.limpiarToken()
        sesionDao.cerrarSesiones()
    }
}

fun UsuarioDto.toEntity(): Usuario = Usuario(
    id, nombre, alias, "", correo, telefono, direccion, rol, if (activo) 1 else 0
)
