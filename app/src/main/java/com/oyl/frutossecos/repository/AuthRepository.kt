package com.oyl.frutossecos.repository

import com.oyl.frutossecos.Sesion
import com.oyl.frutossecos.SesionDao
import com.oyl.frutossecos.Usuario
import com.oyl.frutossecos.UsuarioDao
import com.oyl.frutossecos.dto.LoginRequest
import com.oyl.frutossecos.dto.RegistroRequest
import com.oyl.frutossecos.dto.UsuarioDto
import com.oyl.frutossecos.network.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repositorio que gestiona la autenticación y registro de usuarios
 * contra la API REST de Spring Boot.
 *
 * Cuenta con sincronización bidireccional y fallback a Room (SQLite)
 * para operar fluidamente tanto en línea como en modo sin conexión.
 */
class AuthRepository(
    private val usuarioDao: UsuarioDao,
    private val sesionDao: SesionDao
) {

    private val api get() = RetrofitClient.authApi

    /**
     * Inicializa la cuenta de Administrador de prueba en SQLite en segundo plano si no existe.
     */
    suspend fun inicializarAdminSiEsNecesario() = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val adminExiste = usuarioDao.buscarPorAliasOCorreo("admin@admin.cl")
        if (adminExiste == null) {
            usuarioDao.insertarSiNoExisten(
                listOf(
                    Usuario(
                        id = 1,
                        nombre = "Administrador",
                        alias = "admin",
                        contrasena = "admin123",
                        correo = "admin@admin.cl",
                        telefono = "+56 9 1234 1234",
                        direccion = "Santiago",
                        rol = "administrador",
                        activo = 1
                    )
                )
            )
        }
    }

    /**
     * Autentica al usuario contra el backend.
     * Si no hay conexión o el servidor está apagado, valida contra Room local.
     */
    suspend fun login(alias: String, contrasena: String): ResultadoSync<Usuario> {
        inicializarAdminSiEsNecesario()
        return try {
            val request = LoginRequest(alias = alias, contrasena = contrasena)
            val respuesta = api.login(request)
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val loginResponse = respuesta.body()!!
                // Configurar token para futuras peticiones
                RetrofitClient.establecerToken(loginResponse.token)
                // Guardar usuario en caché local
                val usuarioLocal = loginResponse.usuario.toEntity().copy(contrasena = contrasena)
                usuarioDao.insertarSiNoExisten(listOf(usuarioLocal))
                // Crear sesión local
                val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val sesion = Sesion(
                    id = 1,
                    idUsuario = usuarioLocal.id,
                    token = loginResponse.token,
                    fechaInicio = formatoFecha.format(Date())
                )
                sesionDao.guardarSesion(sesion)
                ResultadoSync.Exito(usuarioLocal)
            } else {
                // Si el servidor respondió pero rechazó, intentar validar si existe localmente
                val local = usuarioDao.autenticar(alias, contrasena)
                if (local != null) {
                    ResultadoSync.DesdeCacheConError(
                        datos = local,
                        mensajeError = "Autenticado con base de datos local."
                    )
                } else {
                    ResultadoSync.Error("Credenciales inválidas: usuario o contraseña incorrectos.")
                }
            }
        } catch (e: Exception) {
            // Intento de login offline con base de datos Room
            val usuarioLocal = usuarioDao.autenticar(alias, contrasena)
            if (usuarioLocal != null) {
                ResultadoSync.DesdeCacheConError(
                    datos = usuarioLocal,
                    mensajeError = "Sesión offline: sin conexión al servidor."
                )
            } else {
                ResultadoSync.Error("No se pudo conectar al servidor ni existe el usuario localmente: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Registra un nuevo cliente en el backend de Spring Boot.
     * Si el servidor no responde, lo guarda en Room (SQLite) para que el usuario
     * pueda comenzar a usar la aplicación de inmediato.
     */
    suspend fun registro(request: RegistroRequest): ResultadoSync<Usuario> {
        inicializarAdminSiEsNecesario()
        return try {
            val respuesta = api.registro(request)
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val usuarioLocal = respuesta.body()!!.toEntity().copy(contrasena = request.contrasena)
                usuarioDao.insertarSiNoExisten(listOf(usuarioLocal))
                ResultadoSync.Exito(usuarioLocal)
            } else {
                ResultadoSync.Error("No se pudo registrar en el servidor: ${respuesta.code()} ${respuesta.message()}")
            }
        } catch (e: Exception) {
            // Fallback sin conexión: guardar en la base SQLite local
            val idNuevo = ((usuarioDao.obtenerTodos().maxOfOrNull { it.id } ?: 1) + 1)
            val usuarioLocal = Usuario(
                id = idNuevo,
                nombre = request.nombre,
                alias = request.alias,
                contrasena = request.contrasena,
                correo = request.correo,
                telefono = request.telefono.ifBlank { "+56 9 1234 5678" },
                direccion = request.direccion.ifBlank { "Santiago" },
                rol = "cliente",
                activo = 1
            )
            usuarioDao.insertarSiNoExisten(listOf(usuarioLocal))
            ResultadoSync.DesdeCacheConError(
                datos = usuarioLocal,
                mensajeError = "Registrado localmente (servidor no disponible)."
            )
        }
    }

    /**
     * Cierra la sesión activa, limpia el token y elimina los registros de sesión local.
     */
    fun cerrarSesion() {
        RetrofitClient.limpiarToken()
        sesionDao.cerrarSesiones()
    }
}

/**
 * Convierte un UsuarioDto (formato API) a Usuario (entidad Room).
 */
fun UsuarioDto.toEntity(): Usuario = Usuario(
    id = id,
    nombre = nombre,
    alias = alias,
    contrasena = "",
    correo = correo,
    telefono = telefono,
    direccion = direccion,
    rol = rol,
    activo = if (activo) 1 else 0
)
