package com.oyl.frutossecos.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun PerfilScreen(
    nombreGuardado: String,
    correoGuardado: String,
    telefonoGuardado: String,
    avisosGuardados: Boolean,
    onGuardar: (String, String, String, Boolean) -> Unit,
    onCerrarSesion: () -> Unit,
    tiendaRetiro: String = "Providencia",
    onAbrirMapa: () -> Unit = {}
) {
    var nombre by rememberSaveable(nombreGuardado) { mutableStateOf(nombreGuardado) }
    var correo by rememberSaveable(correoGuardado) { mutableStateOf(correoGuardado) }
    var telefono by rememberSaveable(telefonoGuardado) { mutableStateOf(telefonoGuardado) }
    var avisos by rememberSaveable(avisosGuardados) { mutableStateOf(avisosGuardados) }
    var intentoGuardar by rememberSaveable { mutableStateOf(false) }
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val archivoAvatar = remember(context) { File(context.filesDir, "avatar_perfil_demo.jpg") }
    var versionAvatar by remember { mutableIntStateOf(0) }
    var mensajeAvatar by remember { mutableStateOf<String?>(null) }
    val avatar = remember(versionAvatar) { cargarAvatar(archivoAvatar) }

    // GetContent permite elegir una foto sin solicitar permisos amplios de galería.
    val selectorFoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val imagen = runCatching {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    // Se lee y reduce la foto para evitar imágenes enormes en memoria.
                    val bytes = stream.readBytes()
                    val opcionesMedida = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opcionesMedida)
                    val mayorLado = maxOf(opcionesMedida.outWidth, opcionesMedida.outHeight)
                    val factor = generateSequence(1) { it * 2 }.first { mayorLado / it <= 512 }
                    val opciones = BitmapFactory.Options().apply { inSampleSize = factor }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opciones)
                }
            }.getOrNull()
            if (imagen != null) {
                val guardado = runCatching {
                    archivoAvatar.outputStream().use { imagen.compress(Bitmap.CompressFormat.JPEG, 85, it) }
                }.getOrDefault(false)
                imagen.recycle()
                if (guardado) {
                    versionAvatar++
                    mensajeAvatar = "Foto de perfil actualizada en este dispositivo."
                } else {
                    mensajeAvatar = "No se pudo guardar la fotografía."
                }
            } else {
                mensajeAvatar = "No se pudo abrir esa imagen. Prueba con otra fotografía."
            }
        }
    }

    val errorNombre = nombre.trim().length < 2
    val errorCorreo = !Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()
    val errorTelefono = telefono.isNotBlank() &&
        (!telefono.all { it.isDigit() || it in "+ -()" } ||
            telefono.count { it.isDigit() } !in 9..15)
    val verde = Color(0xFF456747)
    val crema = Color(0xFFF4EBDD)

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier.fillMaxSize().imePadding()
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Mi perfil", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = verde)
            Column(modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(76.dp).background(crema, CircleShape),
                    contentAlignment = Alignment.Center) {
                    if (avatar != null) {
                        Image(
                            bitmap = avatar.asImageBitmap(),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val iniciales = nombre.trim().split(Regex("\\s+"))
                            .filter { it.isNotBlank() }.take(2)
                            .joinToString("") { it.take(1).uppercase() }.ifBlank { "OYL" }
                        Text(iniciales, style = MaterialTheme.typography.headlineMedium, color = verde)
                    }
                }
                Text(nombreGuardado.ifBlank { "Cliente OYL" },
                    style = MaterialTheme.typography.titleLarge)
                Text("Cuenta de cliente · Demo", style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { selectorFoto.launch("image/*") }) {
                    Text(if (avatar == null) "Agregar foto de perfil" else "Cambiar foto de perfil")
                }
                if (avatar != null) {
                    TextButton(onClick = {
                        archivoAvatar.delete()
                        versionAvatar++
                        mensajeAvatar = "Foto eliminada."
                    }) { Text("Quitar foto") }
                }
                mensajeAvatar?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
            Text("Datos personales", fontWeight = FontWeight.Bold)
            OutlinedTextField(value = nombre, onValueChange = { nombre = it },
                modifier = Modifier.fillMaxWidth(), label = { Text("Nombre y apellido") },
                singleLine = true, isError = intentoGuardar && errorNombre,
                supportingText = { if (intentoGuardar && errorNombre) Text("Escribe al menos 2 caracteres.") })
            OutlinedTextField(value = correo, onValueChange = { correo = it },
                modifier = Modifier.fillMaxWidth(), label = { Text("Correo electrónico") },
                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = intentoGuardar && errorCorreo,
                supportingText = { if (intentoGuardar && errorCorreo) Text("Ingresa un correo válido.") })
            OutlinedTextField(value = telefono, onValueChange = { telefono = it },
                modifier = Modifier.fillMaxWidth(), label = { Text("Teléfono (opcional)") },
                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = intentoGuardar && errorTelefono,
                supportingText = { if (intentoGuardar && errorTelefono) Text("Ingresa un teléfono de 9 a 15 dígitos.") })
            Text("Preferencias", fontWeight = FontWeight.Bold)
            Surface(color = crema, shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tienda de retiro: $tiendaRetiro")
                    TextButton(onClick = onAbrirMapa) {
                        Text("Ver mapa y cambiar tienda", color = verde)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Recibir avisos", Modifier.weight(1f))
                        Switch(checked = avisos, onCheckedChange = { avisos = it })
                    }
                    Text("Español · Peso chileno (CLP)")
                    Text("Preferencia de demostración. Todavía no activa notificaciones del teléfono.",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            Button(onClick = {
                intentoGuardar = true
                if (!errorNombre && !errorCorreo && !errorTelefono) {
                    onGuardar(nombre.trim(), correo.trim(), telefono.trim(), avisos)
                    intentoGuardar = false
                }
            }, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = verde)) {
                Text("Guardar cambios · Demo")
            }
            OutlinedButton(onClick = { confirmarSalida = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar sesión", color = verde)
            }
            Text("La foto se guarda solo en este dispositivo. Los demás datos se conservan durante la sesión de demostración.",
                style = MaterialTheme.typography.bodySmall)
        }
    }
    if (confirmarSalida) {
        AlertDialog(onDismissRequest = { confirmarSalida = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Se borrarán el perfil, la fotografía, el carrito y el historial de pedidos de esta sesión demo.") },
            confirmButton = { TextButton(onClick = {
                confirmarSalida = false
                archivoAvatar.delete()
                onCerrarSesion()
            }) { Text("Cerrar sesión") } },
            dismissButton = { TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") } })
    }
}

private fun cargarAvatar(archivo: File): Bitmap? =
    if (archivo.exists()) runCatching { BitmapFactory.decodeFile(archivo.absolutePath) }.getOrNull()
    else null
