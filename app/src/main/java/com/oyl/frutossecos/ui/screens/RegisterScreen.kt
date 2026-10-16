
package com.oyl.frutossecos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val VerdeOYL = Color(0xFF456747)
private val CremaOYL = Color(0xFFF4EBDD)
private val GrafitoOYL = Color(0xFF292D26)

@Composable
fun RegisterScreen(
    onVolver: () -> Unit,
    onRegistroExitoso: () -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var confirmarClave by rememberSaveable { mutableStateOf("") }

    val correoValido = android.util.Patterns.EMAIL_ADDRESS
        .matcher(correo.trim()).matches()

    val nombreError = nombre.isNotBlank() && nombre.trim().length < 2
    val correoError = correo.isNotBlank() && !correoValido
    val claveError = clave.isNotBlank() && clave.length < 6
    val confirmarError = confirmarClave.isNotBlank() &&
            confirmarClave != clave

    val formularioValido =
        nombre.trim().length >= 2 &&
                correoValido &&
                clave.length >= 6 &&
                confirmarClave == clave

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CremaOYL
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crear cuenta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeOYL
            )

            Text(
                text = "Únete a la comunidad OYL",
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
                color = GrafitoOYL
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre completo") },
                singleLine = true,
                isError = nombreError,
                supportingText = {
                    if (nombreError) Text("Escribe al menos 2 caracteres.")
                },
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Correo electrónico") },
                singleLine = true,
                isError = correoError,
                supportingText = {
                    if (correoError) Text("Ingresa un correo válido.")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = clave,
                onValueChange = { clave = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Contraseña") },
                singleLine = true,
                isError = claveError,
                supportingText = {
                    if (claveError) Text("Usa al menos 6 caracteres.")
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmarClave,
                onValueChange = { confirmarClave = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirmar contraseña") },
                singleLine = true,
                isError = confirmarError,
                supportingText = {
                    if (confirmarError) Text("Las contraseñas no coinciden.")
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onRegistroExitoso,
                enabled = formularioValido,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeOYL
                )
            ) {
                Text("Crear cuenta")
            }

            TextButton(onClick = onVolver) {
                Text("Volver al inicio", color = VerdeOYL)
            }
        }
    }
}
