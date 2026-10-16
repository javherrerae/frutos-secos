
package com.oyl.frutossecos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val VerdeOYL = Color(0xFF456747)
private val CremaOYL = Color(0xFFF4EBDD)
private val CarameloOYL = Color(0xFFC58B52)
private val GrafitoOYL = Color(0xFF292D26)

private data class ProductoDestacado(
    val nombre: String,
    val descripcion: String,
    val precio: Int,
    val emoji: String
)

private val productosDemo = listOf(
    ProductoDestacado(
        "Almendras",
        "Bolsa de 250 g",
        3490,
        "🥜"
    ),
    ProductoDestacado(
        "Nueces",
        "Bolsa de 250 g",
        4290,
        "🌰"
    ),
    ProductoDestacado(
        "Mix natural",
        "Bolsa de 300 g",
        3990,
        "🥣"
    )
)

@Composable
fun DashboardScreen(
    onAbrirCatalogo: () -> Unit,
    onAbrirCarrito: () -> Unit,
    onAbrirPerfil: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CremaOYL
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "OYL",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = VerdeOYL
                    )
                    Text(
                        text = "FRUTOS SECOS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = GrafitoOYL
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onAbrirCarrito) {
                        Text("🛒 Carrito", color = VerdeOYL)
                    }
                    TextButton(onClick = onAbrirPerfil) {
                        Text("👤", fontSize = 22.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¡Hola! 👋",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = GrafitoOYL
            )

            Text(
                text = "¿Qué se te antoja hoy?",
                fontSize = 16.sp,
                color = GrafitoOYL,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = VerdeOYL
                )
            ) {
                Column(
                    modifier = Modifier.padding(22.dp)
                ) {
                    Text(
                        text = "NATURALMENTE BUENOS",
                        color = CremaOYL,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Pequeños snacks,\ngrande bienestar",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 31.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onAbrirCatalogo,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CremaOYL,
                            contentColor = VerdeOYL
                        )
                    ) {
                        Text("Explorar productos")
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Categorías",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = GrafitoOYL
            )

            Spacer(modifier = Modifier.height(14.dp))

            val categorias = listOf(
                "🥜" to "Frutos secos",
                "🌻" to "Semillas",
                "🍇" to "Frutas deshidratadas",
                "🥣" to "Mix saludables"
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categorias) { categoria ->
                    Column(
                        modifier = Modifier
                            .width(112.dp)
                            .background(
                                Color.White,
                                RoundedCornerShape(18.dp)
                            )
                            .clickable(onClick = onAbrirCatalogo)
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = categoria.first,
                            fontSize = 30.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = categoria.second,
                            fontSize = 12.sp,
                            color = GrafitoOYL
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Productos destacados",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrafitoOYL
                )

                TextButton(onClick = onAbrirCatalogo) {
                    Text("Ver todos", color = VerdeOYL)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            productosDemo.forEach { producto ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable(onClick = onAbrirCatalogo),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(66.dp),
                            shape = CircleShape,
                            color = CremaOYL
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = producto.emoji,
                                    fontSize = 30.sp
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 14.dp)
                        ) {
                            Text(
                                text = producto.nombre,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = GrafitoOYL
                            )
                            Text(
                                text = producto.descripcion,
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "$${producto.precio} CLP",
                                fontWeight = FontWeight.Bold,
                                color = VerdeOYL,
                                modifier = Modifier.padding(top = 5.dp)
                            )
                        }

                        Text(
                            text = "›",
                            fontSize = 30.sp,
                            color = CarameloOYL
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onCerrarSesion,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Cerrar sesión", color = VerdeOYL)
            }
        }
    }
}
