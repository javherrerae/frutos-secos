package com.oyl.frutossecos

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.oyl.frutossecos.ui.components.BottomNavigationBar
import com.oyl.frutossecos.ui.theme.OYL_Frutos_SecosTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TemaYBarraTest {
    @get:Rule val compose = createComposeRule()
    @Test fun barraNavegaYNoRepiteDestinoSeleccionado() {
        var destinos = mutableListOf<String>()
        compose.setContent { BottomNavigationBar("detalle") { destinos.add(it) } }
        compose.onNodeWithText("Carrito").performClick()
        compose.onNodeWithText("Pedidos").performClick()
        compose.onNodeWithText("Perfil").performClick()
        assertEquals(listOf("carrito", "pedidos", "perfil"), destinos)
        compose.onNodeWithText("Catálogo").assertIsSelected()
    }
    @Test fun temaClaroYOscuroAplicanColoresDiferentes() {
        var claro = Color.Unspecified
        var oscuro = Color.Unspecified
        compose.setContent {
            OYL_Frutos_SecosTheme(darkTheme = false, dynamicColor = false) { claro = MaterialTheme.colorScheme.primary; Text("Claro") }
            OYL_Frutos_SecosTheme(darkTheme = true, dynamicColor = false) { oscuro = MaterialTheme.colorScheme.primary; Text("Oscuro") }
        }
        compose.onNodeWithText("Claro").assertExists()
        compose.onNodeWithText("Oscuro").assertExists()
        assertNotEquals(claro, oscuro)
    }
}
