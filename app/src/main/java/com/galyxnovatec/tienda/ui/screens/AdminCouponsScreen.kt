package com.galyxnovatec.tienda.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.data.model.Cupon
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCouponsScreen(alVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var cupones by remember { mutableStateOf<List<Cupon>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }

    fun cargarCupones() {
        scope.launch {
            isLoading = true
            try {
                cupones = RetrofitClient.instance.getCupones()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar cupones", Toast.LENGTH_SHORT).show()
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { cargarCupones() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cupones GALY", fontWeight = FontWeight.Black, color = Color.White) },
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
                Icon(Icons.Default.Add, "Nuevo Cupón")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = ColorGalyRed)
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(cupones) { cupon ->
                        CouponItem(cupon) {
                            scope.launch {
                                val res = RetrofitClient.instance.deleteCupon(mapOf("id" to cupon.id))
                                if (res["success"] == true) {
                                    Toast.makeText(context, "Cupón eliminado", Toast.LENGTH_SHORT).show()
                                    cargarCupones()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        var codigo by remember { mutableStateOf("") }
        var descuento by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Color(0xFF111111),
            title = { Text("Nuevo Cupón", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = codigo,
                        onValueChange = { codigo = it.uppercase() },
                        label = { Text("Código") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = descuento,
                        onValueChange = { descuento = it },
                        label = { Text("Porcentaje (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        val res = RetrofitClient.instance.addCupon(mapOf("codigo" to codigo, "descuento_porcentaje" to (descuento.toIntOrNull() ?: 0)))
                        if (res["success"] == true) {
                            showDialog = false
                            cargarCupones()
                        }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed)) {
                    Text("GUARDAR")
                }
            }
        )
    }
}

@Composable
fun CouponItem(cupon: Cupon, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F0F)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(cupon.codigo, fontWeight = FontWeight.Black, fontSize = 18.sp, color = ColorGalyRed)
                Text("${cupon.descuentoPorcentaje}% de Descuento", color = Color.Gray, fontSize = 14.sp)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Color.Gray)
            }
        }
    }
}
