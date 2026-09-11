package com.galyxnovatec.tienda.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    alSeleccionarCategoria: (String) -> Unit,
    alVolver: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // IMPORTANTE: Hacerlo reactivo para que se actualice solo
    val categorias by remember { 
        derivedStateOf { ProductRepository.obtenerCategorias() } 
    }
    var isLoading by remember { mutableStateOf(false) }

    // Forzar carga al entrar
    LaunchedEffect(Unit) {
        isLoading = true
        ProductRepository.sincronizarTodo()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorías GALY", fontWeight = FontWeight.Black, color = Color.White) },
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
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ColorGalyRed)
                }
            } else if (categorias.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(Icons.Default.CloudOff, null, Modifier.size(80.dp), tint = Color.DarkGray)
                        Spacer(Modifier.height(16.dp))
                        Text("No se pudo conectar al servidor", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Revisa que XAMPP esté encendido y que el archivo get_categories.php esté en htdocs/galy_api/",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        
                        Button(
                            onClick = { 
                                scope.launch {
                                    isLoading = true
                                    val exito = ProductRepository.sincronizarTodo()
                                    isLoading = false
                                    if (!exito) {
                                        Toast.makeText(context, "Error: ${ProductRepository.ultimoError}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.padding(top = 32.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("REINTENTAR CARGA", fontWeight = FontWeight.Black)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categorias) { cat ->
                        CategoryRow(
                            name = cat,
                            icon = when {
                                cat.contains("Laptop", true) -> Icons.Default.Laptop
                                cat.contains("Audífono", true) -> Icons.Default.Headset
                                cat.contains("Mouse", true) -> Icons.Default.Mouse
                                cat.contains("Monitor", true) -> Icons.Default.DesktopWindows
                                cat.contains("Smartphone", true) -> Icons.Default.Smartphone
                                cat.contains("Teclado", true) -> Icons.Default.Keyboard
                                cat.contains("Gaming", true) -> Icons.Default.Gamepad
                                else -> Icons.Default.Category
                            },
                            onClick = { alSeleccionarCategoria(cat) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryRow(name: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = ColorGalyRed.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = ColorGalyRed, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(name, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = Color.White)
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.DarkGray)
        }
    }
}
