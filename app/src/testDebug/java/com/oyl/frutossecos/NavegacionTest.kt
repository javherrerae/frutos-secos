package com.oyl.frutossecos

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.oyl.frutossecos.navegation.AppNavigation
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w480dp-h1000dp")
class NavegacionTest {
    @get:Rule val compose = createComposeRule()

    @Test fun navegaDeInicioACatalogoCarritoPedidosYPerfil() {
        compose.setContent { AppNavigation(pantallaInicial = "dashboard") }
        compose.onNodeWithText("Explorar productos").performClick()
        compose.onNodeWithText("Buscar productos").assertExists()
        compose.onNodeWithText("Escanear QR").performClick()
        compose.onNodeWithText("Escanear producto").assertExists()
        compose.onNodeWithText("← Volver").performClick()
        compose.onNodeWithText("Carrito").performClick()
        compose.onNodeWithText("Tu carrito está vacío").assertExists()
        compose.onNodeWithText("Pedidos").performClick()
        compose.onNodeWithText("Todavía no tienes pedidos.").assertExists()
        compose.onNodeWithText("Perfil").performClick()
        compose.onNodeWithText("Mi perfil").assertExists()
    }
}
