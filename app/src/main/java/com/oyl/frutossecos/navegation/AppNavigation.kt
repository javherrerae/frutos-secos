
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
    var pantalla by remember {
        mutableStateOf(pantallaInicial)
    }

    // Producto seleccionado para mostrar su detalle.
    var productoSeleccionado by remember {
        mutableStateOf<Producto?>(null)
    }

    // ViewModel compartido durante la navegación.
    val carritoViewModel: CarritoViewModel = viewModel()

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
                onCerrarSesion = {
                    carritoViewModel.vaciarCarrito()
                    productoSeleccionado = null
                    pantalla = "inicio"
                }
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
                    if (carritoViewModel.items.isNotEmpty()) {
                        pantalla = "confirmacion"
                    } else {
                        onMensaje("El carrito está vacío.")
                    }
                }
            )
        }

        // PANTALLA 8: CONFIRMACIÓN
        "confirmacion" -> {
            ConfirmacionScreen(
                total = carritoViewModel.subtotal,
                onVolverInicio = {
                    carritoViewModel.vaciarCarrito()
                    productoSeleccionado = null
                    pantalla = "dashboard"
                }
            )
        }

        // PANTALLA 9: PERFIL PROVISIONAL
        "perfil" -> {
            PantallaPendiente(
                titulo = "Mi perfil",
                onVolver = {
                    pantalla = "dashboard"
                }
            )
        }

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

// Pantalla provisional hasta implementar el perfil real.
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
