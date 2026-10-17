package com.oyl.frutossecos.navegation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oyl.frutossecos.AppDatabase
import com.oyl.frutossecos.PantallaBienvenida
import com.oyl.frutossecos.Usuario
import com.oyl.frutossecos.dto.RegistroRequest
import com.oyl.frutossecos.model.Producto
import com.oyl.frutossecos.model.TiendasDemo
import com.oyl.frutossecos.repository.AuthRepository
import com.oyl.frutossecos.repository.ResultadoSync
import com.oyl.frutossecos.ui.components.BottomNavigationBar
import com.oyl.frutossecos.ui.screens.*
import com.oyl.frutossecos.viewmodel.CarritoViewModel
import com.oyl.frutossecos.viewmodel.PedidosViewModel
import com.oyl.frutossecos.viewmodel.ProductosViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AppNavigation(
    pantallaInicial: String = "inicio",
    onMensaje: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Base de datos y repositorios para la API y persistencia Room
    val db = remember(context) { AppDatabase.crear(context) }
    val authRepository = remember(db) { AuthRepository(db.usuarioDao(), db.sesionDao()) }

    // Estado de navegación
    var pantalla by rememberSaveable { mutableStateOf(pantallaInicial) }

    // Producto seleccionado para pantalla de detalle
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }

    // ViewModels compartidos durante la navegación
    val productosViewModel: ProductosViewModel = viewModel()
    val carritoViewModel: CarritoViewModel = viewModel()
    val pedidosViewModel: PedidosViewModel = viewModel()

    // Estado del usuario activo autenticado
    var usuarioLogueado by remember { mutableStateOf<Usuario?>(null) }
    var estadoConexion by remember { mutableStateOf("Conectado al servidor") }

    // Estados de autenticación (login y registro)
    var loginCargando by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var registroCargando by remember { mutableStateOf(false) }
    var registroError by remember { mutableStateOf<String?>(null) }

    var codigoPedido by rememberSaveable { mutableStateOf<String?>(null) }
    var totalConfirmado by rememberSaveable { mutableIntStateOf(0) }

    var nombrePerfil by rememberSaveable { mutableStateOf("") }
    var correoPerfil by rememberSaveable { mutableStateOf("") }
    var telefonoPerfil by rememberSaveable { mutableStateOf("") }
    var avisosPerfil by rememberSaveable { mutableStateOf(true) }
    var tiendaId by rememberSaveable { mutableStateOf("providencia") }
    var origenMapa by rememberSaveable { mutableStateOf("perfil") }
    val tiendaActual = TiendasDemo.buscar(tiendaId)

    val cerrarSesion: () -> Unit = {
        coroutineScope.launch(Dispatchers.IO) {
            authRepository.cerrarSesion()
        }
        carritoViewModel.vaciarCarrito()
        pedidosViewModel.limpiarSesion()
        usuarioLogueado = null
        codigoPedido = null
        productoSeleccionado = null
        nombrePerfil = ""
        correoPerfil = ""
        telefonoPerfil = ""
        avisosPerfil = true
        tiendaId = "providencia"
        origenMapa = "perfil"
        loginError = null
        registroError = null
        pantalla = "inicio"
    }

    BackHandler(enabled = pantalla != "inicio" && pantalla != "dashboard") {
        pantalla = when (pantalla) {
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
        if (mostrarBarra) {
            BottomNavigationBar(
                pantallaActual = pantalla,
                onNavegar = { pantalla = it }
            )
        }
    }) { espacio ->
        Box(modifier = Modifier.fillMaxSize().padding(espacio)) {
            when (pantalla) {

                // PANTALLA 1: BIENVENIDA
                "inicio" -> {
                    PantallaBienvenida(
                        onLogin = {
                            loginError = null
                            pantalla = "login"
                        },
                        onRegistro = {
                            registroError = null
                            pantalla = "registro"
                        }
                    )
                }

                // PANTALLA 2: LOGIN CONECTADO A LA API / AUTH REPOSITORY
                "login" -> {
                    LoginScreen(
                        onVolver = {
                            loginError = null
                            pantalla = "inicio"
                        },
                        cargando = loginCargando,
                        mensajeError = loginError,
                        onIngresar = { credencial, clave ->
                            loginCargando = true
                            loginError = null
                            coroutineScope.launch {
                                val resultado = withContext(Dispatchers.IO) {
                                    authRepository.login(credencial, clave)
                                }
                                loginCargando = false
                                when (resultado) {
                                    is ResultadoSync.Exito -> {
                                        val usuario = resultado.datos
                                        usuarioLogueado = usuario
                                        nombrePerfil = usuario.nombre
                                        correoPerfil = usuario.correo
                                        telefonoPerfil = usuario.telefono
                                        estadoConexion = "Conectado al servidor central"
                                        carritoViewModel.configurarUsuario(usuario.id)
                                        pedidosViewModel.configurarUsuario(usuario.id)
                                        onMensaje("¡Bienvenido/a, ${usuario.nombre}!")
                                        pantalla = "dashboard"
                                    }
                                    is ResultadoSync.DesdeCacheConError -> {
                                        val usuario = resultado.datos
                                        usuarioLogueado = usuario
                                        nombrePerfil = usuario.nombre
                                        correoPerfil = usuario.correo
                                        telefonoPerfil = usuario.telefono
                                        estadoConexion = "Modo sin conexión (datos locales)"
                                        carritoViewModel.configurarUsuario(usuario.id)
                                        pedidosViewModel.configurarUsuario(usuario.id)
                                        onMensaje("Sesión offline iniciada: ${usuario.nombre}")
                                        pantalla = "dashboard"
                                    }
                                    is ResultadoSync.Error -> {
                                        loginError = resultado.mensajeError
                                    }
                                }
                            }
                        }
                    )
                }

                // PANTALLA 3: REGISTRO CONECTADO A LA API / AUTH REPOSITORY
                "registro" -> {
                    RegisterScreen(
                        onVolver = {
                            registroError = null
                            pantalla = "inicio"
                        },
                        cargando = registroCargando,
                        mensajeError = registroError,
                        onRegistrar = { nombre, correo, clave ->
                            registroCargando = true
                            registroError = null
                            coroutineScope.launch {
                                val aliasGenerado = correo.substringBefore("@").lowercase()
                                val request = RegistroRequest(
                                    nombre = nombre,
                                    alias = aliasGenerado,
                                    contrasena = clave,
                                    correo = correo,
                                    telefono = "+56 9 0000 0000",
                                    direccion = "Santiago"
                                )
                                val resultado = withContext(Dispatchers.IO) {
                                    authRepository.registro(request)
                                }
                                registroCargando = false
                                when (resultado) {
                                    is ResultadoSync.Exito -> {
                                        onMensaje("¡Cuenta registrada con éxito en el servidor!")
                                        pantalla = "login"
                                    }
                                    is ResultadoSync.DesdeCacheConError -> {
                                        onMensaje("Cuenta registrada localmente (sin conexión al servidor)")
                                        pantalla = "login"
                                    }
                                    is ResultadoSync.Error -> {
                                        registroError = resultado.mensajeError
                                    }
                                }
                            }
                        }
                    )
                }

                // PANTALLA 4: DASHBOARD
                "dashboard" -> {
                    DashboardScreen(
                        onAbrirCatalogo = { pantalla = "catalogo" },
                        onAbrirCarrito = { pantalla = "carrito" },
                        onAbrirPerfil = { pantalla = "perfil" },
                        onCerrarSesion = cerrarSesion
                    )
                }

                // PANTALLA 5: CATÁLOGO CONECTADO A LA API REST (PRODUCTOS VIEWMODEL)
                "catalogo" -> {
                    CatalogoScreen(
                        onVolver = { pantalla = "dashboard" },
                        productos = productosViewModel.productos,
                        cargando = productosViewModel.cargando,
                        mensajeEstado = productosViewModel.mensajeEstado,
                        onRefrescar = { productosViewModel.cargarCatalogo() },
                        onAbrirProducto = { producto ->
                            productoSeleccionado = producto
                            pantalla = "detalle"
                        },
                        onAbrirCarrito = { pantalla = "carrito" },
                        onAbrirQR = { pantalla = "qr" }
                    )
                }

                // PANTALLA 6: DETALLE DEL PRODUCTO
                "detalle" -> {
                    // Buscar el producto actualizado (con stock rebajado) desde el ViewModel
                    val producto = productoSeleccionado?.let { sel ->
                        productosViewModel.buscarPorId(sel.id) ?: sel
                    }
                    if (producto != null) {
                        DetalleProductoScreen(
                            producto = producto,
                            onVolver = { pantalla = "catalogo" },
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

                // PANTALLA 7: CARRITO CONECTADO A ROOM Y PREPARACIÓN DE PEDIDO
                "carrito" -> {
                    CarritoScreen(
                        items = carritoViewModel.items,
                        subtotal = carritoViewModel.subtotal,
                        cargando = pedidosViewModel.cargando,
                        onVolver = { pantalla = "catalogo" },
                        onAumentar = { productoId -> carritoViewModel.aumentarCantidad(productoId) },
                        onDisminuir = { productoId -> carritoViewModel.disminuirCantidad(productoId) },
                        onEliminar = { productoId -> carritoViewModel.eliminarProducto(productoId) },
                        onConfirmar = {
                            // Capturar los ítems antes de vaciar el carrito
                            val itemsParaStock = carritoViewModel.items.map {
                                it.producto.id to it.cantidad
                            }
                            pedidosViewModel.confirmar(
                                items = carritoViewModel.items,
                                tienda = tiendaActual,
                                onCompletado = { pedido ->
                                    if (pedido != null) {
                                        codigoPedido = pedido.codigo
                                        totalConfirmado = pedido.total
                                        carritoViewModel.vaciarCarrito()
                                        // Rebajar el stock localmente en Room y refrescar catálogo
                                        productosViewModel.rebajarStockLocal(itemsParaStock)
                                        pantalla = "confirmacion"
                                    } else {
                                        onMensaje("Revisa el carrito y las cantidades antes de confirmar.")
                                    }
                                }
                            )
                        },
                        tiendaRetiro = tiendaActual.comuna,
                        direccionRetiro = tiendaActual.direccion,
                        onCambiarRetiro = { origenMapa = "carrito"; pantalla = "mapa" }
                    )
                }

                // PANTALLA 8: CONFIRMACIÓN CON DATOS REALES DE PEDIDO Y SERVIDOR
                "confirmacion" -> {
                    ConfirmacionScreen(
                        total = if (totalConfirmado > 0) totalConfirmado else (pedidosViewModel.buscar(codigoPedido)?.total ?: carritoViewModel.subtotal),
                        codigoPedido = codigoPedido,
                        mensajeServidor = pedidosViewModel.mensajeUltimoPedido,
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

                // PANTALLA 9: PERFIL DE USUARIO CON DATOS REALES Y PERSISTENCIA
                "perfil" -> {
                    PerfilScreen(
                        nombreGuardado = nombrePerfil,
                        correoGuardado = correoPerfil,
                        telefonoGuardado = telefonoPerfil,
                        avisosGuardados = avisosPerfil,
                        rol = usuarioLogueado?.rol ?: "cliente",
                        estadoConexion = estadoConexion,
                        onGuardar = { nombre, correo, telefono, avisos ->
                            nombrePerfil = nombre
                            correoPerfil = correo
                            telefonoPerfil = telefono
                            avisosPerfil = avisos
                            usuarioLogueado?.let { u ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    val modificado = u.copy(
                                        nombre = nombre,
                                        correo = correo,
                                        telefono = telefono
                                    )
                                    db.usuarioDao().actualizar(modificado)
                                    usuarioLogueado = modificado
                                }
                            }
                            onMensaje("Perfil guardado y actualizado.")
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
                        onMensaje("Punto de retiro actualizado.")
                        pantalla = origenMapa
                    }
                )

                "qr" -> QRScreen(
                    onVolver = { pantalla = "catalogo" },
                    onAbrirProducto = { producto ->
                        productoSeleccionado = producto
                        pantalla = "detalle"
                    }
                )

                else -> {
                    PantallaPendiente(
                        titulo = "Pantalla no encontrada",
                        onVolver = { pantalla = "inicio" }
                    )
                }
            }
        }
    }
}

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
