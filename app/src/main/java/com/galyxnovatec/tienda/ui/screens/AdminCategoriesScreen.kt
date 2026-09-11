package com.galyxnovatec.tienda.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoriesScreen(alVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val categorias = ProductRepository.listaCategoriasBase
    var showDialog by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        ProductRepository.sincronizarTodo()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Categorías", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = ColorGalyRed, contentColor = Color.White) {
                Icon(Icons.Default.Add, null)
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(categorias) { cat ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#${cat.id}",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = cat.nombre,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        
                        IconButton(onClick = { /* Lógica editar */ }) {
                            Icon(Icons.Default.Edit, null, tint = Color.Gray)
                        }
                        IconButton(onClick = { 
                            scope.launch {
                                val success = ProductRepository.eliminarCategoria(cat.id)
                                if (success) {
                                    Toast.makeText(context, "Eliminado", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Delete, null, tint = ColorGalyRed)
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                containerColor = Color(0xFF1A1A1A),
                titleContentColor = Color.White,
                textContentColor = Color.LightGray,
                title = { Text("Nueva Categoría") },
                text = {
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        label = { Text("Nombre de la categoría") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ColorGalyRed,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { 
                            if (newCatName.isNotEmpty()) {
                                isLoading = true
                                scope.launch {
                                    val success = ProductRepository.agregarCategoria(newCatName)
                                    isLoading = false
                                    if (success) {
                                        Toast.makeText(context, "Categoría creada", Toast.LENGTH_SHORT).show()
                                        showDialog = false
                                        newCatName = ""
                                    } else {
                                        Toast.makeText(context, "Error al crear", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }, 
                        colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text("GUARDAR", color = Color.White)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("CANCELAR", color = Color.Gray) }
                }
            )
        }
    }
}
