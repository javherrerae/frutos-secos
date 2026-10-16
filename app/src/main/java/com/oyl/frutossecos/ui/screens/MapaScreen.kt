package com.oyl.frutossecos.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.view.MotionEvent
import android.webkit.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.oyl.frutossecos.model.TiendaDemo
import com.oyl.frutossecos.model.TiendasDemo

@Composable
fun MapaScreen(tiendaActualId: String, onVolver: () -> Unit,
    onSeleccionar: (String) -> Unit) {
    var seleccion by rememberSaveable { mutableStateOf(tiendaActualId) }
    val tienda = TiendasDemo.buscar(seleccion)
    val verde = Color(0xFF456747)
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onVolver) { Text("Volver", color = verde) }
            Text("Tiendas y retiro", style = MaterialTheme.typography.headlineSmall)
            Text("Locales, direcciones y ubicaciones de demostración.",
                style = MaterialTheme.typography.bodySmall)
            MapaOpenStreetMap(tienda, Modifier.fillMaxWidth().height(230.dp)
                .clip(RoundedCornerShape(16.dp)))
            Text("Elige dónde retirar · Gratis", style = MaterialTheme.typography.titleMedium)
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(TiendasDemo.todas, key = { it.id }) { punto ->
                    Surface(onClick = { seleccion = punto.id },
                        shape = RoundedCornerShape(14.dp),
                        color = if (seleccion == punto.id) Color(0xFFF4EBDD) else Color(0xFFF6F6F3)) {
                        Row(Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = seleccion == punto.id, onClick = null)
                            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                                Text(punto.nombre, style = MaterialTheme.typography.titleSmall)
                                Text("${punto.direccion} · ${punto.comuna}",
                                    style = MaterialTheme.typography.bodySmall)
                                Text(punto.horario, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            Button(onClick = { onSeleccionar(tienda.id) }, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = verde)) {
                Text("Retirar en ${tienda.comuna}")
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
private fun MapaOpenStreetMap(tienda: TiendaDemo, modifier: Modifier = Modifier) {
    var intento by remember { mutableStateOf(0) }
    var cargando by remember(tienda.id, intento) { mutableStateOf(true) }
    var fallo by remember(tienda.id, intento) { mutableStateOf(false) }
    val url = remember(tienda.id) {
        Uri.parse("https://www.openstreetmap.org/export/embed.html").buildUpon()
            .appendQueryParameter("bbox", "${tienda.longitud - 0.015},${tienda.latitud - 0.01},${tienda.longitud + 0.015},${tienda.latitud + 0.01}")
            .appendQueryParameter("layer", "mapnik")
            .appendQueryParameter("marker", "${tienda.latitud},${tienda.longitud}")
            .build().toString()
    }
    Box(modifier) {
        key(tienda.id, intento) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                val uri = request.url
                                return uri.scheme != "https" || uri.host != "www.openstreetmap.org" ||
                                    uri.path != "/export/embed.html"
                            }
                            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                                cargando = true
                            }
                            override fun onPageFinished(view: WebView, url: String?) { cargando = false }
                            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                                if (request.isForMainFrame) { fallo = true; cargando = false }
                            }
                            override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, response: WebResourceResponse) {
                                if (request.isForMainFrame) { fallo = true; cargando = false }
                            }
                        }
                        setOnTouchListener { view, event ->
                            view.parent?.requestDisallowInterceptTouchEvent(
                                event.action != MotionEvent.ACTION_UP && event.action != MotionEvent.ACTION_CANCEL)
                            false
                        }
                        loadUrl(url)
                    }
                },
                onReset = null,
                onRelease = { it.stopLoading(); it.destroy() }
            )
        }
        if (cargando && !fallo) {
            Surface(Modifier.align(Alignment.TopCenter), color = Color.White) {
                Text("Cargando mapa…", Modifier.padding(8.dp))
            }
        }
        if (fallo) {
            Surface(Modifier.fillMaxSize(), color = Color(0xFFF4EBDD)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No se pudo cargar el mapa. Revisa la conexión a internet.")
                    TextButton(onClick = { intento++ }) { Text("Reintentar") }
                    Text("Puedes elegir el punto de retiro en la lista.",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
