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
class NavegacionDesconocidaTest {
    @get:Rule val compose = createComposeRule()
    @Test fun pantallaDesconocidaPermiteVolverAlInicio() {
        compose.setContent { AppNavigation(pantallaInicial = "desconocida") }
        compose.onNodeWithText("Pantalla no encontrada").assertExists()
        compose.onNodeWithText("Esta pantalla está en desarrollo.").assertExists()
        compose.onNodeWithText("Volver").performClick()
        compose.onNodeWithText("¡Naturalmente buenos!").assertExists()
    }
}
