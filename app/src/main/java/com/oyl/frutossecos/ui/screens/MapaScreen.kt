package com.oyl.frutossecos.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.oyl.frutossecos.model.TiendaDemo
import com.oyl.frutossecos.model.TiendasDemo
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/** Puntos geográficos reales de referencia; OYL no posee locales de retiro. */
@Composable
fun MapaScreen(
    tiendaActualId: String,
    onVolver: () -> Unit,
    onSeleccionar: (String) -> Unit
) {
    var seleccion by rememberSaveable(tiendaActualId) { mutableStateOf(tiendaActualId) }
    val tienda = TiendasDemo.buscar(seleccion)
    val verde = Color(0xFF456747)
    val contexto = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(onClick = onVolver) { Text("Volver", color = verde) }
            Text("Mapa de ubicaciones", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Lugares reales de referencia. No son sucursales ni puntos de retiro de OYL.",
                style = MaterialTheme.typography.bodySmall
            )

            MapaNativoOpenStreetMap(
                tienda = tienda,
                modifier = Modifier.fillMaxWidth().height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            TextButton(onClick = {
                val enlace = "https://www.openstreetmap.org/?mlat=${tienda.latitud}" +
                    "&mlon=${tienda.longitud}#map=16/${tienda.latitud}/${tienda.longitud}"
                contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(enlace)))
            }) {
                Text("Abrir ubicación en el navegador", color = verde)
            }

            Text("Selecciona una ubicación de referencia", style = MaterialTheme.typography.titleMedium)
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TiendasDemo.todas, key = { it.id }) { punto ->
                    Surface(
                        onClick = { seleccion = punto.id },
                        shape = RoundedCornerShape(14.dp),
                        color = if (seleccion == punto.id) Color(0xFFF4EBDD)
                        else Color(0xFFF6F6F3)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = seleccion == punto.id, onClick = null)
                            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                                Text(punto.nombre, style = MaterialTheme.typography.titleSmall)
                                Text("${punto.direccion} · ${punto.comuna}", style = MaterialTheme.typography.bodySmall)
                                Text(punto.horario, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            Button(
                onClick = { onSeleccionar(tienda.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = verde)
            ) {
                Text("Seleccionar ${tienda.comuna}")
            }
        }
    }
}

/** Mapa Android nativo: evita el WebView que quedaba en blanco en algunos celulares. */
@Composable
private fun MapaNativoOpenStreetMap(tienda: TiendaDemo, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    val mapa = remember(contexto) {
        Configuration.getInstance().userAgentValue = contexto.packageName
        MapView(contexto).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setUseDataConnection(true)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
        }
    }

    DisposableEffect(mapa) {
        mapa.onResume()
        onDispose {
            mapa.onPause()
            mapa.onDetach()
        }
    }

    LaunchedEffect(mapa, tienda.id) {
        val punto = GeoPoint(tienda.latitud, tienda.longitud)
        mapa.overlays.clear()
        mapa.overlays.add(Marker(mapa).apply {
            position = punto
            title = tienda.nombre
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        })
        mapa.controller.setCenter(punto)
        mapa.invalidate()
    }

    AndroidView(factory = { mapa }, modifier = modifier)
}
