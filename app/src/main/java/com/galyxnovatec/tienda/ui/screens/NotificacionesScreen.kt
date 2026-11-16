package com.galyxnovatec.tienda.ui.screens

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed

private const val PREF_NOTIFICACIONES = "galy_notifications_pref"
private const val KEY_NOTIFICACIONES = "activas"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(alVolver: () -> Unit) {
    val context = LocalContext.current
    var notificacionesActivadas by remember { mutableStateOf(true) }
    var permisoConcedido by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        notificacionesActivadas = context.getSharedPreferences(PREF_NOTIFICACIONES, Context.MODE_PRIVATE)
            .getBoolean(KEY_NOTIFICACIONES, true)
        permisoConcedido = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoConcedido = concedido
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Preferencias de Notificaciones",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Controla cómo recibes las novedades y avisos de GALY.",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Notifications, null, tint = ColorGalyRed)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Avisos y ofertas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Pedidos, promociones y recomendaciones",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = notificacionesActivadas,
                        onCheckedChange = { activar ->
                            notificacionesActivadas = activar
                            context.getSharedPreferences(PREF_NOTIFICACIONES, Context.MODE_PRIVATE)
                                .edit()
                                .putBoolean(KEY_NOTIFICACIONES, activar)
                                .apply()
                            Toast.makeText(
                                context,
                                if (activar) "Notificaciones activas" else "Notificaciones desactivadas",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }

            if (!permisoConcedido) {
                Button(
                    onClick = { permisoLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed, contentColor = Color.White)
                ) {
                    Text("ACTIVAR NOTIFICACIONES", fontWeight = FontWeight.ExtraBold)
                }
            }

            Surface(
                color = Color.Cyan.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = Color.Cyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Recuerda que las notificaciones son una herramienta poderosa de ventas. Úsalas con moderación.",
                        color = Color.Cyan,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
