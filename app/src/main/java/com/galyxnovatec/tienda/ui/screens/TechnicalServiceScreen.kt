package com.galyxnovatec.tienda.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.model.ServicioTecnico
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicalServiceScreen(
    alVolver: () -> Unit
) {
    val context = LocalContext.current
    val numeroGaly = "51978104136"
    var servicios by remember { mutableStateOf<List<ServicioTecnico>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        ProductRepository.sincronizarTodo()
        servicios = ProductRepository.obtenerServicios()
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Centro de Soporte GALY", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
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
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Section con Imagen Completa (Ajustada para verla 100%)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth 
                )
                
                // Capa de contraste premium para que el texto resalte sobre la imagen
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent, 
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Black.copy(alpha = 0.9f)
                                )
                            )
                        )
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, 
                    modifier = Modifier.padding(bottom = 24.dp, start = 20.dp, end = 20.dp)
                ) {
                    Text(
                        "¿PROBLEMAS TÉCNICOS?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Soluciones expertas en hardware y software",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                // Info badges
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SupportBadge(icon = Icons.Default.Verified, text = "Certificado")
                    SupportBadge(icon = Icons.Default.Timer, text = "Express")
                    SupportBadge(icon = Icons.Default.Shield, text = "Garantía")
                }

                Text(
                    "Servicios Profesionales",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                
                if (isLoading) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ColorGalyRed)
                    }
                } else {
                    val displayList = if (servicios.isEmpty()) {
                        listOf(
                            ServicioTecnico(1, "Reparación de Hardware", "Placas madre, pantallas y más.", 0.0),
                            ServicioTecnico(2, "Mantenimiento Preventivo", "Limpieza y pasta térmica.", 0.0),
                            ServicioTecnico(3, "Optimización de Software", "Sistemas y drivers gaming.", 0.0)
                        )
                    } else {
                        servicios
                    }

                    displayList.forEach { servicio ->
                        ModernServiceItem(
                            title = servicio.nombre,
                            desc = servicio.descripcion ?: "Calidad GALY garantizada.",
                            icon = when {
                                servicio.nombre.contains("Hardware", true) -> Icons.Default.DeveloperBoard
                                servicio.nombre.contains("Mantenimiento", true) -> Icons.Default.Air
                                servicio.nombre.contains("Software", true) -> Icons.Default.Terminal
                                else -> Icons.Default.Settings
                            },
                            onClick = {
                                val mensaje = "Hola GALY, necesito información sobre el servicio: ${servicio.nombre}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$numeroGaly?text=${Uri.encode(mensaje)}"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Contacto Directo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Asistencia Inmediata", fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp)
                        Text(
                            "Comunícate directamente con nuestros técnicos",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                        )
                        
                        Button(
                            onClick = { 
                                val mensaje = "Hola GALY, necesito soporte técnico para mi equipo."
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$numeroGaly?text=${Uri.encode(mensaje)}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, null, Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("WHATSAPP SOPORTE", fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(
                            onClick = { 
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:978104136"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Icon(Icons.Default.Phone, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("LLAMAR AHORA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun SupportBadge(icon: ImageVector, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = Color(0xFF1A1A1A)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, Modifier.size(20.dp), tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
    }
}

@Composable
fun ModernServiceItem(title: String, desc: String, icon: ImageVector, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF111111),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            Text(desc, fontSize = 13.sp, color = Color.Gray)
        }
    }
}
