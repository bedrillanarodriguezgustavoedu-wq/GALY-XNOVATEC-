package com.galyxnovatec.tienda.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.data.manager.AuthManager
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDataScreen(alVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val user = AuthManager.currentUser

    var nombre by remember { mutableStateOf(user?.nombre ?: "") }
    var telefono by remember { mutableStateOf(user?.telefono ?: "") }
    var direccion by remember { mutableStateOf(user?.direccion ?: "") }
    var password by remember { mutableStateOf("") }
    
    val isLoading = AuthManager.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Datos", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Información Personal", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            
            UserDataField("Nombre Completo", nombre, Icons.Default.Person) { nombre = it }
            UserDataField("Email (No editable)", user?.email ?: "", Icons.Default.Email, enabled = false) {}
            UserDataField("Teléfono", telefono, Icons.Default.Phone) { telefono = it }
            UserDataField("Dirección Principal", direccion, Icons.Default.Home) { direccion = it }
            
            Spacer(Modifier.height(16.dp))
            Text("Seguridad", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            
            UserDataField("Nueva Contraseña", password, Icons.Default.Lock, isPassword = true) { password = it }
            Text("Deja en blanco si no quieres cambiarla", color = Color.DarkGray, fontSize = 11.sp)

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    scope.launch {
                        val error = AuthManager.updateProfile(
                            nombre.ifEmpty { null },
                            telefono.ifEmpty { null },
                            direccion.ifEmpty { null },
                            password.ifEmpty { null }
                        )
                        if (error == null) {
                            Toast.makeText(context, "¡Datos actualizados!", Toast.LENGTH_SHORT).show()
                            alVolver()
                        } else {
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun UserDataField(
    label: String,
    value: String,
    icon: ImageVector,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(icon, null, tint = ColorGalyRed) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = ColorGalyRed,
            focusedBorderColor = ColorGalyRed,
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            disabledTextColor = Color.Gray,
            disabledBorderColor = Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(16.dp),
        enabled = enabled,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true
    )
}
