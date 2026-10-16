
package com.oyl.frutossecos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oyl.frutossecos.model.ItemCarrito
import java.text.NumberFormat
import java.util.Locale

private val VerdeCarrito = Color(0xFF456747)
private val CremaCarrito = Color(0xFFF4EBDD)
private val GrafitoCarrito = Color(0xFF292D26)

private fun formatoCarrito(precio: Int): String {
    return "$" + NumberFormat.getNumberInstance(
        Locale("es", "CL")
    ).format(precio) + " CLP"
}

@Composable
fun CarritoScreen(
    items: List<ItemCarrito>,
    subtotal: Int,
    onVolver: () -> Unit,
    onAumentar: (Int) -> Unit,
    onDisminuir: (Int) -> Unit,
    onEliminar: (Int) -> Unit,
    onConfirmar: () -> Unit,
    tiendaRetiro: String = "Providencia",
    direccionRetiro: String = "Av. del Almendro 120",
    onCambiarRetiro: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(18.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onVolver) {
                Text(
                    text = "‹ Volver",
                    color = VerdeCarrito,
                    fontSize = 16.sp
                )
            }

            Text(
                text = "Tu carrito",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = GrafitoCarrito,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = items.sumOf { it.cantidad }.let { cantidad ->
                "$cantidad " + (if (cantidad == 1) "bolsa" else "bolsas") + " en tu carrito"
            },
            fontSize = 14.sp,
            color = GrafitoCarrito
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (items.isEmpty()) {
            // Estado vacío
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🛍️", fontSize = 56.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Tu carrito está vacío",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrafitoCarrito
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Explora el catálogo y encuentra tus favoritos.",
                    color = GrafitoCarrito
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onVolver,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdeCarrito
                    )
                ) {
                    Text("Explorar productos")
                }
            }
        } else {
            // Productos y resumen
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(color = CremaCarrito, shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp)) {
                            Text("Retiro en $tiendaRetiro · Gratis", fontWeight = FontWeight.Bold)
                            Text("$direccionRetiro · Tienda demo", style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = onCambiarRetiro) {
                                Text("Cambiar punto de retiro", color = VerdeCarrito)
                            }
                        }
                    }
                }
                items(
                    items = items,
                    key = { it.producto.id }
                ) { item ->
                    TarjetaItemCarrito(
                        item = item,
                        onAumentar = {
                            onAumentar(item.producto.id)
                        },
                        onDisminuir = {
                            onDisminuir(item.producto.id)
                        },
                        onEliminar = {
                            onEliminar(item.producto.id)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Resumen del pedido
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = CremaCarrito
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {
                            FilaPrecio(
                                etiqueta = "Subtotal",
                                precio = formatoCarrito(subtotal)
                            )

                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )

                            FilaPrecio(
                                etiqueta = "Retiro en tienda",
                                precio = "Gratis"
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = VerdeCarrito.copy(alpha = 0.4f)
                            )

                            FilaPrecio(
                                etiqueta = "Total",
                                precio = formatoCarrito(subtotal),
                                destacado = true
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onConfirmar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeCarrito
                )
            ) {
                Text(
                    text = "Confirmar pedido demo  →",
                    fontSize = 15.sp
                )
            }

            Text(
                text = "Simulación: no se realiza ningún pago real.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                fontSize = 11.sp,
                color = GrafitoCarrito
            )
        }
    }
}

@Composable
private fun TarjetaItemCarrito(
    item: ItemCarrito,
    onAumentar: () -> Unit,
    onDisminuir: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CremaCarrito
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen provisional
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(
                        Color.White,
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.producto.emoji,
                    fontSize = 34.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.producto.nombre,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = GrafitoCarrito
                )

                Text(
                    text = "${item.producto.peso} · " +
                            formatoCarrito(item.producto.precio),
                    fontSize = 11.sp,
                    color = GrafitoCarrito
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDisminuir,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("−")
                    }

                    Text(
                        text = item.cantidad.toString(),
                        modifier = Modifier.padding(horizontal = 10.dp),
                        color = GrafitoCarrito
                    )

                    OutlinedButton(
                        onClick = onAumentar,
                        enabled = item.cantidad < item.producto.stock,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("+")
                    }
                }

                TextButton(
                    onClick = onEliminar,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Eliminar",
                        color = VerdeCarrito,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = formatoCarrito(item.subtotal),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeCarrito
            )
        }
    }
}

@Composable
private fun FilaPrecio(
    etiqueta: String,
    precio: String,
    destacado: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            modifier = Modifier.weight(1f),
            fontSize = if (destacado) 16.sp else 13.sp,
            fontWeight = if (destacado) FontWeight.Bold
            else FontWeight.Normal,
            color = GrafitoCarrito
        )

        Text(
            text = precio,
            fontSize = if (destacado) 15.sp else 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (destacado) VerdeCarrito else GrafitoCarrito
        )
    }
}
