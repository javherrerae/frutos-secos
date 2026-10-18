package com.oyl.frutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.oyl.frutossecos.Producto
import com.oyl.frutossecos.viewmodel.AdminProductosViewModel
import java.text.NumberFormat
import java.util.Locale

private val VerdeProductosAdmin = Color(0xFF456747)
private val CremaProductosAdmin = Color(0xFFF4EBDD)

/** Pantalla 11. Consulta real o caché; los formularios no escriben sin endpoints CRUD confirmados. */
@Composable
fun GestionProductosScreen(
    viewModel: AdminProductosViewModel,
    onVolver: () -> Unit
) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    var mostrarFormulario by rememberSaveable { mutableStateOf(false) }
    var productoEditar by remember { mutableStateOf<Producto?>(null) }
    val productos = viewModel.productos.filter {
        it.nombre.contains(busqueda, ignoreCase = true) ||
            it.categoria.contains(busqueda, ignoreCase = true)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onVolver) { Text("← Administración", color = VerdeProductosAdmin) }
            Text("Gestionar productos", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = VerdeProductosAdmin)
            Text("Consulta el catálogo de productos activos.", style = MaterialTheme.typography.bodyMedium)

            if (viewModel.mensaje != null) {
                Surface(color = CremaProductosAdmin, shape = RoundedCornerShape(12.dp)) {
                    Text(viewModel.mensaje.orEmpty(), modifier = Modifier.fillMaxWidth().padding(12.dp),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            if (viewModel.esModoLocal) {
                Text("Datos locales: no se permite modificar el servidor sin conexión.",
                    color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            OutlinedTextField(busqueda, onValueChange = { busqueda = it },
                modifier = Modifier.fillMaxWidth(), label = { Text("Buscar producto o categoría") },
                singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { productoEditar = null; mostrarFormulario = true },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeProductosAdmin)) {
                    Text("+ Nuevo producto")
                }
                OutlinedButton(onClick = viewModel::actualizarCatalogo,
                    enabled = !viewModel.cargando && !viewModel.guardando) {
                    Text("Actualizar")
                }
            }
            if (viewModel.cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (productos.isEmpty() && !viewModel.cargando) {
                Text("No hay productos para mostrar. Revisa la conexión o los filtros.")
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.weight(1f)) {
                items(productos, key = { it.id }) { producto ->
                    Card(onClick = { productoEditar = producto; mostrarFormulario = true },
                        colors = CardDefaults.cardColors(containerColor = CremaProductosAdmin),
                        shape = RoundedCornerShape(14.dp)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp)) {
                            Text(producto.nombre, fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium)
                            Text("${producto.categoria} · ${producto.presentacion}")
                            Text("${formatearPrecioAdmin(producto.precio)} · Stock: ${producto.stock}")
                            Text("Ver formulario de edición ›", color = VerdeProductosAdmin,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {
        FormularioProductoPendiente(productoEditar, onCerrar = { mostrarFormulario = false })
    }
}

private fun formatearPrecioAdmin(precio: Double): String =
    "$" + NumberFormat.getIntegerInstance(Locale("es", "CL")).format(precio) + " CLP"

/** No enviar cambios de alta/edición hasta definir POST/PUT/activar con el backend. */
@Composable
private fun FormularioProductoPendiente(producto: Producto?, onCerrar: () -> Unit) {
    var nombre by remember(producto?.id) { mutableStateOf(producto?.nombre.orEmpty()) }
    var categoria by remember(producto?.id) { mutableStateOf(producto?.categoria.orEmpty()) }
    var descripcion by remember(producto?.id) { mutableStateOf(producto?.descripcion.orEmpty()) }
    var presentacion by remember(producto?.id) { mutableStateOf(producto?.presentacion.orEmpty()) }
    var precio by remember(producto?.id) { mutableStateOf(producto?.precio?.toString().orEmpty()) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(if (producto == null) "Nuevo producto" else "Editar producto") },
        text = {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    isError = nombre.isNotBlank() && nombre.trim().length < 2)
                OutlinedTextField(categoria, { categoria = it }, label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(presentacion, { presentacion = it }, label = { Text("Presentación") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(precio, { precio = it }, label = { Text("Precio CLP") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = precio.isNotBlank() && (precio.toDoubleOrNull() == null || precio.toDouble() < 0))
                OutlinedTextField(descripcion, { descripcion = it },
                    label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Text("Formulario preparado. Crear, editar y activar/desactivar productos requiere " +
                    "confirmar los endpoints y permisos del backend. No se guardan estos cambios.",
                    style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = {}, enabled = false) { Text("Guardar (pendiente)") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cerrar") } }
    )
}
