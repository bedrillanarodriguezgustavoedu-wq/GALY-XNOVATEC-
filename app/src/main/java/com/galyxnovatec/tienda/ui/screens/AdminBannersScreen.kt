package com.galyxnovatec.tienda.ui.screens

import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.AddPhotoAlternate
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBannersScreen(alVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val banners = ProductRepository.banners
    
    var showDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newSubtitle by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        ProductRepository.sincronizarTodo(context)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.manage_banners_title), fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = Color.White, contentColor = Color.Black) {
                Icon(Icons.Default.Add, null)
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(banners) { banner ->
                Card(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                ) {
                    Box {
                        AsyncImage(
                            model = banner.getFullImageUrl(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.logo_2),
                            placeholder = painterResource(R.drawable.logo_2)
                        )
                        IconButton(
                            onClick = { 
                                scope.launch {
                                    val success = ProductRepository.eliminarBanner(banner.id)
                                    if (success) {
                                        Toast.makeText(context, "Banner eliminado", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape),
                        ) {
                            Icon(Icons.Default.Delete, null, tint = ColorGalyRed)
                        }
                        Text(
                            text = banner.titulo ?: "",
                            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp).background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp)).padding(4.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { if(!isUploading) showDialog = false },
                containerColor = Color(0xFF1A1A1A),
                titleContentColor = Color.White,
                textContentColor = Color.LightGray,
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddPhotoAlternate, null, tint = ColorGalyRed, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Nuevo Anuncio Premium", fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // SELECTOR DE IMAGEN MEJORADO
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF111111))
                                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(20.dp))
                                .clickable { if(!isUploading) launcher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedImageUri == null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddAPhoto, null, tint = ColorGalyRed.copy(alpha = 0.8f), modifier = Modifier.size(32.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("Toca para subir foto", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Overlay para indicar que se puede cambiar
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Edit, null, tint = Color.White)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = newTitle, 
                            onValueChange = { newTitle = it }, 
                            label = { Text("Título del anuncio") },
                            placeholder = { Text("Ej: Laptops Gamer i7", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Edit, null, tint = ColorGalyRed, modifier = Modifier.size(20.dp)) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ColorGalyRed,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                focusedLabelColor = ColorGalyRed
                            )
                        )
                        OutlinedTextField(
                            value = newSubtitle, 
                            onValueChange = { newSubtitle = it }, 
                            label = { Text("Subtítulo / Promo") },
                            placeholder = { Text("Ej: OFERTA TERMINA", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Info, null, tint = ColorGalyRed, modifier = Modifier.size(20.dp)) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ColorGalyRed,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                focusedLabelColor = ColorGalyRed
                            )
                        )
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showDialog = false },
                            enabled = !isUploading
                        ) {
                            Text("CANCELAR", color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = { 
                                scope.launch {
                                    isUploading = true
                                    try {
                                        if (selectedImageUri != null) {
                                            val file = uriToFile(context, selectedImageUri!!)
                                            if (file != null) {
                                                val uploadedName = ProductRepository.subirImagen(file)
                                                if (uploadedName != null) {
                                                    val success = ProductRepository.agregarBanner(uploadedName, newTitle, newSubtitle)
                                                    if (success) {
                                                        Toast.makeText(context, "✅ ¡Anuncio publicado!", Toast.LENGTH_SHORT).show()
                                                        showDialog = false
                                                        selectedImageUri = null
                                                        newTitle = ""
                                                        newSubtitle = ""
                                                    } else {
                                                        Toast.makeText(context, "Error al guardar banner", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isUploading = false
                                    }
                                }
                            }, 
                            colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isUploading && selectedImageUri != null && newTitle.isNotBlank(),
                            modifier = Modifier.height(48.dp)
                        ) {
                            if (isUploading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Text("GUARDAR", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }
                    }
                },
                dismissButton = null,
                shape = RoundedCornerShape(28.dp)
            )
        }
    }
}

private fun uriToFile(context: Context, uri: Uri): File? {
    val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
    return try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        file
    } catch (e: Exception) {
        null
    }
}
