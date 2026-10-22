
package com.oyl.frutossecos.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oyl.frutossecos.model.Producto
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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

        // Escena propia de la tienda: los frutos giran y caen dentro de una bolsa.
        EscenaBolsaNatural(producto = producto)

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


@Composable
private fun EscenaBolsaNatural(producto: Producto) {
    var repeticion by remember(producto.id) { mutableIntStateOf(0) }
    val enVistaPrevia = LocalInspectionMode.current
    val movimiento = remember(producto.id, repeticion) {
        Animatable(if (enVistaPrevia) 1f else 0f)
    }
    LaunchedEffect(producto.id, repeticion) {
        if (!enVistaPrevia) {
            movimiento.animateTo(1f, tween(durationMillis = 2400, easing = LinearEasing))
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth().height(250.dp)
            .background(CremaDetalle, RoundedCornerShape(20.dp))
    ) {
        Canvas(Modifier.fillMaxSize().semantics {
            contentDescription = "Ilustración de frutos secos entrando en una bolsa"
        }) {
            val unidad = size.minDimension
            val cx = size.width / 2f
            val parteSuperior = size.height * 0.48f
            val base = size.height * 0.91f
            val ancho = unidad * 0.43f
            val avance = movimiento.value

            // Sombra elíptica y arco decorativo del recorrido.
            drawOval(VerdeDetalle.copy(alpha = 0.12f),
                topLeft = Offset(cx - ancho * 0.7f, base - 5.dp.toPx()),
                size = Size(ancho * 1.4f, 12.dp.toPx()))
            drawOval(VerdeDetalle.copy(alpha = 0.12f),
                topLeft = Offset(cx - unidad * 0.43f, size.height * 0.10f),
                size = Size(unidad * 0.86f, unidad * 0.44f),
                style = Stroke(width = 1.dp.toPx()))

            // Cada fruto recorre una órbita distinta y luego cae tras el borde de la bolsa.
            for (indice in 0 until 9) {
                val t = ((avance - indice * 0.035f) / 0.70f).coerceIn(0f, 1f)
                if (t <= 0f || t >= 1f) continue
                val giro = (indice * PI * 2.0 / 9.0 + t.coerceAtMost(0.58f) * PI * 2.0)
                val radio = unidad * (0.30f + (indice % 3) * 0.035f)
                val orbitaX = cx + cos(giro).toFloat() * radio
                val orbitaY = size.height * 0.27f + sin(giro).toFloat() * unidad * 0.15f
                val caida = ((t - 0.58f) / 0.42f).coerceIn(0f, 1f)
                val destinoX = cx + (indice % 3 - 1) * unidad * 0.06f
                val x = orbitaX + (destinoX - orbitaX) * caida
                val y = orbitaY + (parteSuperior + unidad * 0.12f - orbitaY) * caida * caida
                val centro = Offset(x, y)
                val fruto = Path().apply {
                    moveTo(x, y - 12.dp.toPx())
                    cubicTo(x + 15.dp.toPx(), y - 3.dp.toPx(),
                        x + 10.dp.toPx(), y + 10.dp.toPx(), x, y + 12.dp.toPx())
                    cubicTo(x - 10.dp.toPx(), y + 10.dp.toPx(),
                        x - 15.dp.toPx(), y - 3.dp.toPx(), x, y - 12.dp.toPx())
                    close()
                }
                rotate(degrees = (t * 540f + indice * 35f), pivot = centro) {
                    drawPath(fruto, if (indice % 3 == 0) VerdeDetalle else Color(0xFFC58B52))
                    drawLine(Color.White.copy(alpha = 0.6f),
                        Offset(x, y - 7.dp.toPx()), Offset(x, y + 7.dp.toPx()),
                        strokeWidth = 1.5.dp.toPx())
                }
            }

            // La bolsa se dibuja por encima para ocultar los frutos cuando entran.
            val bolsa = Path().apply {
                moveTo(cx - ancho * 0.45f, parteSuperior)
                lineTo(cx + ancho * 0.45f, parteSuperior)
                lineTo(cx + ancho * 0.56f, base)
                lineTo(cx - ancho * 0.56f, base)
                close()
            }
            drawPath(bolsa, Color(0xFFC58B52))
            drawLine(Color(0xFF9A6738),
                Offset(cx - ancho * 0.45f, parteSuperior),
                Offset(cx + ancho * 0.45f, parteSuperior), 5.dp.toPx())
            drawRoundRect(Color.White,
                topLeft = Offset(cx - ancho * 0.36f, parteSuperior + unidad * 0.08f),
                size = Size(ancho * 0.72f, unidad * 0.27f),
                cornerRadius = CornerRadius(10.dp.toPx()))
        }
        Text("OYL · Natural", color = VerdeDetalle, fontWeight = FontWeight.Bold,
            fontSize = 13.sp, modifier = Modifier.align(Alignment.TopStart).padding(16.dp))
        Text(producto.emoji, fontSize = 40.sp,
            modifier = Modifier.align(Alignment.Center).offset(y = 45.dp))
        TextButton(onClick = { repeticion++ }, modifier = Modifier.align(Alignment.BottomEnd)) {
            Text("↻ Repetir", color = VerdeDetalle, fontSize = 12.sp)
        }
    }
}
