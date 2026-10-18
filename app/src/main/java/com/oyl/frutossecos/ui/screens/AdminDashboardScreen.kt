package com.oyl.frutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val VerdeAdmin = Color(0xFF456747)
private val CremaAdmin = Color(0xFFF4EBDD)

/** Entrada de administrador. Los permisos se comprueban además en AppNavigation. */
@Composable
fun AdminDashboardScreen(
    onAbrirProductos: () -> Unit,
    onAbrirInventario: () -> Unit,
    onAbrirPerfil: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Surface(color = CremaAdmin, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("OYL · Administración", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = VerdeAdmin)
            Text("Gestionar la tienda", style = MaterialTheme.typography.titleLarge)
            Text("Consulta productos y existencias desde el catálogo disponible. " +
                "Los cambios se confirman únicamente si el servidor responde.",
                style = MaterialTheme.typography.bodyMedium)

            Card(onClick = onAbrirProductos, shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Text("Gestión de productos", style = MaterialTheme.typography.titleLarge,
                        color = VerdeAdmin, fontWeight = FontWeight.Bold)
                    Text("Catálogo, búsqueda y formularios de producto")
                }
            }
            Card(onClick = onAbrirInventario, shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Text("Inventario y alertas", style = MaterialTheme.typography.titleLarge,
                        color = VerdeAdmin, fontWeight = FontWeight.Bold)
                    Text("Stock disponible, alertas y ajustes autorizados")
                }
            }
            OutlinedButton(onClick = onAbrirPerfil, modifier = Modifier.fillMaxWidth()) {
                Text("Mi perfil")
            }
            TextButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar sesión", color = VerdeAdmin)
            }
        }
    }
}
