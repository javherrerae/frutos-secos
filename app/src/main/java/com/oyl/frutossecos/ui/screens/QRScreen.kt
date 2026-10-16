package com.oyl.frutossecos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.oyl.frutossecos.data.ProductosDemo
import com.oyl.frutossecos.model.Producto

/** Los QR de prueba contienen OYL:1, OYL:2, ... OYL:6. */
@Composable
fun QRScreen(onVolver: () -> Unit, onAbrirProducto: (Producto) -> Unit) {
    val context = LocalContext.current
    val verde = Color(0xFF456747)
    var resultado by remember { mutableStateOf<String?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }
    var escaneando by remember { mutableStateOf(false) }
    val producto = remember(resultado) {
        val id = resultado?.trim()?.takeIf { it.startsWith("OYL:", ignoreCase = true) }
            ?.substringAfter(':')?.toIntOrNull()
        ProductosDemo.productos.find { it.id == id }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onVolver, modifier = Modifier.align(Alignment.Start)) { Text("← Volver") }
        Text("Escanear producto", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold, color = verde)
        Text("Apunta la cámara al código QR de un producto OYL de prueba.")
        Button(
            enabled = !escaneando,
            colors = ButtonDefaults.buttonColors(containerColor = verde),
            onClick = {
                escaneando = true
                aviso = null
                val opciones = GmsBarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .enableAutoZoom()
                    .build()
                GmsBarcodeScanning.getClient(context, opciones).startScan()
                    .addOnSuccessListener { codigo ->
                        resultado = codigo.rawValue
                        aviso = if (resultado.isNullOrBlank()) "El QR no contiene texto." else null
                    }
                    .addOnCanceledListener { aviso = "Escaneo cancelado." }
                    .addOnFailureListener { error ->
                        aviso = "No se pudo abrir el escáner. Verifica Google Play Services y la cámara."
                    }
                    .addOnCompleteListener { escaneando = false }
            }
        ) { Text(if (escaneando) "Abriendo cámara…" else "Abrir cámara y escanear QR") }

        if (resultado != null) {
            Surface(color = Color(0xFFF4EBDD), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (producto != null) {
                        Text(producto.emoji + "  " + producto.nombre,
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(producto.descripcion)
                        Text("Presentación: ${producto.peso}")
                        Text("Precio: $${producto.precio} CLP · Stock: ${producto.stock}")
                        Button(onClick = { onAbrirProducto(producto) }) {
                            Text("Ver detalle del producto")
                        }
                    } else if (!resultado.isNullOrBlank()) {
                        Text("Código no reconocido", fontWeight = FontWeight.Bold)
                        Text("Este código QR no corresponde a un producto del catálogo.")
                        Text("Contenido leído: ${resultado?.take(120)}")
                    }
                }
            }
        }
        if (aviso != null) Text(aviso!!, color = MaterialTheme.colorScheme.error)
        Text(
            "Actualmente se reconocen códigos de productos OYL:1 a OYL:6.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
