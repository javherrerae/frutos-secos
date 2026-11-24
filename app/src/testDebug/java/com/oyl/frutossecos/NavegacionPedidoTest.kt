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
class NavegacionPedidoTest {
    @get:Rule val compose = createComposeRule()
    @Test fun confirmacionPermiteVerDetalleYVolverAlHistorial() {
        compose.setContent { AppNavigation(pantallaInicial = "confirmacion") }
        compose.onNodeWithText("Ver mi pedido").performScrollTo().performClick()
        compose.onNodeWithText("Este pedido ya no está disponible en la sesión.").assertExists()
        compose.onNodeWithText("Volver a mis pedidos").performClick()
        compose.onNodeWithText("Todavía no tienes pedidos.").assertExists()
        compose.onNodeWithText("Explorar catálogo").performClick()
        compose.onNodeWithText("Buscar productos").assertExists()
    }
}
