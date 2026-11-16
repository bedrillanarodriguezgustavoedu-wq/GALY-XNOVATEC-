package com.galyxnovatec.tienda.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.data.model.Cupon
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuponesScreen(
    alVolver: () -> Unit,
    alIrAlCarrito: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var cupones by remember { mutableStateOf<List<Cupon>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

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
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = ColorGalyRed)
            } else if (cupones.isEmpty()) {
                Text(
                    text = "Todavía no tenemos cupones disponibles. ¡Vuelve pronto!",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(cupones) { cupon ->
                        CuponCard(
                            cupon = cupon,
                            onCopiar = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("CuponGALY", cupon.codigo))
                                Toast.makeText(context, "Código ${cupon.codigo} copiado", Toast.LENGTH_SHORT).show()
                            },
                            onIrAlCarrito = onIrAlCarrito
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CuponCard(cupon: Cupon, onCopiar: () -> Unit, onIrAlCarrito: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F0F)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(cupon.codigo, fontWeight = FontWeight.Black, fontSize = 18.sp, color = ColorGalyRed)
                    Text("${cupon.descuentoPorcentaje}% de Descuento", color = Color.Gray, fontSize = 14.sp)
                }
                IconButton(onClick = onCopiar) {
                    Icon(Icons.Default.ContentCopy, null, tint = Color.Gray)
                }
            }
            Button(
                onClick = onIrAlCarrito,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed, contentColor = Color.White)
            ) {
                Text("USAR EN MI CARRITO", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
