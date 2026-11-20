package com.oyl.frutossecos.model

// Errores por campo en el login. Si queda en null, el campo está ok
data class LoginErrores(
    val identificador: String? = null,
    val clave: String? = null
) {
    val hayErrores: Boolean
        get() = identificador != null || clave != null
}

// Estado del formulario de login que observará la pantalla
data class LoginFormUiState(
    val identificador: String = "",
    val clave: String = "",
    val claveVisible: Boolean = false, // Para el ojito de mostrar/ocultar clave
    val errores: LoginErrores = LoginErrores(),
    val esValido: Boolean = false,
    val cargando: Boolean = false,
    val mensajeErrorGeneral: String? = null
)

// Errores de cada campo al crear una cuenta
data class RegistroErrores(
    val nombre: String? = null,
    val correo: String? = null,
    val clave: String? = null,
    val confirmarClave: String? = null
) {
    val hayErrores: Boolean
        get() = nombre != null || correo != null || clave != null || confirmarClave != null
}

// Estado completo del formulario de registro
data class RegistroFormUiState(
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val confirmarClave: String = "",
    val claveVisible: Boolean = false,
    val confirmarClaveVisible: Boolean = false,
    val errores: RegistroErrores = RegistroErrores(),
    val esValido: Boolean = false,
    val cargando: Boolean = false,
    val mensajeErrorGeneral: String? = null
)
