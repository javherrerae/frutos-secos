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
import androidx.compose.material.icons.filled.Person
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
fun RegisterScreen(
    onVolver: () -> Unit,
    onRegistroExitoso: (Usuario) -> Unit,
    viewModel: AuthViewModel
) {
    // Obtenemos los valores y los errores desde el ViewModel
    val estado by viewModel.registroState.collectAsState()

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

            // Campo de nombre
            OutlinedTextField(
                value = estado.nombre,
                onValueChange = { viewModel.onRegistroNombreChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre completo") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = VerdeOYL
                    )
                },
                isError = estado.errores.nombre != null,
                supportingText = {
                    estado.errores.nombre?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo de correo
            OutlinedTextField(
                value = estado.correo,
                onValueChange = { viewModel.onRegistroCorreoChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Correo electrónico") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = VerdeOYL
                    )
                },
                isError = estado.errores.correo != null,
                supportingText = {
                    estado.errores.correo?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo de contraseña
            OutlinedTextField(
                value = estado.clave,
                onValueChange = { viewModel.onRegistroClaveChange(it) },
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
                    IconButton(onClick = { viewModel.alternarVisibilidadClaveRegistro() }) {
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

            Spacer(modifier = Modifier.height(8.dp))

            // Campo para confirmar contraseña
            OutlinedTextField(
                value = estado.confirmarClave,
                onValueChange = { viewModel.onRegistroConfirmarClaveChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirmar contraseña") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = VerdeOYL
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { viewModel.alternarVisibilidadConfirmarClave() }) {
                        Icon(
                            imageVector = if (estado.confirmarClaveVisible) Icons.Default.Info else Icons.Default.Lock,
                            contentDescription = if (estado.confirmarClaveVisible) "Ocultar clave" else "Mostrar clave",
                            tint = if (estado.confirmarClaveVisible) VerdeOYL else Color.Gray
                        )
                    }
                },
                visualTransformation = if (estado.confirmarClaveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = estado.errores.confirmarClave != null,
                supportingText = {
                    estado.errores.confirmarClave?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                shape = RoundedCornerShape(14.dp)
            )

            // Mensaje si ocurre un error al registrar
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
                onClick = { viewModel.registrarUsuario(onExito = onRegistroExitoso) },
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
                    Text("Crear cuenta")
                }
            }

            TextButton(onClick = onVolver) {
                Text("Volver al inicio", color = VerdeOYL)
            }
        }
    }
}
