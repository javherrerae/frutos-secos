package com.oyl.frutossecos

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.oyl.frutossecos.model.LineaPedidoDemo
import com.oyl.frutossecos.model.PedidoDemo
import com.oyl.frutossecos.model.Producto
import com.oyl.frutossecos.model.ItemCarrito
import com.oyl.frutossecos.navegation.AppNavigation
import com.oyl.frutossecos.ui.screens.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w480dp-h1000dp")
class PantallasTest {
    @get:Rule val compose = createComposeRule()
    private val producto = Producto(7, "Almendras", "Frutos", "Naturales", "500 g", 2000, 2, "🥜")

    @Test fun bienvenidaPermiteIniciarSesionOCrearCuenta() {
        var destino = ""
        compose.setContent { PantallaBienvenida({ destino = "login" }, { destino = "registro" }) }
        compose.onNodeWithText("Iniciar sesión").performClick()
        assertEquals("login", destino)
        compose.onNodeWithText("Crear cuenta").performClick()
        assertEquals("registro", destino)
    }

    @Test fun navegacionDeBienvenidaARegistroYRetorno() {
        compose.setContent { AppNavigation() }
        compose.onNodeWithText("Crear cuenta").performClick()
        compose.onNodeWithText("Nombre completo").assertExists()
        compose.onNodeWithText("Volver al inicio").performClick()
        compose.onNodeWithText("¡Naturalmente buenos!").assertExists()
    }

    @Test fun pantallaQrPermiteVolverSinAbrirCamara() {
        var vueltas = 0
        compose.setContent { QRScreen(listOf(producto), { vueltas++ }, {}) }
        compose.onNodeWithText("Escanear producto").assertIsDisplayed()
        compose.onNodeWithText("← Volver").performClick()
        assertEquals(1, vueltas)
    }

    @Test fun mapaPermiteSeleccionarUbicacionDeReferencia() {
        var seleccion = ""
        compose.setContent { MapaScreen("providencia", {}, { seleccion = it }) }
        compose.onNodeWithText("Plaza Ñuñoa").performScrollTo().performClick()
        compose.onNodeWithText("Seleccionar Ñuñoa").performClick()
        assertEquals("nunoa", seleccion)
    }

    @Test fun loginValidaYEntregaCredenciales() {
        var credenciales = ""
        compose.setContent { LoginScreen({}, { usuario, clave -> credenciales = "$usuario:$clave" }, mensajeError = "Error de acceso") }
        compose.onNodeWithText("Error de acceso").assertExists()
        compose.onNodeWithText("Iniciar sesión").assertIsNotEnabled()
        compose.onNodeWithText("Correo electrónico o alias").performTextInput("ana")
        compose.onNodeWithText("Contraseña").performTextInput("123")
        compose.onNodeWithText("Debe tener al menos 6 caracteres.").assertExists()
        compose.onNodeWithText("Contraseña").performTextInput("456")
        compose.onNodeWithText("Iniciar sesión").performClick()
        assertEquals("ana:123456", credenciales)
    }

    @Test fun registroValidaCamposYEnviaDatos() {
        var resultado = ""
        compose.setContent { RegisterScreen({}, { nombre, correo, clave -> resultado = "$nombre:$correo:$clave" }) }
        compose.onNodeWithText("Nombre completo").performTextInput("Ana")
        compose.onNodeWithText("Correo electrónico").performTextInput("ana@example.com")
        compose.onNodeWithText("Contraseña", substring = false).performTextInput("123456")
        compose.onNodeWithText("Confirmar contraseña").performTextInput("123456")
        compose.onAllNodesWithText("Crear cuenta").filter(hasClickAction()).onFirst().performClick()
        assertEquals("Ana:ana@example.com:123456", resultado)
    }

    @Test fun dashboardNavegaDesdeProductosYCarrito() {
        var destino = ""
        compose.setContent { DashboardScreen(listOf(producto), { destino = "catalogo" }, { destino = "carrito" },
            { destino = "perfil" }, { destino = "salir" }) }
        compose.onNodeWithText("🛒 Carrito").performClick()
        assertEquals("carrito", destino)
        compose.onNodeWithText("Explorar productos").performScrollTo().performClick()
        assertEquals("catalogo", destino)
        compose.onNodeWithText("Almendras").performScrollTo().assertIsDisplayed()
    }

    @Test fun perfilValidaYConfirmaSalida() {
        var salidas = 0
        var guardados = 0
        compose.setContent { PerfilScreen("Ana", "ana@example.com", "", true,
            { _, _, _, _ -> guardados++ }, { salidas++ }) }
        compose.onNodeWithText("Guardar cambios de perfil").performScrollTo().performClick()
        assertEquals(1, guardados)
        compose.onNodeWithText("Cerrar sesión").performScrollTo().performClick()
        compose.onNodeWithText("¿Cerrar sesión?").assertIsDisplayed()
        compose.onNodeWithText("Cancelar").performClick()
        assertEquals(0, salidas)
    }

    @Test fun carritoVacioPermiteExplorar() {
        var vueltas = 0
        compose.setContent { CarritoScreen(emptyList(), 0, onVolver = { vueltas++ }, onAumentar = {},
            onDisminuir = {}, onEliminar = {}, onConfirmar = {}) }
        compose.onNodeWithText("Tu carrito está vacío").assertIsDisplayed()
        compose.onNodeWithText("Explorar productos").performClick()
        assertEquals(1, vueltas)
    }

    @Test fun carritoConProductosPermiteCambiarRetiroYConfirmar() {
        var accion = ""
        compose.setContent { CarritoScreen(listOf(ItemCarrito(producto, 1)), 2000,
            onVolver = {}, onAumentar = { accion = "aumentar:$it" },
            onDisminuir = { accion = "disminuir:$it" }, onEliminar = { accion = "eliminar:$it" },
            onConfirmar = { accion = "confirmar" }, onCambiarRetiro = { accion = "retiro" }) }
        compose.onNodeWithText("Cambiar ubicación de referencia").performClick()
        assertEquals("retiro", accion)
        compose.onNodeWithText("Almendras").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("+").performScrollTo().performClick()
        assertEquals("aumentar:7", accion)
        compose.onNodeWithText("Confirmar pedido demo  →").performClick()
        assertEquals("confirmar", accion)
    }

    @Test fun carritoProcesandoNoPermiteConfirmar() {
        compose.setContent { CarritoScreen(listOf(ItemCarrito(producto, 1)), 2000, cargando = true,
            onVolver = {}, onAumentar = {}, onDisminuir = {}, onEliminar = {}, onConfirmar = { fail("No debe confirmar") }) }
        compose.onNodeWithText("Procesando pedido...").assertIsNotEnabled()
    }

    @Test fun catalogoFiltraYAbreProducto() {
        var seleccionado: Producto? = null
        val otro = producto.copy(id = 8, nombre = "Semillas", categoria = "Semillas")
        compose.setContent { CatalogoScreen({}, { seleccionado = it }, {}, {}, productos = listOf(producto, otro),
            mensajeEstado = "Catálogo actualizado") }
        compose.onNodeWithText("Catálogo actualizado").assertIsDisplayed()
        compose.onNodeWithText("Almendras, semillas, fruta...").performTextInput("Almendras")
        compose.onNodeWithText("1 productos").performScrollTo().assertIsDisplayed()
        compose.onNode(hasText("Almendras") and hasClickAction() and !isEditable()).performScrollTo().performClick()
        assertEquals(producto, seleccionado)
    }

    @Test fun catalogoSinCoincidenciasMuestraMensaje() {
        compose.setContent { CatalogoScreen({}, {}, {}, {}, productos = listOf(producto)) }
        compose.onNodeWithText("Almendras, semillas, fruta...").performTextInput("inexistente")
        compose.onNodeWithText("No encontramos productos con esa búsqueda.").performScrollTo().assertIsDisplayed()
    }

    @Test fun detalleLimitaCantidadAlStockYAgrega() {
        var cantidad = 0
        compose.setContent { DetalleProductoScreen(producto, {}, { _, n -> cantidad = n }) }
        compose.onNodeWithText("+").performScrollTo().performClick()
        compose.onNodeWithText("+").assertIsNotEnabled()
        compose.onNodeWithText("Agregar 2 bolsa(s) al carrito").performScrollTo().performClick()
        assertEquals(2, cantidad)
    }

    @Test fun detalleAgotadoImpideAgregar() {
        compose.setContent { DetalleProductoScreen(producto.copy(stock = 0), {}, { _, _ -> fail("No debe agregar") }) }
        compose.onNodeWithText("Producto agotado").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Agregar 1 bolsa(s) al carrito").performScrollTo().assertIsNotEnabled()
    }

    @Test fun pedidosVaciosPermiteAbrirCatalogo() {
        var abiertos = 0
        compose.setContent { PedidosScreen(emptyList(), {}, { abiertos++ }) }
        compose.onNodeWithText("Todavía no tienes pedidos.").assertIsDisplayed()
        compose.onNodeWithText("Explorar catálogo").performClick()
        assertEquals(1, abiertos)
    }

    @Test fun pedidosFiltraEntregadosYAbreSeleccionado() {
        var seleccionado = ""
        val pedidos = listOf(PedidoDemo("A1", 0, emptyList(), estado = "Confirmado"),
            PedidoDemo("B2", 0, emptyList(), estado = "Entregado"))
        compose.setContent { PedidosScreen(pedidos, { seleccionado = it }, {}) }
        compose.onNodeWithText("En curso (1)").performClick()
        compose.onNodeWithText("A1").assertIsDisplayed()
        compose.onNodeWithText("B2").assertDoesNotExist()
        compose.onNodeWithText("Ver detalle").performScrollTo().performClick()
        assertEquals("A1", seleccionado)
    }

    @Test fun detallePedidoMuestraLineasYVuelve() {
        var vueltas = 0
        val pedido = PedidoDemo("P12", 0, listOf(LineaPedidoDemo("Nueces", "500 g", 2000, 2)))
        compose.setContent { DetallePedidoScreen(pedido) { vueltas++ } }
        compose.onNodeWithText("P12").assertIsDisplayed()
        compose.onNodeWithText("Nueces").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Volver a mis pedidos").performScrollTo().performClick()
        assertEquals(1, vueltas)
    }

    @Test fun detalleAusenteInformaYPermiteVolver() {
        var vueltas = 0
        compose.setContent { DetallePedidoScreen(null) { vueltas++ } }
        compose.onNodeWithText("Este pedido ya no está disponible en la sesión.").assertIsDisplayed()
        compose.onNodeWithText("Volver a mis pedidos").performClick()
        assertEquals(1, vueltas)
    }

    @Test fun administracionNavegaALasOpciones() {
        var destino = ""
        compose.setContent { AdminDashboardScreen({ destino = "productos" }, { destino = "inventario" },
            { destino = "perfil" }, { destino = "salir" }) }
        compose.onNodeWithText("Gestión de productos").performClick()
        assertEquals("productos", destino)
        compose.onNodeWithText("Inventario y alertas").performClick()
        assertEquals("inventario", destino)
        compose.onNodeWithText("Mi perfil").performScrollTo().performClick()
        assertEquals("perfil", destino)
        compose.onNodeWithText("Cerrar sesión").performScrollTo().performClick()
        assertEquals("salir", destino)
    }
}
