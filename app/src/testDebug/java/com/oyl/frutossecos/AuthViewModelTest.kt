package com.oyl.frutossecos

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.oyl.frutossecos.viewmodel.AuthViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AuthViewModelTest {
    @Test fun loginSoloEsValidoConIdentificadorYClaveSuficientes() {
        val vm = AuthViewModel(ApplicationProvider.getApplicationContext<Application>())
        vm.onLoginIdentificadorChange("  ana  ")
        vm.onLoginClaveChange("123")
        assertFalse(vm.loginState.value.esValido)
        assertNotNull(vm.loginState.value.errores.clave)
        vm.onLoginClaveChange("123456")
        assertTrue(vm.loginState.value.esValido)
        vm.alternarVisibilidadClaveLogin()
        assertTrue(vm.loginState.value.claveVisible)
        vm.onLoginIdentificadorChange("   ")
        assertFalse(vm.loginState.value.esValido)
    }

    @Test fun registroCompruebaCorreoClaveYConfirmacion() {
        val vm = AuthViewModel(ApplicationProvider.getApplicationContext<Application>())
        vm.onRegistroNombreChange("A")
        vm.onRegistroCorreoChange("correo inválido")
        vm.onRegistroClaveChange("123")
        vm.onRegistroConfirmarClaveChange("otra")
        assertFalse(vm.registroState.value.esValido)
        assertNotNull(vm.registroState.value.errores.nombre)
        assertNotNull(vm.registroState.value.errores.correo)
        assertNotNull(vm.registroState.value.errores.confirmarClave)
        vm.onRegistroNombreChange("Ana")
        vm.onRegistroCorreoChange("ana@example.com")
        vm.onRegistroClaveChange("123456")
        vm.onRegistroConfirmarClaveChange("123456")
        assertTrue(vm.registroState.value.esValido)
        vm.alternarVisibilidadClaveRegistro()
        vm.alternarVisibilidadConfirmarClave()
        assertTrue(vm.registroState.value.claveVisible)
        assertTrue(vm.registroState.value.confirmarClaveVisible)
    }
}
