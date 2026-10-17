package com.oyl.frutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oyl.frutossecos.model.PedidoDemo
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val VerdePedidos = Color(0xFF456747)
private val CremaPedidos = Color(0xFFF4EBDD)
private fun dineroPedido(valor: Int) = "$" + NumberFormat.getIntegerInstance(Locale("es", "CL")).format(valor) + " CLP"
private fun fechaPedido(valor: Long) = SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale("es", "CL")).format(Date(valor))

@Composable
fun PedidosScreen(pedidos: List<PedidoDemo>, onVerPedido: (String) -> Unit,
    onIrCatalogo: () -> Unit) {
    var soloEnCurso by rememberSaveable { mutableStateOf(false) }
    val visibles = pedidos.filter { !soloEnCurso || it.estado != "Entregado" }
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text("Mis pedidos", style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold, color = VerdePedidos)
                Text("Historial de pedidos", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !soloEnCurso, onClick = { soloEnCurso = false },
                        label = { Text("Todos (${pedidos.size})") })
                    FilterChip(selected = soloEnCurso, onClick = { soloEnCurso = true },
                        label = { Text("En curso (${pedidos.count { it.estado != "Entregado" }})") })
                }
            }
            if (visibles.isEmpty()) item {
                Text(if (pedidos.isEmpty()) "Todavía no tienes pedidos." else "No hay pedidos en curso.")
                Text("Cuando confirmes un carrito, aparecerá aquí con su detalle.")
                Button(onClick = onIrCatalogo,
                    colors = ButtonDefaults.buttonColors(containerColor = VerdePedidos)) {
                    Text("Explorar catálogo")
                }
            }
            items(visibles, key = { it.codigo }) { pedido ->
                Surface(color = CremaPedidos, shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(pedido.codigo, fontWeight = FontWeight.Bold, color = VerdePedidos)
                        Text(pedido.estado)
                        Text(fechaPedido(pedido.fechaMillis), style = MaterialTheme.typography.bodySmall)
                        Text("${pedido.cantidadBolsas} " + if (pedido.cantidadBolsas == 1) "bolsa" else "bolsas")
                        Text(dineroPedido(pedido.total), fontWeight = FontWeight.Bold)
                        Text("Ubicación de referencia: ${pedido.puntoRetiro}")
                        TextButton(onClick = { onVerPedido(pedido.codigo) }) {
                            Text("Ver detalle", color = VerdePedidos)
                        }
                    }
                }
            }
            item { Text("Historial guardado en este dispositivo hasta cerrar sesión. No se realizan pagos ni se envían pedidos al servidor.",
                style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
fun DetallePedidoScreen(pedido: PedidoDemo?, onVolver: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                TextButton(onClick = onVolver) { Text("Volver a mis pedidos", color = VerdePedidos) }
                Text("Detalle del pedido", style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold, color = VerdePedidos)
            }
            if (pedido == null) {
                item { Text("Este pedido ya no está disponible en la sesión.") }
            } else {
                item {
                    Text(pedido.codigo, fontWeight = FontWeight.Bold)
                    Text(fechaPedido(pedido.fechaMillis))
                    Text("Estado: ${pedido.estado}", color = VerdePedidos)
                }
                items(pedido.productos) { producto ->
                    Surface(color = CremaPedidos, shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(producto.nombre, fontWeight = FontWeight.Bold)
                            Text(producto.formato)
                            Text("${producto.cantidad} × ${dineroPedido(producto.precioUnitario)}")
                            Text(dineroPedido(producto.subtotal), color = VerdePedidos)
                        }
                    }
                }
                item {
                    Text("Subtotal: ${dineroPedido(pedido.total)}")
                    Text("Ubicación de referencia: ${pedido.puntoRetiro}")
                    Text(pedido.direccionRetiro)
                    Text(
                        "Ubicación real de referencia. No existe retiro físico de OYL.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text("Total: ${dineroPedido(pedido.total)}", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge, color = VerdePedidos)
                }
                item {
                    Text(
                        "Este pedido se guarda en el dispositivo. No se realizó ningún pago real. El historial se elimina al cerrar sesión.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
