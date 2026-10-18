package com.oyl.frutossecos.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oyl.frutossecos.AppDatabase
import com.oyl.frutossecos.Usuario
import com.oyl.frutossecos.dto.RegistroRequest
import com.oyl.frutossecos.model.LoginFormUiState
import com.oyl.frutossecos.model.LoginErrores
import com.oyl.frutossecos.model.RegistroFormUiState
import com.oyl.frutossecos.model.RegistroErrores
import com.oyl.frutossecos.repository.AuthRepository
import com.oyl.frutossecos.repository.ResultadoSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // Conexión con Room usando la base de datos de la app
    private val db = AppDatabase.crear(application)
    val repository = AuthRepository(db.usuarioDao(), db.sesionDao())

    // Estado para el login
    private val _loginState = MutableStateFlow(LoginFormUiState())
    val loginState: StateFlow<LoginFormUiState> = _loginState.asStateFlow()

    // Estado para el registro
    private val _registroState = MutableStateFlow(RegistroFormUiState())
    val registroState: StateFlow<RegistroFormUiState> = _registroState.asStateFlow()

    // Usuario que tiene la sesión iniciada actualmente
    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    init {
        // Al arrancar, creamos el admin de prueba en SQLite si no existe y leemos la sesión
        viewModelScope.launch(Dispatchers.IO) {
            repository.inicializarAdminSiEsNecesario()
            val sesion = db.sesionDao().obtenerSesionActiva()
            val sesionActiva = sesion?.let { db.usuarioDao().obtenerPorId(it.idUsuario) }
            _usuarioActual.value = sesionActiva
        }
    }

    // --- Validaciones y acciones de Login ---

    fun onLoginIdentificadorChange(valor: String) {
        _loginState.update { estado ->
            val error = if (valor.isNotBlank() && valor.trim().isEmpty()) "Ingresa tu correo o alias" else null
            val nuevosErrores = estado.errores.copy(identificador = error)
            estado.copy(
                identificador = valor,
                errores = nuevosErrores,
                mensajeErrorGeneral = null,
                esValido = valor.trim().isNotEmpty() && estado.clave.length >= 6
            )
        }
    }

    fun onLoginClaveChange(valor: String) {
        _loginState.update { estado ->
            val error = if (valor.isNotEmpty() && valor.length < 6) "La contraseña debe tener al menos 6 caracteres" else null
            val nuevosErrores = estado.errores.copy(clave = error)
            estado.copy(
                clave = valor,
                errores = nuevosErrores,
                mensajeErrorGeneral = null,
                esValido = estado.identificador.trim().isNotEmpty() && valor.length >= 6
            )
        }
    }

    fun alternarVisibilidadClaveLogin() {
        _loginState.update { it.copy(claveVisible = !it.claveVisible) }
    }

    fun iniciarSesion(onExito: (Usuario) -> Unit) {
        val estado = _loginState.value
        if (!estado.esValido || estado.cargando) return

        _loginState.update { it.copy(cargando = true, mensajeErrorGeneral = null) }

        viewModelScope.launch(Dispatchers.IO) {
            // Pausa breve para apreciar el indicador de carga en pantalla
            delay(800)

            val resultado = repository.login(estado.identificador.trim(), estado.clave.trim())
            _loginState.update { it.copy(cargando = false) }

            when (resultado) {
                is ResultadoSync.Exito -> {
                    _usuarioActual.value = resultado.datos
                    _loginState.update { LoginFormUiState() }
                    launch(Dispatchers.Main) { onExito(resultado.datos) }
                }
                is ResultadoSync.DesdeCacheConError -> {
                    _usuarioActual.value = resultado.datos
                    _loginState.update { LoginFormUiState() }
                    launch(Dispatchers.Main) { onExito(resultado.datos) }
                }
                is ResultadoSync.Error -> {
                    _loginState.update { it.copy(mensajeErrorGeneral = resultado.mensajeError) }
                }
            }
        }
    }

    // --- Validaciones y acciones de Registro ---

    fun onRegistroNombreChange(valor: String) {
        _registroState.update { estado ->
            val error = if (valor.isNotBlank() && valor.trim().length < 2) "El nombre debe tener al menos 2 letras" else null
            val nuevosErrores = estado.errores.copy(nombre = error)
            val valido = validarFormularioRegistro(valor, estado.correo, estado.clave, estado.confirmarClave)
            estado.copy(nombre = valor, errores = nuevosErrores, esValido = valido, mensajeErrorGeneral = null)
        }
    }

    fun onRegistroCorreoChange(valor: String) {
        _registroState.update { estado ->
            val formatoCorrecto = Patterns.EMAIL_ADDRESS.matcher(valor.trim()).matches()
            val error = if (valor.isNotBlank() && !formatoCorrecto) "Ingresa un correo electrónico válido" else null
            val nuevosErrores = estado.errores.copy(correo = error)
            val valido = validarFormularioRegistro(estado.nombre, valor, estado.clave, estado.confirmarClave)
            estado.copy(correo = valor, errores = nuevosErrores, esValido = valido, mensajeErrorGeneral = null)
        }
    }

    fun onRegistroClaveChange(valor: String) {
        _registroState.update { estado ->
            val error = if (valor.isNotEmpty() && valor.length < 6) "Mínimo 6 caracteres" else null
            val errorConfirmar = if (estado.confirmarClave.isNotEmpty() && estado.confirmarClave != valor) "Las contraseñas no coinciden" else null
            val nuevosErrores = estado.errores.copy(clave = error, confirmarClave = errorConfirmar)
            val valido = validarFormularioRegistro(estado.nombre, estado.correo, valor, estado.confirmarClave)
            estado.copy(clave = valor, errores = nuevosErrores, esValido = valido, mensajeErrorGeneral = null)
        }
    }

    fun onRegistroConfirmarClaveChange(valor: String) {
        _registroState.update { estado ->
            val error = if (valor.isNotEmpty() && valor != estado.clave) "Las contraseñas no coinciden" else null
            val nuevosErrores = estado.errores.copy(confirmarClave = error)
            val valido = validarFormularioRegistro(estado.nombre, estado.correo, estado.clave, valor)
            estado.copy(confirmarClave = valor, errores = nuevosErrores, esValido = valido, mensajeErrorGeneral = null)
        }
    }

    fun alternarVisibilidadClaveRegistro() {
        _registroState.update { it.copy(claveVisible = !it.claveVisible) }
    }

    fun alternarVisibilidadConfirmarClave() {
        _registroState.update { it.copy(confirmarClaveVisible = !it.confirmarClaveVisible) }
    }

    private fun validarFormularioRegistro(nom: String, cor: String, cla: String, conf: String): Boolean {
        val correoValido = Patterns.EMAIL_ADDRESS.matcher(cor.trim()).matches()
        return nom.trim().length >= 2 && correoValido && cla.length >= 6 && cla == conf
    }

    fun registrarUsuario(onExito: (Usuario) -> Unit) {
        val estado = _registroState.value
        if (!estado.esValido || estado.cargando) return

        _registroState.update { it.copy(cargando = true, mensajeErrorGeneral = null) }

        viewModelScope.launch(Dispatchers.IO) {
            // Pausa breve para apreciar el indicador de carga en pantalla
            delay(800)

            val aliasGenerado = estado.correo.substringBefore("@").lowercase()
            val request = RegistroRequest(
                nombre = estado.nombre.trim(),
                alias = aliasGenerado,
                contrasena = estado.clave.trim(),
                correo = estado.correo.trim(),
                telefono = "+56 9 0000 0000",
                direccion = "Santiago"
            )
            val resultado = repository.registro(request)
            _registroState.update { it.copy(cargando = false) }

            when (resultado) {
                is ResultadoSync.Exito -> {
                    _usuarioActual.value = resultado.datos
                    _registroState.update { RegistroFormUiState() }
                    launch(Dispatchers.Main) { onExito(resultado.datos) }
                }
                is ResultadoSync.DesdeCacheConError -> {
                    _usuarioActual.value = resultado.datos
                    _registroState.update { RegistroFormUiState() }
                    launch(Dispatchers.Main) { onExito(resultado.datos) }
                }
                is ResultadoSync.Error -> {
                    _registroState.update { it.copy(mensajeErrorGeneral = resultado.mensajeError) }
                }
            }
        }
    }

    fun cerrarSesion(onCompletado: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.cerrarSesion()
            _usuarioActual.value = null
            launch(Dispatchers.Main) { onCompletado() }
        }
    }
}
