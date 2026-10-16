package com.oyl.frutossecos

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.oyl.frutossecos.navegation.AppNavigation

// Colores principales de la aplicación OYL
private val VerdeOYL = Color(0xFF456747)
private val CremaOYL = Color(0xFFF4EBDD)
private val CarameloOYL = Color(0xFFC58B52)
private val GrafitoOYL = Color(0xFF292D26)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {

                // La navegación centraliza las pantallas de la aplicación.
                AppNavigation(
                    onMensaje = { mensaje ->
                        Toast.makeText(
                            this,
                            mensaje,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }
}

// Pantalla 1: bienvenida de la aplicación
@Composable
fun PantallaBienvenida(
    onLogin: () -> Unit,
    onRegistro: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CremaOYL
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 28.dp,
                    vertical = 32.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(
                modifier = Modifier.weight(1f)
            )

            // Logotipo de OYL
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = VerdeOYL,
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "OYL",
                        color = Color.White,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "FRUTOS SECOS",
                        color = CremaOYL,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            // Mensaje principal
            Text(
                text = "¡Naturalmente buenos!",
                color = VerdeOYL,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "Descubre frutos secos, semillas y mucho más.",
                color = GrafitoOYL,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(
                modifier = Modifier.height(36.dp)
            )

            // Botón para iniciar sesión
            Button(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeOYL
                )
            ) {
                Text(
                    text = "Iniciar sesión",
                    fontSize = 16.sp
                )
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            // Botón para crear una cuenta
            OutlinedButton(
                onClick = onRegistro,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = VerdeOYL
                )
            ) {
                Text(
                    text = "Crear cuenta",
                    fontSize = 16.sp
                )
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            // Texto inferior
            Text(
                text = "Alimenta tu bienestar, naturalmente.",
                color = CarameloOYL,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
