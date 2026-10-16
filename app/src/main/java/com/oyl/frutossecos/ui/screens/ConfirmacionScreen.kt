
package com.oyl.frutossecos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onVolverInicio: () -> Unit
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
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Indicador visual de éxito
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(
                        VerdeConfirmacion,
                        RoundedCornerShape(55.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold
                )
            }

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
                text = "Tu pedido de demostración se ha registrado " +
                        "correctamente en esta sesión.",
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
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Esta operación es una simulación. " +
                        "No se realizó ningún pago ni se envió " +
                        "una orden a un servidor.",
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                color = GrafitoConfirmacion
            )

            Spacer(modifier = Modifier.height(30.dp))

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
