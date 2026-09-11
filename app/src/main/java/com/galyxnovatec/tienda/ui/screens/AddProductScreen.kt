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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

/**
 * Pantalla para que el administrador pueda añadir nuevos productos a la tienda.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    alVolver: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var nombre by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var precioNormal by remember { mutableStateOf("") }
    var precioOferta by remember { mutableStateOf("") }
    var imagenUrl by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var descripcion by remember { mutableStateOf("") }
    var descuento by remember { mutableStateOf("") }
    var etiquetaEspecial by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("10") }
    
    var imagenesSecundarias by remember { mutableStateOf("") }
    val secondaryUris = remember { mutableStateListOf<Uri>() }
    val secondaryUrls = remember { mutableStateListOf<String>() }
    
    var isLoading by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    // Launcher para la imagen principal
    val mainLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        imageUri = uri
        if (uri != null) {
            scope.launch {
                isUploading = true
                val file = ProductRepository.uriToFile(context, uri)
                if (file != null) {
                    val uploadedName = ProductRepository.subirImagen(file)
                    if (uploadedName != null) {
                        imagenUrl = uploadedName
                        Toast.makeText(context, "Imagen principal subida", Toast.LENGTH_SHORT).show()
                    }
                }
                isUploading = false
            }
        }
    }

    // Launcher para imágenes secundarias (múltiples)
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
                imagenesSecundarias = secondaryUrls.joinToString(", ")
                isUploading = false
                Toast.makeText(context, "${uris.size} imágenes subidas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_product_title), fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = padding)
                .background(Color.Black)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.info_general_header), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text(stringResource(R.string.product_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = ColorGalyRed,
                    unfocusedLabelColor = Color.Gray,
                    focusedBorderColor = ColorGalyRed,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = marca,
                    onValueChange = { marca = it },
                    label = { Text(stringResource(R.string.brand_label)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = ColorGalyRed,
                        unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = ColorGalyRed,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )
                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text(stringResource(R.string.category_label)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = ColorGalyRed,
                        unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = ColorGalyRed,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )
            }

            Text(stringResource(R.string.prices_offers_header), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = precioNormal,
                    onValueChange = { 
                        precioNormal = it
                        val normal = it.toDoubleOrNull() ?: 0.0
                        val desc = descuento.toIntOrNull() ?: 0
                        if (desc > 0) {
                            precioOferta = (normal * (1.0 - (desc / 100.0))).toInt().toString()
                        }
                    },
                    label = { Text(stringResource(R.string.normal_price_label)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    prefix = { Text(stringResource(R.string.sol_prefix)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = ColorGalyRed,
                        unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = ColorGalyRed,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )
                OutlinedTextField(
                    value = precioOferta,
                    onValueChange = { precioOferta = it },
                    label = { Text(stringResource(R.string.offer_price_label)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    prefix = { Text(stringResource(R.string.sol_prefix)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = ColorGalyRed,
                        unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = ColorGalyRed,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )
            }

            OutlinedTextField(
                value = descuento,
                onValueChange = { 
                    descuento = it
                    val normal = precioNormal.toDoubleOrNull() ?: 0.0
                    val desc = it.toIntOrNull() ?: 0
                    if (desc > 0 && normal > 0) {
                        precioOferta = (normal * (1.0 - (desc / 100.0))).toInt().toString()
                    }
                },
                label = { Text(stringResource(R.string.discount_label)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                suffix = { Text("%") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = ColorGalyRed,
                    unfocusedLabelColor = Color.Gray,
                    focusedBorderColor = ColorGalyRed,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                )
            )

            OutlinedTextField(
                value = stock,
                onValueChange = { stock = it },
                label = { Text("Stock Inicial") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = ColorGalyRed,
                    unfocusedLabelColor = Color.Gray,
                    focusedBorderColor = ColorGalyRed,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                )
            )

            Text(stringResource(R.string.presentation_details_header), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)

            // SELECTOR DE IMAGEN PRINCIPAL
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A1A1A))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(16.dp))
                    .clickable { mainLauncher.launch("image/*") },
                contentAlignment = Alignment.Center,
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = ColorGalyRed)
                } else {
                    val displayImage = if (imageUri != null) imageUri 
                                       else if (imagenUrl.isNotEmpty()) {
                                           if (imagenUrl.startsWith("http")) imagenUrl 
                                           else "http://10.0.2.2/galy_api/img/$imagenUrl"
                                       } else null

                    if (displayImage == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(40.dp), tint = Color.LightGray)
                            Spacer(Modifier.height(8.dp))
                            Text("Foto Principal", color = Color.LightGray, fontSize = 14.sp)
                        }
                    } else {
                        AsyncImage(
                            model = displayImage,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            error = painterResource(R.drawable.logo_2),
                            placeholder = painterResource(R.drawable.logo_2)
                        )
                    }
                }
            }

            // SELECTOR DE IMÁGENES SECUNDARIAS
            Text("Imágenes de Galería", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A1A1A))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(16.dp))
                    .clickable { multiLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (secondaryUris.isEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhotoLibrary, null, tint = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Text("Añadir fotos a la galería", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(secondaryUris) { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        item {
                            Box(
                                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF222222)).clickable { multiLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, null, tint = Color.White)
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = imagenUrl,
                onValueChange = { imagenUrl = it },
                label = { Text(stringResource(R.string.image_url_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text(stringResource(R.string.or_paste_link)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = ColorGalyRed,
                    unfocusedLabelColor = Color.Gray,
                    focusedBorderColor = ColorGalyRed,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                )
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text(stringResource(R.string.description_label)) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = ColorGalyRed,
                    unfocusedLabelColor = Color.Gray,
                    focusedBorderColor = ColorGalyRed,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                )
            )

            OutlinedTextField(
                value = etiquetaEspecial,
                onValueChange = { etiquetaEspecial = it },
                label = { Text(stringResource(R.string.features_label)) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text(stringResource(R.string.features_placeholder)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = ColorGalyRed,
                    unfocusedLabelColor = Color.Gray,
                    focusedBorderColor = ColorGalyRed,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                )
            )

            OutlinedTextField(
                value = imagenesSecundarias,
                onValueChange = { imagenesSecundarias = it },
                label = { Text("Nombres de archivos (Galeria)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = ColorGalyRed,
                    unfocusedLabelColor = Color.Gray,
                    focusedBorderColor = ColorGalyRed,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (nombre.isBlank() || precioNormal.isBlank() || categoria.isBlank()) {
                        Toast.makeText(context, context.getString(R.string.fields_mandatory_error), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    scope.launch {
                        isLoading = true
                        try {
                            val secondaryList = imagenesSecundarias.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            
                            val res = RetrofitClient.instance.addProducto(
                                mapOf(
                                    "nombre" to nombre,
                                    "marca" to marca,
                                    "categoria" to categoria,
                                    "precio_normal" to (precioNormal.toDoubleOrNull() ?: 0.0),
                                    "precio_oferta" to (precioOferta.toDoubleOrNull() ?: 0.0),
                                    "imagen_url" to imagenUrl,
                                    "descripcion" to descripcion,
                                    "descuento" to (descuento.toIntOrNull() ?: 0),
                                    "etiqueta_especial" to etiquetaEspecial,
                                    "stock" to (stock.toIntOrNull() ?: 10),
                                    "imagenes_secundarias" to secondaryList
                                )
                            )
                            
                            if (res["success"] == true) {
                                // Sincronizamos para que el nuevo producto aparezca de inmediato
                                ProductRepository.sincronizarTodo()
                                Toast.makeText(context, "✅ Producto guardado con éxito", Toast.LENGTH_LONG).show()
                                alVolver()
                            } else {
                                Toast.makeText(context, context.getString(R.string.error_prefix, res["message"]), Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.network_error_prefix, e.message), Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                enabled = !isLoading && !isUploading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text(stringResource(R.string.save_product_button), fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
