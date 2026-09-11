package com.galyxnovatec.tienda.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.model.User
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed

/**
 * Pantalla para que el administrador gestione la lista de usuarios registrados.
 *
 * @param alVolver Función para navegar hacia atrás.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(alVolver: () -> Unit) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(value = true) }

    LaunchedEffect(Unit) {
        try {
            users = RetrofitClient.instance.getAllUsers()
        } catch (e: Exception) {
            Log.e("GALY", "Error cargando usuarios: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.user_list_title), fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
            )
        },
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues = padding).background(Color.Black), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColorGalyRed)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues = padding).background(Color.Black),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(users) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = ColorGalyRed.copy(alpha = 0.2f)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, null, tint = ColorGalyRed)
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(user.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                Text(user.email, fontSize = 13.sp, color = Color.LightGray)
                                if (!user.telefono.isNullOrEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                        Icon(Icons.Default.Phone, null, modifier = Modifier.size(12.dp), tint = Color.LightGray)
                                        Spacer(Modifier.width(4.dp))
                                        Text(user.telefono, fontSize = 12.sp, color = Color.LightGray)
                                    }
                                }
                            }
                            if (user.esAdmin) {
                                Badge(containerColor = ColorGalyRed) { Text("ADMIN", color = Color.White) }
                            }
                        }
                    }
                }
            }
        }
    }
}
