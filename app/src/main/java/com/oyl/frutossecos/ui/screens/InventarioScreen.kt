package com.oyl.frutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.oyl.frutossecos.viewmodel.AdminProductosViewModel

private val VerdeStock = Color(0xFF456747)
private val CremaStock = Color(0xFFF4EBDD)

/**
 * Pantalla 12. Lee existencias del backend/caché.
 * Ajusta el stock absoluto solo mediante la API existente y con confirmación explícita.
 * El registro histórico de movimientos está pendiente de endpoint del servidor.
 */
@Composable
fun InventarioScreen(viewModel: AdminProductosViewModel, onVolver: () -> Unit) {
    var productoId by rememberSaveable { mutableIntStateOf(-1) }
    var entrada by rememberSaveable { mutableStateOf(true) }
    var cantidadTexto by rememberSaveable { mutableStateOf("") }
    var confirmar by rememberSaveable { mutableStateOf(false) }
    val productos = viewModel.productos
    val seleccionado = productos.firstOrNull { it.id == productoId }
    val cantidad = cantidadTexto.toIntOrNull()
    val nuevoStock = if (seleccionado != null && cantidad != null) {
        if (entrada) seleccionado.stock.toLong() + cantidad.toLong()
        else seleccionado.stock.toLong() - cantidad.toLong()
    } else null
    val valido = seleccionado != null && cantidad != null && cantidad > 0 && cantidad <= 100000 &&
        nuevoStock != null && nuevoStock >= 0L && nuevoStock <= Int.MAX_VALUE.toLong()
    val bajoStock = productos.count { it.stock <= 10 }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onVolver) { Text("← Administración", color = VerdeStock) }
            Text("Inventario y alertas", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = VerdeStock)
            Surface(color = CremaStock, shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                    Text("${productos.sumOf { it.stock }} bolsas registradas",
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("$bajoStock productos con stock bajo (10 unidades o menos)")
                    Text(if (viewModel.esModoLocal) "Sin conexión: solo consulta local" else "Datos sincronizados con el servidor",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            if (viewModel.mensaje != null) {
                Text(viewModel.mensaje.orEmpty(), style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = viewModel::actualizarCatalogo,
                enabled = !viewModel.cargando && !viewModel.guardando) { Text("Actualizar catálogo") }
            if (viewModel.cargando || viewModel.guardando) LinearProgressIndicator(Modifier.fillMaxWidth())
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(productos, key = { it.id }) { producto ->
                    Card(onClick = {
                        productoId = producto.id
                        cantidadTexto = ""
                    }, colors = CardDefaults.cardColors(
                        containerColor = if (producto.id == productoId) CremaStock else Color(0xFFF6F6F3))) {
                        Column(Modifier.fillMaxWidth().padding(12.dp)) {
                            Text(producto.nombre, fontWeight = FontWeight.SemiBold)
                            Text("${producto.presentacion} · ${producto.stock} bolsas")
                            if (producto.stock <= 10) {
                                Text("Bajo stock · Reponer", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(10.dp))
                    Text("Registrar ajuste de existencias", style = MaterialTheme.typography.titleMedium)
                    Text("Selecciona un producto de la lista. Se enviará el nuevo stock al servidor " +
                        "si hay conexión y autorización.", style = MaterialTheme.typography.bodySmall)
                    if (seleccionado != null) {
                        Spacer(Modifier.height(8.dp))
                        Text("${seleccionado.nombre} · Actual: ${seleccionado.stock}")
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilterChip(selected = entrada, onClick = { entrada = true }, label = { Text("Entrada +") })
                            FilterChip(selected = !entrada, onClick = { entrada = false }, label = { Text("Salida −") })
                        }
                        OutlinedTextField(
                            value = cantidadTexto, onValueChange = { valor ->
                                cantidadTexto = valor.filter { it.isDigit() }.take(6)
                            }, label = { Text("Cantidad de bolsas") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(), singleLine = true,
                            isError = cantidadTexto.isNotBlank() && !valido
                        )
                        if (nuevoStock != null) Text("Stock resultante: $nuevoStock")
                        Button(
                            onClick = { confirmar = true },
                            enabled = valido && viewModel.escrituraHabilitada && !viewModel.esModoLocal && !viewModel.cargando && !viewModel.guardando,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeStock)
                        ) { Text("Revisar actualización") }
                    } else {
                        Text("Selecciona un producto para registrar una entrada o salida.")
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("El envío de ajustes está deshabilitado por seguridad hasta validar el backend. " +
                        "El historial de movimientos y la protección ante actualizaciones simultáneas " +
                        "requieren implementación adicional en Spring Boot.",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    if (confirmar && seleccionado != null && valido && nuevoStock != null) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("Confirmar ajuste de inventario") },
            text = { Text("${seleccionado.nombre}: ${seleccionado.stock} → $nuevoStock bolsas. " +
                "La actualización se enviará al servidor y no se podrá deshacer desde esta pantalla.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmar = false
                    viewModel.guardarStock(seleccionado.id, nuevoStock.toInt())
                    cantidadTexto = ""
                }) { Text("Confirmar envío") }
            },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } }
        )
    }
}
