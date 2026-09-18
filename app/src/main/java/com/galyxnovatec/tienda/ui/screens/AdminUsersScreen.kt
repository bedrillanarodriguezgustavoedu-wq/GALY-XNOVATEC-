package com.galyxnovatec.tienda.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.model.User
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(alVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showEditPointsDialog by remember { mutableStateOf(false) }

    fun cargarUsuarios() {
        scope.launch {
            isLoading = true
            try {
                users = RetrofitClient.instance.getAllUsers()
            } catch (e: Exception) {
                Log.e("GALY", "Error cargando usuarios: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { cargarUsuarios() }

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
            Box(Modifier.fillMaxSize().padding(padding).background(Color.Black), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColorGalyRed)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(users) { user ->
                    UserCardMaster(
                        user = user,
                        onEditPoints = { 
                            selectedUser = user
                            showEditPointsDialog = true 
                        },
                        onToggleAdmin = {
                            scope.launch {
                                try {
                                    val res = RetrofitClient.instance.updateUsuario(
                                        mapOf("id" to user.id, "es_admin" to if (user.esAdmin) 0 else 1)
                                    )
                                    if (res["success"] == true) {
                                        Toast.makeText(context, "Rango actualizado", Toast.LENGTH_SHORT).show()
                                        cargarUsuarios()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showEditPointsDialog && selectedUser != null) {
        var nuevosPuntos by remember { mutableStateOf(selectedUser!!.puntos.toString()) }
        AlertDialog(
            onDismissRequest = { showEditPointsDialog = false },
            containerColor = Color(0xFF111111),
            title = { Text("Editar Puntos GALY", color = Color.White) },
            text = {
                Column {
                    Text("Usuario: ${selectedUser!!.nombre}", color = Color.Gray, fontSize = 12.sp)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = nuevosPuntos,
                        onValueChange = { nuevosPuntos = it },
                        label = { Text("Puntos actuales") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val res = RetrofitClient.instance.updateUsuario(
                                    mapOf("id" to selectedUser!!.id, "puntos" to (nuevosPuntos.toIntOrNull() ?: 0))
                                )
                                if (res["success"] == true) {
                                    showEditPointsDialog = false
                                    cargarUsuarios()
                                    Toast.makeText(context, "Puntos actualizados", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed)
                ) {
                    Text("GUARDAR")
                }
            }
        )
    }
}

@Composable
fun UserCardMaster(user: User, onEditPoints: () -> Unit, onToggleAdmin: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = if(user.esAdmin) ColorGalyRed.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = if(user.esAdmin) ColorGalyRed else Color.Gray)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(user.nombre, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                Text(user.email, fontSize = 12.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    val nivelColor = when {
                        user.puntos >= 1500 -> Color(0xFFFFD700)
                        user.puntos >= 500 -> Color(0xFFC0C0C0)
                        else -> Color(0xFFCD7F32)
                    }
                    Box(Modifier.size(8.dp).background(nivelColor, CircleShape))
                    Spacer(Modifier.width(6.dp))
                    Text("${user.puntos} Pts", fontSize = 12.sp, color = nivelColor, fontWeight = FontWeight.Bold)
                    Text(" (${user.getNivelNombre()})", fontSize = 10.sp, color = Color.Gray)
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEditPoints) {
                    Icon(Icons.Default.Edit, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onToggleAdmin) {
                    Icon(
                        imageVector = if(user.esAdmin) Icons.Default.Shield else Icons.Default.Shield, 
                        contentDescription = null, 
                        tint = if(user.esAdmin) ColorGalyRed else Color.DarkGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
