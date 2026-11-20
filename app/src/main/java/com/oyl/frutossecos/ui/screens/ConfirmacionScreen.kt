
package com.oyl.frutossecos.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

private val VerdeConfirmacion = Color(0xFF456747)
private val CremaConfirmacion = Color(0xFFF4EBDD)
private val GrafitoConfirmacion = Color(0xFF292D26)

@Composable
fun ConfirmacionScreen(
    total: Int,
    codigoPedido: String? = null,
    mensajeServidor: String? = null,
    onVolverInicio: () -> Unit,
    onVerPedido: (() -> Unit)? = null
) {
    val precio = "$" + NumberFormat.getNumberInstance(
        Locale("es", "CL")
    ).format(total) + " CLP"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CremaConfirmacion
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Se anima al entrar a la confirmación; no se repite en bucle.
            IndicadorPedidoAnimado(codigoPedido = codigoPedido)

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "¡Pedido confirmado!",
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeConfirmacion,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Gracias por elegir OYL.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = GrafitoConfirmacion
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (!codigoPedido.isNullOrBlank())
                    "Tu orden $codigoPedido se ha registrado exitosamente en el sistema."
                else
                    "Tu pedido se ha procesado correctamente en el sistema.",
                fontSize = 15.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center,
                color = GrafitoConfirmacion
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Resumen del pedido
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!codigoPedido.isNullOrBlank()) {
                        Text(
                            text = "Código de seguimiento",
                            fontSize = 12.sp,
                            color = GrafitoConfirmacion
                        )
                        Text(
                            text = codigoPedido,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = VerdeConfirmacion
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Text(
                        text = "Total de la compra",
                        fontSize = 14.sp,
                        color = GrafitoConfirmacion
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = precio,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = VerdeConfirmacion
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Estado inicial: Pendiente de Validación",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GrafitoConfirmacion
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = mensajeServidor ?: "Esta operación es una simulación. No se realizó ningún pago ni se enviaron cobros bancarios.",
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                color = GrafitoConfirmacion
            )

            Spacer(modifier = Modifier.height(30.dp))

            if (onVerPedido != null) {
                Button(onClick = onVerPedido, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeConfirmacion)) {
                    Text("Ver mi pedido")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(
                onClick = onVolverInicio,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeConfirmacion
                )
            ) {
                Text(
                    text = "Volver al inicio",
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun IndicadorPedidoAnimado(codigoPedido: String?) {
    // La vista previa muestra el resultado final. Un pedido distinto inicia otra animación.
    val enVistaPrevia = LocalInspectionMode.current
    val progreso = remember(codigoPedido) {
        Animatable(if (enVistaPrevia) 1f else 0f)
    }

    LaunchedEffect(codigoPedido) {
        if (!enVistaPrevia) {
            progreso.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
            )
        }
    }

    Canvas(
        modifier = Modifier
            .size(110.dp)
            .semantics { contentDescription = "Pedido registrado" }
            .graphicsLayer {
                // El círculo aparece primero, sin desplazar el resto de la pantalla.
                val entrada = (progreso.value / 0.35f).coerceIn(0f, 1f)
                alpha = entrada
                scaleX = 0.8f + 0.2f * entrada
                scaleY = scaleX
            }
    ) {
        drawCircle(color = VerdeConfirmacion)

        val trazo = ((progreso.value - 0.25f) / 0.75f).coerceIn(0f, 1f)
        if (trazo > 0f) {
            val marca = Path().apply {
                moveTo(size.width * 0.28f, size.height * 0.51f)
                lineTo(size.width * 0.44f, size.height * 0.67f)
                lineTo(size.width * 0.73f, size.height * 0.35f)
            }
            val medida = PathMeasure().apply { setPath(marca, false) }
            val segmento = Path()
            medida.getSegment(0f, medida.length * trazo, segmento, true)
            drawPath(
                path = segmento,
                color = Color.White,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}
