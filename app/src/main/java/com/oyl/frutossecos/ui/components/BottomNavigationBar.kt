package com.oyl.frutossecos.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun BottomNavigationBar(pantallaActual: String, onNavegar: (String) -> Unit) {
    val destinos = listOf("dashboard", "catalogo", "carrito", "pedidos", "perfil")
    val etiquetas = listOf("Inicio", "Catálogo", "Carrito", "Pedidos", "Perfil")
    val iconos = listOf(Icons.Default.Home, Icons.Default.List,
        Icons.Default.ShoppingCart, Icons.Default.List, Icons.Default.Person)
    NavigationBar(containerColor = Color.White, contentColor = Color(0xFF456747)) {
        destinos.forEachIndexed { indice, destino ->
            NavigationBarItem(
                selected = pantallaActual == destino ||
                    (destino == "catalogo" && pantallaActual == "detalle") ||
                    (destino == "pedidos" && pantallaActual == "detallePedido"),
                onClick = { if (pantallaActual != destino) onNavegar(destino) },
                icon = { Icon(iconos[indice], contentDescription = null) },
                label = { Text(etiquetas[indice]) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF456747),
                    selectedTextColor = Color(0xFF456747),
                    indicatorColor = Color(0xFFF4EBDD)
                )
            )
        }
    }
}
