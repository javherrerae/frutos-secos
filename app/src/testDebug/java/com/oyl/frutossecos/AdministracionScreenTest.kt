package com.oyl.frutossecos

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.oyl.frutossecos.ui.screens.GestionProductosScreen
import com.oyl.frutossecos.ui.screens.InventarioScreen
import com.oyl.frutossecos.viewmodel.AdministracionProductos
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w480dp-h1000dp")
class AdministracionScreenTest {
    @get:Rule val compose = createComposeRule()
    private class Fake : AdministracionProductos {
        override val productos = listOf(Producto(1, "Nueces", "Frutos", "Natural", 2000.0, "500 g", 5, "", 1))
        override val cargando = false
        override val guardando = false
        override val mensaje: String? = null
        var accion = ""
        override fun actualizarCatalogo() { accion = "actualizar" }
        override fun guardarProducto(id: Int?, nombre: String, categoria: String, descripcion: String, presentacion: String, precioTexto: String, onGuardado: () -> Unit) {
            accion = "guardar:$nombre:$precioTexto"; onGuardado()
        }
        override fun cambiarActivo(id: Int, activar: Boolean) { accion = "activo:$id:$activar" }
        override fun registrarMovimiento(id: Int, entrada: Boolean, cantidad: Int, onGuardado: () -> Unit) {
            accion = "movimiento:$id:$entrada:$cantidad"; onGuardado()
        }
    }

    @Test fun inventarioValidaStockYConfirmaMovimiento() {
        val fake = Fake()
        compose.setContent { InventarioScreen(fake, {}) }
        compose.onNodeWithText("Nueces").performClick()
        compose.onNodeWithText("Salida −").performScrollTo().performClick()
        compose.onNodeWithText("Cantidad de unidades").performScrollTo().performTextInput("6")
        compose.onNodeWithText("Stock previsto: -1").assertExists()
        compose.onNodeWithText("Confirmar ajuste").assertIsNotEnabled()
        compose.onNodeWithText("Cantidad de unidades").performTextClearance()
        compose.onNodeWithText("Cantidad de unidades").performTextInput("2")
        compose.onNodeWithText("Confirmar ajuste").assertIsEnabled()
        assertEquals("", fake.accion)
    }

    @Test fun gestionFiltraProductosYPermiteDesactivar() {
        val fake = Fake()
        compose.setContent { GestionProductosScreen(fake, {}) }
        compose.onNodeWithText("Desactivar").performClick()
        assertEquals("activo:1:false", fake.accion)
        compose.onNodeWithText("Buscar producto o categoría").performTextInput("inexistente")
        compose.onNodeWithText("No hay productos para mostrar. Prueba otro filtro.").assertExists()
        compose.onNodeWithText("Actualizar").performClick()
        assertEquals("actualizar", fake.accion)
    }

}
