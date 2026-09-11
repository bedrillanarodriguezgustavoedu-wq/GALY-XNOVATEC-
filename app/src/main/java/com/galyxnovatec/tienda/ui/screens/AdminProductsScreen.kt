package com.galyxnovatec.tienda.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.model.Producto
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductsScreen(
    alVolver: () -> Unit,
    alIrAEditarProducto: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allProducts = ProductRepository.productos
    
    var textoBusqueda by remember { mutableStateOf("") }
    var selectedProductId by remember { mutableIntStateOf(-1) }
    var isUploading by remember { mutableStateOf(false) }

    val filteredProducts = remember(textoBusqueda, allProducts.size) {
        if (textoBusqueda.isEmpty()) allProducts.filter { it.id != -1 }
        else allProducts.filter { it.id != -1 && (it.nombre.contains(textoBusqueda, true) || it.marca.contains(textoBusqueda, true)) }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && selectedProductId != -1) {
            scope.launch {
                isUploading = true
                val file = ProductRepository.uriToFile(context, uri)
                if (file != null) {
                    val uploadedName = ProductRepository.subirImagen(file)
                    if (uploadedName != null) {
                        val exito = ProductRepository.actualizarImagenProducto(selectedProductId, uploadedName)
                        if (exito) {
                            Toast.makeText(context, "✅ Imagen actualizada!", Toast.LENGTH_SHORT).show()
                            ProductRepository.sincronizarTodo()
                        }
                    }
                }
                isUploading = false
                selectedProductId = -1
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Inventario", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        Toast.makeText(context, "Escáner de Barras activado", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.QrCodeScanner, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black)) {
            // Buscador rápido
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Buscar por nombre o marca...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = ColorGalyRed) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = ColorGalyRed,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Text(
                "Total: ${filteredProducts.size} productos", 
                color = ColorGalyRed, 
                fontSize = 12.sp, 
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProducts) { producto ->
                    AdminProductRow(
                        producto = producto,
                        onUpdateImage = {
                            selectedProductId = producto.id
                            launcher.launch("image/*")
                        },
                        onEdit = { alIrAEditarProducto(producto.id) }
                    )
                }
            }
        }
        
        if (isUploading) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColorGalyRed)
            }
        }
    }
}

@Composable
fun AdminProductRow(
    producto: Producto, 
    onUpdateImage: () -> Unit,
    onEdit: () -> Unit
) {
    val needsImage = producto.imagenUrl.isNullOrEmpty() || 
                     producto.imagenUrl.contains("placeholder") || 
                     producto.imagenUrl.contains("via.placeholder")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, if (needsImage) ColorGalyRed else Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (needsImage) ColorGalyRed.copy(alpha = 0.1f) else Color.Black)
                    .border(
                        BorderStroke(if (needsImage) 2.dp else 0.dp, if (needsImage) ColorGalyRed else Color.Transparent),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                if (needsImage) {
                    Icon(
                        Icons.Default.ImageNotSupported, 
                        null, 
                        tint = ColorGalyRed, 
                        modifier = Modifier.align(Alignment.Center).size(24.dp)
                    )
                } else {
                    AsyncImage(
                        model = producto.getFullImageUrl(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        contentScale = ContentScale.Fit,
                        error = painterResource(R.drawable.logo_2)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    producto.nombre, 
                    fontWeight = FontWeight.Bold, 
                    color = Color.White, 
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(producto.marca, color = Color.Gray, fontSize = 11.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("S/ ${producto.precioOferta.toInt()}", color = ColorGalyRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = if (producto.stock < 5) Color.Red.copy(alpha = 0.2f) else Color.Green.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "Stock: ${producto.stock}", 
                            color = if (producto.stock < 5) Color.Red else Color(0xFF00FF41), 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onUpdateImage,
                    modifier = Modifier.background(if (needsImage) ColorGalyRed else Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.AddAPhoto, 
                        null, 
                        tint = if (needsImage) Color.White else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
