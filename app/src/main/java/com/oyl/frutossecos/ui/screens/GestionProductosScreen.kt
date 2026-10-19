package com.oyl.frutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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

/** Pantalla 11: alta, edición y estado de productos persistidos localmente. */
@Composable
fun GestionProductosScreen(viewModel: AdminProductosViewModel, onVolver: () -> Unit) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    var mostrarFormulario by rememberSaveable { mutableStateOf(false) }
    var productoEditar by remember { mutableStateOf<Producto?>(null) }
    val productos = viewModel.productos.filter {
        it.nombre.contains(busqueda, ignoreCase = true) ||
            it.categoria.contains(busqueda, ignoreCase = true)
    }

    Surface(Modifier.fillMaxSize(), color = Color.White) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onVolver) { Text("← Administración", color = VerdeProductosAdmin) }
            Text("Gestionar productos", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = VerdeProductosAdmin)
            Text("Los cambios se guardan en Room/SQLite de este dispositivo.",
                style = MaterialTheme.typography.bodySmall)
            if (viewModel.mensaje != null) {
                Surface(color = CremaProductosAdmin, shape = RoundedCornerShape(12.dp)) {
                    Text(viewModel.mensaje.orEmpty(), Modifier.fillMaxWidth().padding(12.dp),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            OutlinedTextField(
                value = busqueda, onValueChange = { busqueda = it },
                modifier = Modifier.fillMaxWidth(), label = { Text("Buscar producto o categoría") },
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { productoEditar = null; mostrarFormulario = true },
                    enabled = !viewModel.cargando && !viewModel.guardando,
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeProductosAdmin)
                ) { Text("+ Nuevo") }
                OutlinedButton(
                    onClick = viewModel::actualizarCatalogo,
                    enabled = !viewModel.cargando && !viewModel.guardando
                ) { Text("Actualizar") }
            }
            if (viewModel.cargando || viewModel.guardando) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            if (productos.isEmpty() && !viewModel.cargando) {
                Text("No hay productos para mostrar. Prueba otro filtro.")
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(productos, key = { it.id }) { producto ->
                    Card(
                        onClick = { productoEditar = producto; mostrarFormulario = true },
                        enabled = !viewModel.guardando,
                        colors = CardDefaults.cardColors(containerColor = CremaProductosAdmin),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(producto.nombre, fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium)
                            Text("${producto.categoria} · ${producto.presentacion}")
                            Text("${precioCLPAdmin(producto.precio)} · Stock: ${producto.stock}")
                            Text(if (producto.activo == 1) "Activo" else "Inactivo",
                                color = if (producto.activo == 1) VerdeProductosAdmin else MaterialTheme.colorScheme.error)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tocar para editar ›", color = VerdeProductosAdmin,
                                    style = MaterialTheme.typography.bodySmall)
                                TextButton(
                                    onClick = { viewModel.cambiarActivo(producto.id, producto.activo != 1) },
                                    enabled = !viewModel.cargando && !viewModel.guardando
                                ) { Text(if (producto.activo == 1) "Desactivar" else "Activar") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {
        FormularioProductoLocal(
            producto = productoEditar,
            guardando = viewModel.guardando,
            onGuardar = { nombre, categoria, descripcion, presentacion, precio ->
                viewModel.guardarProducto(
                    id = productoEditar?.id,
                    nombre = nombre, categoria = categoria, descripcion = descripcion,
                    presentacion = presentacion, precioTexto = precio,
                    onGuardado = { mostrarFormulario = false }
                )
            },
            onCerrar = { mostrarFormulario = false }
        )
    }
}

private fun precioCLPAdmin(precio: Double): String =
    "$" + NumberFormat.getIntegerInstance(Locale("es", "CL")).format(precio) + " CLP"

@Composable
private fun FormularioProductoLocal(
    producto: Producto?,
    guardando: Boolean,
    onGuardar: (String, String, String, String, String) -> Unit,
    onCerrar: () -> Unit
) {
    var nombre by remember(producto?.id) { mutableStateOf(producto?.nombre.orEmpty()) }
    var categoria by remember(producto?.id) { mutableStateOf(producto?.categoria.orEmpty()) }
    var descripcion by remember(producto?.id) { mutableStateOf(producto?.descripcion.orEmpty()) }
    var presentacion by remember(producto?.id) { mutableStateOf(producto?.presentacion.orEmpty()) }
    var precio by remember(producto?.id) { mutableStateOf(producto?.precio?.toInt()?.toString().orEmpty()) }
    val precioNumero = precio.toIntOrNull()
    val valido = nombre.trim().length >= 2 && categoria.isNotBlank() &&
        presentacion.isNotBlank() && precioNumero != null && precioNumero in 1..99999999

    AlertDialog(
        onDismissRequest = { if (!guardando) onCerrar() },
        title = { Text(if (producto == null) "Nuevo producto" else "Editar producto") },
        text = {
            Column(
                Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    isError = nombre.isNotBlank() && nombre.trim().length < 2)
                OutlinedTextField(categoria, { categoria = it }, label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(presentacion, { presentacion = it }, label = { Text("Presentación (ej. Bolsa 250 g)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(precio, { precio = it.filter { c -> c.isDigit() }.take(8) },
                    label = { Text("Precio en pesos chilenos") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = precio.isNotBlank() && (precioNumero == null || precioNumero < 1))
                OutlinedTextField(descripcion, { descripcion = it }, label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(), minLines = 2)
                if (producto == null) {
                    Text("El producto se crea activo con stock 0; repón existencias desde Inventario.",
                        style = MaterialTheme.typography.bodySmall)
                }
                Text("Guardado local: no se sincroniza con otros dispositivos.",
                    style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = { onGuardar(nombre, categoria, descripcion, presentacion, precio) },
                enabled = valido && !guardando) { Text(if (guardando) "Guardando…" else "Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCerrar, enabled = !guardando) { Text("Cancelar") }
        }
    )
}
