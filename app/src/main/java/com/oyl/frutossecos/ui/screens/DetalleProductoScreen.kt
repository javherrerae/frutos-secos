
package com.oyl.frutossecos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oyl.frutossecos.model.Producto
import java.text.NumberFormat
import java.util.Locale

private val VerdeDetalle = Color(0xFF456747)
private val CremaDetalle = Color(0xFFF4EBDD)
private val GrafitoDetalle = Color(0xFF292D26)

private fun precioDetalle(precio: Int): String {
    return "$" + NumberFormat.getNumberInstance(
        Locale("es", "CL")
    ).format(precio) + " CLP"
}

@Composable
fun DetalleProductoScreen(
    producto: Producto,
    onVolver: () -> Unit,
    onAgregarAlCarrito: (Producto, Int) -> Unit
) {
    var cantidad by remember(producto.id) {
        mutableIntStateOf(1)
    }

    var favorito by remember(producto.id) {
        mutableStateOf(false)
    }

    var mensaje by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onVolver) {
                Text(
                    text = "‹ Volver",
                    color = VerdeDetalle,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(
                onClick = { favorito = !favorito }
            ) {
                Text(
                    text = if (favorito) "♥" else "♡",
                    fontSize = 28.sp,
                    color = VerdeDetalle
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Imagen provisional
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .background(
                    CremaDetalle,
                    RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = producto.emoji,
                fontSize = 100.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Categoría
        Surface(
            color = CremaDetalle,
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = producto.categoria,
                color = VerdeDetalle,
                fontSize = 12.sp,
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 7.dp
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Nombre y precio
        Text(
            text = producto.nombre,
            fontSize = 27.sp,
            fontWeight = FontWeight.Medium,
            color = GrafitoDetalle
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = producto.peso,
            fontSize = 14.sp,
            color = GrafitoDetalle
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = precioDetalle(producto.precio),
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = VerdeDetalle
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = producto.descripcion,
            fontSize = 15.sp,
            lineHeight = 23.sp,
            color = GrafitoDetalle
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "🌿  Producto natural",
            fontSize = 13.sp,
            color = VerdeDetalle
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Disponibilidad y cantidad
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Cantidad",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = GrafitoDetalle
                )

                Text(
                    text = if (producto.stock > 0)
                        "${producto.stock} bolsas disponibles"
                    else "Producto agotado",
                    fontSize = 12.sp,
                    color = if (producto.stock > 0)
                        VerdeDetalle else Color.Red
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        if (cantidad > 1) cantidad--
                    },
                    enabled = cantidad > 1,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(42.dp)
                ) {
                    Text("−", fontSize = 20.sp)
                }

                Text(
                    text = cantidad.toString(),
                    modifier = Modifier.padding(horizontal = 18.dp),
                    fontSize = 16.sp,
                    color = GrafitoDetalle
                )

                OutlinedButton(
                    onClick = {
                        if (cantidad < producto.stock) {
                            cantidad++
                        }
                    },
                    enabled = cantidad < producto.stock,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(42.dp)
                ) {
                    Text("+", fontSize = 20.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Agregar al carrito
        Button(
            onClick = {
                onAgregarAlCarrito(producto, cantidad)
            },
            enabled = producto.stock > 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdeDetalle
            )
        ) {
            Text(
                text = "Agregar $cantidad bolsa(s) al carrito",
                fontSize = 14.sp
            )
        }

        mensaje?.let {
            Text(
                text = it,
                color = VerdeDetalle,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Conservar en un lugar fresco y seco. " +
                    "Revisa el envase para conocer los ingredientes " +
                    "y posibles alérgenos.",
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = GrafitoDetalle
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}
