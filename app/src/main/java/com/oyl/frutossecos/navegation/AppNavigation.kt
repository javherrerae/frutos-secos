
package com.oyl.frutossecos.navegation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Scaffold
import com.oyl.frutossecos.ui.components.BottomNavigationBar
import com.oyl.frutossecos.ui.screens.MapaScreen
import com.oyl.frutossecos.ui.screens.QRScreen
import com.oyl.frutossecos.model.TiendasDemo
import com.oyl.frutossecos.ui.screens.PerfilScreen
import com.oyl.frutossecos.ui.screens.PedidosScreen
import com.oyl.frutossecos.ui.screens.DetallePedidoScreen
import com.oyl.frutossecos.viewmodel.PedidosViewModel
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.oyl.frutossecos.PantallaBienvenida
import com.oyl.frutossecos.model.Producto
import com.oyl.frutossecos.ui.screens.LoginScreen
import com.oyl.frutossecos.ui.screens.RegisterScreen
import com.oyl.frutossecos.ui.screens.DashboardScreen
import com.oyl.frutossecos.ui.screens.CatalogoScreen
import com.oyl.frutossecos.ui.screens.DetalleProductoScreen
import com.oyl.frutossecos.ui.screens.CarritoScreen
import com.oyl.frutossecos.ui.screens.ConfirmacionScreen
import com.oyl.frutossecos.viewmodel.CarritoViewModel

@Composable
fun AppNavigation(
    pantallaInicial: String = "inicio",
    onMensaje: (String) -> Unit = {}
) {
    // Estado de navegación.
    var pantalla by rememberSaveable {
        mutableStateOf(pantallaInicial)
    }

    // Producto seleccionado para mostrar su detalle.
    var productoSeleccionado by remember {
        mutableStateOf<Producto?>(null)
    }

    // ViewModel compartido durante la navegación.
    val carritoViewModel: CarritoViewModel = viewModel()
    val pedidosViewModel: PedidosViewModel = viewModel()
    var codigoPedido by rememberSaveable { mutableStateOf<String?>(null) }

    var nombrePerfil by rememberSaveable { mutableStateOf("") }
    var correoPerfil by rememberSaveable { mutableStateOf("") }
    var telefonoPerfil by rememberSaveable { mutableStateOf("") }
    var avisosPerfil by rememberSaveable { mutableStateOf(true) }
    var tiendaId by rememberSaveable { mutableStateOf("providencia") }
    var origenMapa by rememberSaveable { mutableStateOf("perfil") }
    val tiendaActual = TiendasDemo.buscar(tiendaId)
    val cerrarSesion: () -> Unit = {
        carritoViewModel.vaciarCarrito()
        pedidosViewModel.limpiarSesion()
        codigoPedido = null
        productoSeleccionado = null
        nombrePerfil = ""
        correoPerfil = ""
        telefonoPerfil = ""
        avisosPerfil = true
        tiendaId = "providencia"
        origenMapa = "perfil"
        pantalla = "inicio"
    }
    BackHandler(enabled = pantalla != "inicio" && pantalla != "dashboard") {
        pantalla = when  (pantalla) {
            "qr" -> "catalogo"
            "login", "registro" -> "inicio"
            "detalle", "carrito" -> "catalogo"
            "confirmacion", "detallePedido" -> "pedidos"
            "mapa" -> origenMapa
            else -> "dashboard"
        }
    }
    val mostrarBarra = pantalla in listOf("dashboard", "catalogo", "detalle", "carrito", "pedidos", "detallePedido", "perfil", "mapa")
    Scaffold(bottomBar = {
        if (mostrarBarra) BottomNavigationBar(pantallaActual = pantalla,
            onNavegar = { pantalla = it })
    }) { espacio ->
      Box(modifier = Modifier.fillMaxSize().padding(espacio)) {
    when (pantalla) {

        // PANTALLA 1: BIENVENIDA
        "inicio" -> {
            PantallaBienvenida(
                onLogin = {
                    pantalla = "login"
                },
                onRegistro = {
                    pantalla = "registro"
                }
            )
        }

        // PANTALLA 2: LOGIN
        "login" -> {
            LoginScreen(
                onVolver = {
                    pantalla = "inicio"
                },
                onIngresar = {
                    pantalla = "dashboard"
                }
            )
        }

        // PANTALLA 3: REGISTRO
        "registro" -> {
            RegisterScreen(
                onVolver = {
                    pantalla = "inicio"
                },
                onRegistroExitoso = {
                    onMensaje(
                        "Formulario válido. Falta guardar la cuenta."
                    )
                    pantalla = "login"
                }
            )
        }

        // PANTALLA 4: DASHBOARD
        "dashboard" -> {
            DashboardScreen(
                onAbrirCatalogo = {
                    pantalla = "catalogo"
                },
                onAbrirCarrito = {
                    pantalla = "carrito"
                },
                onAbrirPerfil = {
                    pantalla = "perfil"
                },
                onCerrarSesion = cerrarSesion
            )
        }

        // PANTALLA 5: CATÁLOGO
        "catalogo" -> {
            CatalogoScreen(
                onVolver = {
                    pantalla = "dashboard"
                },
                onAbrirProducto = { producto ->
                    productoSeleccionado = producto
                    pantalla = "detalle"
                },
                onAbrirCarrito = {
                    pantalla = "carrito"
                },
                onAbrirQR = {
                    pantalla = "qr"
                }
            )
        }

        // PANTALLA 6: DETALLE DEL PRODUCTO
        "detalle" -> {
            val producto = productoSeleccionado

            if (producto != null) {
                DetalleProductoScreen(
                    producto = producto,
                    onVolver = {
                        pantalla = "catalogo"
                    },
                    onAgregarAlCarrito = { productoAgregado, cantidad ->
                        carritoViewModel.agregarProducto(
                            producto = productoAgregado,
                            cantidad = cantidad
                        )
                        pantalla = "carrito"
                    }
                )
            } else {
                pantalla = "catalogo"
            }
        }

        // PANTALLA 7: CARRITO
        "carrito" -> {
            CarritoScreen(
                items = carritoViewModel.items,
                subtotal = carritoViewModel.subtotal,
                onVolver = {
                    pantalla = "catalogo"
                },
                onAumentar = { productoId ->
                    carritoViewModel.aumentarCantidad(productoId)
                },
                onDisminuir = { productoId ->
                    carritoViewModel.disminuirCantidad(productoId)
                },
                onEliminar = { productoId ->
                    carritoViewModel.eliminarProducto(productoId)
                },
                onConfirmar = {
                    val pedido = pedidosViewModel.confirmar(carritoViewModel.items, tiendaActual)
                    if (pedido != null) {
                        codigoPedido = pedido.codigo
                        carritoViewModel.vaciarCarrito()
                        pantalla = "confirmacion"
                    } else {
                        onMensaje("Revisa el carrito y las cantidades antes de confirmar.")
                    }
                },
                tiendaRetiro = tiendaActual.comuna,
                direccionRetiro = tiendaActual.direccion,
                onCambiarRetiro = { origenMapa = "carrito"; pantalla = "mapa" }
            )
        }

        // PANTALLA 8: CONFIRMACIÓN
        "confirmacion" -> {
            ConfirmacionScreen(
                total = pedidosViewModel.buscar(codigoPedido)?.total ?: 0,
                onVolverInicio = {
                    productoSeleccionado = null
                    pantalla = "dashboard"
                },
                onVerPedido = { pantalla = "detallePedido" }
            )
        }

        "pedidos" -> PedidosScreen(
            pedidos = pedidosViewModel.pedidos,
            onVerPedido = { codigoPedido = it; pantalla = "detallePedido" },
            onIrCatalogo = { pantalla = "catalogo" }
        )
        "detallePedido" -> DetallePedidoScreen(
            pedido = pedidosViewModel.buscar(codigoPedido),
            onVolver = { pantalla = "pedidos" }
        )

        // PANTALLA 9: PERFIL DE DEMOSTRACIÓN
        "perfil" -> {
            PerfilScreen(
                nombreGuardado = nombrePerfil,
                correoGuardado = correoPerfil,
                telefonoGuardado = telefonoPerfil,
                avisosGuardados = avisosPerfil,
                onGuardar = { nombre, correo, telefono, avisos ->
                    nombrePerfil = nombre
                    correoPerfil = correo
                    telefonoPerfil = telefono
                    avisosPerfil = avisos
                    onMensaje("Perfil guardado para esta sesión demo.")
                },
                onCerrarSesion = cerrarSesion,
                tiendaRetiro = tiendaActual.comuna,
                onAbrirMapa = { origenMapa = "perfil"; pantalla = "mapa" }
            )
        }

        "mapa" -> MapaScreen(
            tiendaActualId = tiendaId,
            onVolver = { pantalla = origenMapa },
            onSeleccionar = { id ->
                tiendaId = id
                onMensaje("Punto de retiro de demostración actualizado.")
                pantalla = origenMapa
            }
        )


        "qr" -> QRScreen(
            onVolver = {
                pantalla = "catalogo"
            },
            onAbrirProducto = { producto ->
                productoSeleccionado = producto
                pantalla = "detalle"
            }
        )
        // RUTA DESCONOCIDA
        else -> {
            PantallaPendiente(
                titulo = "Pantalla no encontrada",
                onVolver = {
                    pantalla = "inicio"
                }
            )
        }
    }
      }
    }
}

// Pantalla para rutas desconocidas.
@Composable
private fun PantallaPendiente(
    titulo: String,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Esta pantalla está en desarrollo.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onVolver) {
            Text("Volver")
        }
    }
}
