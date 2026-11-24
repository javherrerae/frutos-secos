package com.oyl.frutossecos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.oyl.frutossecos.ui.screens.ConfirmacionScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ConfirmacionScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun muestraPedidoYAbreDetalle() {
        var aperturas = 0
        compose.setContent {
            ConfirmacionScreen(total = 12345, codigoPedido = "PED-123", mensajeServidor = "Confirmado por servidor",
                onVolverInicio = {}, onVerPedido = { aperturas++ })
        }
        compose.onNodeWithText("PED-123").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Confirmado por servidor").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Ver mi pedido").performScrollTo().performClick()
        assertEquals(1, aperturas)
    }
}
