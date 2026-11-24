package com.oyl.frutossecos

import com.oyl.frutossecos.api.AuthApi
import com.oyl.frutossecos.dto.*
import com.oyl.frutossecos.repository.AuthRepository
import com.oyl.frutossecos.repository.ResultadoSync
import com.oyl.frutossecos.network.RetrofitClient
import java.io.IOException
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AuthRepositoryTest {
    private val dto = UsuarioDto(7, "Ana", "ana", "ana@example.com", "123", "Santiago", "cliente", true)
    private var usuario: Usuario? = null
    private var sesion: Sesion? = null
    private var cerrar = 0
    private var fallo = false
    private var codigo = 200
    private val usuarios = Proxy.newProxyInstance(UsuarioDao::class.java.classLoader, arrayOf(UsuarioDao::class.java)) { _, metodo, args ->
        when (metodo.name) {
            "guardar" -> { usuario = args[0] as Usuario; null }
            "obtenerPorId" -> usuario
            else -> error(metodo.name)
        }
    } as UsuarioDao
    private val sesiones = Proxy.newProxyInstance(SesionDao::class.java.classLoader, arrayOf(SesionDao::class.java)) { _, metodo, args ->
        when (metodo.name) {
            "guardarSesion" -> { sesion = args[0] as Sesion; null }
            "obtenerSesionActiva" -> sesion
            "cerrarSesiones" -> { cerrar++; sesion = null; null }
            else -> error(metodo.name)
        }
    } as SesionDao
    private val api = object : AuthApi {
        private fun <T> respuesta(valor: T): Response<T> {
            if (fallo) throw IOException("sin red")
            return if (codigo == 200) Response.success(valor) else Response.error(codigo, "{}".toResponseBody())
        }
        override suspend fun login(request: LoginRequest) = respuesta(LoginResponse("token", dto))
        override suspend fun registro(request: RegistroRequest) = respuesta(dto)
        override suspend fun actualizarPerfil(request: PerfilRequest) = respuesta(dto)
    }
    private val repo = AuthRepository(usuarios, sesiones, api)
    @After fun limpiar() { RetrofitClient.limpiarToken() }

    @Test fun loginGuardaPerfilYSesionYLaRestaura() = runBlocking {
        assertTrue(repo.login("ana", "clave") is ResultadoSync.Exito)
        assertEquals("ana", usuario!!.alias)
        assertEquals("token", sesion!!.token)
        assertEquals(usuario, repo.restaurarSesion())
        repo.cerrarSesion()
        assertNull(repo.restaurarSesion())
        assertEquals(1, cerrar)
    }

    @Test fun loginRechazadoCierraSesionYErrorDeRedNoLaBorra() = runBlocking {
        repo.login("ana", "clave")
        fallo = true
        assertTrue(repo.login("ana", "clave") is ResultadoSync.Error)
        assertNotNull(sesion)
        fallo = false
        codigo = 401
        assertTrue(repo.login("ana", "clave") is ResultadoSync.Error)
        assertNull(sesion)
        assertEquals(1, cerrar)
    }

    @Test fun registroYPerfilGuardanSoloRespuestaExitosa() = runBlocking {
        val registro = RegistroRequest("Ana", "ana", "123456", "ana@example.com", "123", "Santiago")
        val perfil = PerfilRequest("Ana", "ana@example.com", "123")
        assertTrue(repo.registro(registro) is ResultadoSync.Exito)
        assertTrue(repo.actualizarPerfil(perfil) is ResultadoSync.Exito)
        assertEquals("Ana", usuario!!.nombre)
        codigo = 403
        assertTrue(repo.registro(registro) is ResultadoSync.Error)
        assertTrue(repo.actualizarPerfil(perfil) is ResultadoSync.Error)
        fallo = true
        assertTrue(repo.registro(registro) is ResultadoSync.Error)
        assertTrue(repo.actualizarPerfil(perfil) is ResultadoSync.Error)
    }
}
