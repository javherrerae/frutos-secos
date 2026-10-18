package com.oyl.frutossecos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oyl.frutossecos.Usuario
import com.oyl.frutossecos.viewmodel.AuthViewModel

private val VerdeOYL = Color(0xFF456747)
private val CremaOYL = Color(0xFFF4EBDD)
private val GrafitoOYL = Color(0xFF292D26)

@Composable
fun LoginScreen(
    onVolver: () -> Unit,
    onIngresarExitoso: (Usuario) -> Unit,
    viewModel: AuthViewModel
) {
    // Obtenemos los valores y los errores directamente del ViewModel
    val estado by viewModel.loginState.collectAsState()

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
                text = "OYL",
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = VerdeOYL
            )

            Text(
                text = "¡Qué bueno tenerte de vuelta!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = GrafitoOYL
            )

            Text(
                text = "Inicia sesión con tu cuenta de OYL Frutos Secos",
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                color = GrafitoOYL
            )

            // Input para usuario o correo
            OutlinedTextField(
                value = estado.identificador,
                onValueChange = { viewModel.onLoginIdentificadorChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Correo electrónico o alias") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = VerdeOYL
                    )
                },
                isError = estado.errores.identificador != null,
                supportingText = {
                    estado.errores.identificador?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Input para la contraseña con botón para ver u ocultar
            OutlinedTextField(
                value = estado.clave,
                onValueChange = { viewModel.onLoginClaveChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Contraseña") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = VerdeOYL
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { viewModel.alternarVisibilidadClaveLogin() }) {
                        Icon(
                            imageVector = if (estado.claveVisible) Icons.Default.Info else Icons.Default.Lock,
                            contentDescription = if (estado.claveVisible) "Ocultar clave" else "Mostrar clave",
                            tint = if (estado.claveVisible) VerdeOYL else Color.Gray
                        )
                    }
                },
                visualTransformation = if (estado.claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = estado.errores.clave != null,
                supportingText = {
                    estado.errores.clave?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                shape = RoundedCornerShape(14.dp)
            )

            // Mensaje en caso de credenciales incorrectas
            if (estado.mensajeErrorGeneral != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = estado.mensajeErrorGeneral!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { viewModel.iniciarSesion(onExito = onIngresarExitoso) },
                enabled = estado.esValido && !estado.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeOYL
                )
            ) {
                if (estado.cargando) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Iniciar sesión")
                }
            }

            TextButton(onClick = onVolver) {
                Text("Volver al inicio", color = VerdeOYL)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "💡 Para pruebas: admin@admin.cl / admin123",
                fontSize = 12.sp,
                color = GrafitoOYL.copy(alpha = 0.7f)
            )
        }
    }
}
