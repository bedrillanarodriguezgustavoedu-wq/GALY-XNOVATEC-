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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
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
fun EditProductScreen(productId: Int, alVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val productoOriginal = remember(productId) { ProductRepository.obtenerPorId(productId) }

    if (productoOriginal == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Producto no encontrado", color = Color.White)
        }
        return
    }

    var nombre by remember { mutableStateOf(productoOriginal.nombre) }
    var marca by remember { mutableStateOf(productoOriginal.marca) }
    var precioNormal by remember { mutableStateOf(productoOriginal.precioNormal.toString()) }
    var precioOferta by remember { mutableStateOf(productoOriginal.precioOferta.toString()) }
    var stock by remember { mutableStateOf(productoOriginal.stock.toString()) }
    var descripcion by remember { mutableStateOf(productoOriginal.descripcion ?: "") }
    var especificaciones by remember { mutableStateOf(productoOriginal.especificaciones ?: "") }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedMainImageName by remember { mutableStateOf<String?>(null) }
    
    // Estados de Galería
    val secondaryUris = remember { mutableStateListOf<Uri>() }
    val secondaryUrls = remember { 
        mutableStateListOf<String>().apply {
            productoOriginal.imagenesSecundarias?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.let { addAll(it) }
        }
    }
    
    var isSaving by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imagenUri = uri
            scope.launch {
                isUploading = true
                val file = ProductRepository.uriToFile(context, uri)
                if (file != null) {
                    uploadedMainImageName = ProductRepository.subirImagen(file)
                }
                isUploading = false
            }
        }
    }

    val multiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            scope.launch {
                isUploading = true
                uris.forEach { uri ->
                    val file = ProductRepository.uriToFile(context, uri)
                    if (file != null) {
                        val uploadedName = ProductRepository.subirImagen(file)
                        if (uploadedName != null) {
                            secondaryUrls.add(uploadedName)
                            secondaryUris.add(uri)
                        }
                    }
                }
                isUploading = false
                Toast.makeText(context, "${uris.size} fotos añadidas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Producto", fontWeight = FontWeight.Black, color = Color.White) },
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Imagen del Producto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF111111))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imagenUri != null) {
                    AsyncImage(
                        model = imagenUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    AsyncImage(
                        model = productoOriginal.getFullImageUrl(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        error = painterResource(R.drawable.logo_2)
                    )
                }
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                    shape = CircleShape,
                    color = ColorGalyRed
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.padding(8.dp).size(20.dp))
                }
                
                if (isUploading) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ColorGalyRed)
                    }
                }
            }

            // Gestión de Galería
            Text("Galería de Fotos", color = ColorGalyRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF111111))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (secondaryUrls.isEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxSize().clickable { multiLauncher.launch("image/*") },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null, tint = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Text("Añadir fotos a la galería", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(secondaryUrls.size) { index ->
                            val url = secondaryUrls[index]
                            Box(modifier = Modifier.size(80.dp)) {
                                val model = if (url.startsWith("http")) url 
                                            else "http://10.0.2.2/galy_api/img/${Uri.encode(url)}"
                                
                                AsyncImage(
                                    model = model,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(R.drawable.logo_2)
                                )
                                // Botón para eliminar de la lista
                                IconButton(
                                    onClick = { secondaryUrls.removeAt(index) },
                                    modifier = Modifier.align(Alignment.TopEnd).size(20.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                        item {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .clickable { multiLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, null, tint = Color.White)
                            }
                        }
                    }
                }
            }

            Text("Información Básica", color = ColorGalyRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del Producto") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )

            OutlinedTextField(
                value = marca,
                onValueChange = { marca = it },
                label = { Text("Marca") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = precioNormal,
                    onValueChange = { precioNormal = it },
                    label = { Text("Precio Normal") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = precioOferta,
                    onValueChange = { precioOferta = it },
                    label = { Text("Precio Oferta") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = textFieldColors()
                )
            }

            OutlinedTextField(
                value = stock,
                onValueChange = { stock = it },
                label = { Text("Stock Actual") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = textFieldColors()
            )

            Text("Detalles y Ficha Técnica", color = ColorGalyRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = textFieldColors()
            )

            OutlinedTextField(
                value = especificaciones,
                onValueChange = { especificaciones = it },
                label = { Text("Especificaciones (separadas por comas)") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = textFieldColors()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        isSaving = true
                        
                        // 2. Actualizar en la Base de Datos
                        val exito = ProductRepository.actualizarProducto(
                            id = productId,
                            nombre = nombre,
                            marca = marca,
                            precioNormal = precioNormal.toDoubleOrNull() ?: 0.0,
                            precioOferta = precioOferta.toDoubleOrNull() ?: 0.0,
                            descripcion = descripcion,
                            especificaciones = especificaciones,
                            stock = stock.toIntOrNull() ?: 10,
                            imagenUrl = uploadedMainImageName,
                            imagenesSecundarias = if (secondaryUrls.isNotEmpty()) secondaryUrls.toList() else null
                        )

                        if (exito) {
                            Toast.makeText(context, "✅ ¡Producto Actualizado!", Toast.LENGTH_SHORT).show()
                            ProductRepository.sincronizarTodo()
                            alVolver()
                        } else {
                            Toast.makeText(context, "❌ Error al guardar", Toast.LENGTH_SHORT).show()
                        }
                        isSaving = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Black)
                }
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = ColorGalyRed,
    unfocusedLabelColor = Color.Gray,
    focusedBorderColor = ColorGalyRed,
    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
)
