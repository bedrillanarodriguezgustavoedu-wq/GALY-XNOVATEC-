package com.galyxnovatec.tienda.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotificationsScreen(alVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var titulo by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anuncios Push", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Enviar Notificación Masiva", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Este mensaje llegará a todos los clientes que tengan la app instalada.", color = Color.Gray, fontSize = 14.sp)

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título del Anuncio") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ej: ¡Oferta Relámpago!") },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = mensaje,
                onValueChange = { mensaje = it },
                label = { Text("Mensaje") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = { Text("Escribe aquí lo que quieres comunicar...") },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            )

            Surface(
                color = Color.Cyan.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = Color.Cyan)
                    Spacer(Modifier.width(12.dp))
                    Text("Recuerda que las notificaciones son una herramienta poderosa de ventas. Úsalas con moderación.", color = Color.Cyan, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (titulo.isBlank() || mensaje.isBlank()) {
                        Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch {
                        isLoading = true
                        try {
                            val res = RetrofitClient.instance.sendPushNotification(mapOf("titulo" to titulo, "mensaje" to mensaje))
                            if (res["success"] == true) {
                                Toast.makeText(context, "¡Anuncio enviado con éxito!", Toast.LENGTH_LONG).show()
                                titulo = ""
                                mensaje = ""
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error al enviar notificación", Toast.LENGTH_SHORT).show()
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, null)
                    Spacer(Modifier.width(12.dp))
                    Text("ENVIAR AHORA", fontWeight = FontWeight.ExtraBold)
                }
            }
            
            Spacer(Modifier.height(20.dp))
        }
    }
}
