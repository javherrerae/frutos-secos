
package com.oyl.frutossecos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oyl.frutossecos.data.ProductosDemo
import com.oyl.frutossecos.model.Producto
import java.text.NumberFormat
import java.util.Locale

private val VerdeCatalogo = Color(0xFF456747)
private val CremaCatalogo = Color(0xFFF4EBDD)
private val GrafitoCatalogo = Color(0xFF292D26)
private val GrisCatalogo = Color(0xFF777777)

private fun formatoPrecio(precio: Int): String {
    return NumberFormat.getNumberInstance(
        Locale("es", "CL")
    ).format(precio) + " CLP"
}

@Composable
fun CatalogoScreen(
    onVolver: () -> Unit,
    onAbrirProducto: (Producto) -> Unit,
    onAbrirCarrito: () -> Unit,
    onAbrirQR: () -> Unit,
    productos: List<Producto> = ProductosDemo.productos,
    cargando: Boolean = false,
    mensajeEstado: String? = null,
    onRefrescar: () -> Unit = {}
) {
    var busqueda by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember {
        mutableStateOf("Todos")
    }

    val categorias = listOf(
        "Todos",
        "Frutos secos",
        "Semillas",
        "Frutas deshidratadas"
    )

    val listaBase = if (productos.isNotEmpty()) productos else ProductosDemo.productos
    val productosFiltrados = listaBase.filter {
        val coincideBusqueda =
            it.nombre.contains(busqueda, ignoreCase = true) ||
                    it.categoria.contains(busqueda, ignoreCase = true)

        val coincideCategoria =
            categoriaSeleccionada == "Todos" ||
                    it.categoria == categoriaSeleccionada

        coincideBusqueda && coincideCategoria
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        // Encabezado de la pantalla
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "‹",
                fontSize = 32.sp,
                color = VerdeCatalogo,
                modifier = Modifier.clickable {
                    onVolver()
                }
            )

            Text(
                text = "Catálogo",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = GrafitoCatalogo,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            )

            TextButton(onClick = onRefrescar) {
                Text(if (cargando) "⏳" else "🔄", fontSize = 18.sp)
            }

            TextButton(onClick = onAbrirQR) {
                Text("Escanear QR")
            }

            TextButton(onClick = onAbrirCarrito) {
                Text(
                    text = "🛍",
                    fontSize = 22.sp
                )
            }
        }

        if (mensajeEstado != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = CremaCatalogo,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = mensajeEstado,
                    fontSize = 11.sp,
                    color = VerdeCatalogo,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Buscar productos",
            fontSize = 13.sp,
            color = GrafitoCatalogo
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de búsqueda
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    CremaCatalogo,
                    RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 14.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = GrafitoCatalogo
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (busqueda.isEmpty()) {
                            Text(
                                text = "Almendras, semillas, fruta...",
                                color = GrisCatalogo,
                                fontSize = 13.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Text("⌕", fontSize = 24.sp, color = VerdeCatalogo)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filtros de categoría
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categorias.forEach { categoria ->
                val seleccionada =
                    categoriaSeleccionada == categoria

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (seleccionada) VerdeCatalogo
                            else CremaCatalogo,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            categoriaSeleccionada = categoria
                        }
                        .padding(vertical = 12.dp, horizontal = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (categoria) {
                            "Frutos secos" -> "Frutos\nsecos"
                            "Frutas deshidratadas" -> "Frutas"
                            else -> categoria
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (seleccionada) Color.White
                        else GrafitoCatalogo
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Título y cantidad de resultados
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Natural para cada día",
                fontSize = 19.sp,
                color = GrafitoCatalogo,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "${productosFiltrados.size} productos",
                fontSize = 11.sp,
                color = VerdeCatalogo
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Productos en dos columnas
        productosFiltrados.chunked(2).forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                fila.forEach { producto ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onAbrirProducto(producto)
                            }
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CremaCatalogo
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp)
                        ) {
                            // Imagen provisional del producto
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(105.dp)
                                    .background(
                                        Color(0xFFE8DCC8),
                                        RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = producto.emoji,
                                    fontSize = 48.sp
                                )
                            }

                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )

                            Text(
                                text = producto.nombre,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GrafitoCatalogo
                            )

                            Spacer(
                                modifier = Modifier.height(5.dp)
                            )

                            Text(
                                text = producto.peso,
                                fontSize = 11.sp,
                                color = GrisCatalogo
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                text = formatoPrecio(producto.precio),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerdeCatalogo
                            )

                            Text(
                                text = if (producto.stock > 0)
                                    "Stock: ${producto.stock}"
                                else "Agotado",
                                fontSize = 10.sp,
                                color = if (producto.stock > 0)
                                    VerdeCatalogo else Color.Red
                            )
                        }
                    }
                }

                // Mantiene el ancho cuando queda un solo producto
                if (fila.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        if (productosFiltrados.isEmpty()) {
            Text(
                text = "No encontramos productos con esa búsqueda.",
                color = GrisCatalogo,
                modifier = Modifier.padding(vertical = 24.dp)
            )
        }
    }
}
