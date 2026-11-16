package com.galyxnovatec.tienda.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
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
import coil.compose.AsyncImage
import com.galyxnovatec.tienda.BuildConfig
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.dialContactNumber
import com.galyxnovatec.tienda.data.openContactWhatsApp
import com.galyxnovatec.tienda.data.manager.AuthManager
import com.galyxnovatec.tienda.data.model.ServicioTecnico
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicalServiceScreen(
    alVolver: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val user = AuthManager.currentUser
    val contactInfo = ProductRepository.informacionContacto
    
    var servicios by remember { mutableStateOf<List<ServicioTecnico>>(emptyList()) }
    var misTickets by remember { mutableStateOf<List<ServicioTecnico>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var isCreatingTicket by remember { mutableStateOf(false) }
    
    // Estados para nuevo ticket
    var showAddDialog by remember { mutableStateOf(false) }
    var modeloEquipo by remember { mutableStateOf("") }
    var descripcionFalla by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        ProductRepository.sincronizarInformacionContacto()
        try {
            servicios = RetrofitClient.instance.getServiciosTecnicos()
            misTickets = user?.let { RetrofitClient.instance.getUserTickets(it.id) }.orEmpty()
        } catch (e: Exception) {
            loadError = e.message ?: "No se pudo cargar la información de soporte."
        }
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Centro de Soporte GALY", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
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
            // Hero Section
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
                // MIS TICKETS DE SOPORTE
                if (misTickets.isNotEmpty()) {
                    Text(
                        "Seguimiento de mis Equipos",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = ColorGalyRed,
                        modifier = Modifier.padding(bottom = 16.dp, top = 12.dp)
                    )
                    
                    misTickets.forEach { ticket ->
                        TicketStatusCard(ticket)
                    }
                    
                    Spacer(Modifier.height(24.dp))
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
                } else if (loadError != null) {
                    Text(loadError.orEmpty(), color = Color(0xFFFF8A80))
                    TextButton(onClick = {
                        scope.launch {
                            isLoading = true
                            loadError = null
                            try {
                                servicios = RetrofitClient.instance.getServiciosTecnicos()
                                misTickets = user?.let { RetrofitClient.instance.getUserTickets(it.id) }.orEmpty()
                            } catch (e: Exception) {
                                loadError = e.message ?: "No se pudo cargar la información de soporte."
                            }
                            isLoading = false
                        }
                    }) { Text("Reintentar") }
                } else {
                    servicios.forEach { servicio ->
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
                                context.openContactWhatsApp(contactInfo?.whatsapp, mensaje)
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
                                if (user != null) showAddDialog = true
                                else {
                                    val mensaje = "Hola GALY, necesito soporte técnico para mi equipo."
                                    context.openContactWhatsApp(contactInfo?.whatsapp, mensaje)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            enabled = !isCreatingTicket,
                            colors = ButtonDefaults.buttonColors(containerColor = if(user != null) ColorGalyRed else Color(0xFF25D366)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(if(user != null) Icons.Default.AddCircle else Icons.AutoMirrored.Filled.Chat, null, Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(if(user != null) "SOLICITAR REPARACIÓN" else "WHATSAPP SOPORTE", fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(
                            onClick = { 
                                context.dialContactNumber(contactInfo?.telefonoPrincipal)
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

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Color(0xFF111111),
            title = { Text("Solicitar Reparación", color = Color.White, fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = modeloEquipo,
                        onValueChange = { modeloEquipo = it },
                        label = { Text("Modelo del Equipo (ej: Acer Nitro 5)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = descripcionFalla,
                        onValueChange = { descripcionFalla = it },
                        label = { Text("Descripción de la falla") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                        isCreatingTicket = true
                        try {
                            val res = RetrofitClient.instance.createTicket(
                                mapOf("usuario_id" to user!!.id, "equipo_modelo" to modeloEquipo.trim(), "descripcion_falla" to descripcionFalla.trim())
                            )
                            if (res["success"] == true) {
                                Toast.makeText(context, "✅ Solicitud enviada", Toast.LENGTH_SHORT).show()
                                showAddDialog = false
                                modeloEquipo = ""
                                descripcionFalla = ""
                                user?.id?.let { misTickets = RetrofitClient.instance.getUserTickets(it) }
                            } else {
                                Toast.makeText(
                                    context,
                                    res["message"]?.toString() ?: "No se pudo enviar la solicitud.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, e.message ?: "No se pudo enviar la solicitud.", Toast.LENGTH_LONG).show()
                        } finally {
                            isCreatingTicket = false
                        }
                    }
                },
                enabled = modeloEquipo.isNotBlank() && descripcionFalla.isNotBlank() && !isCreatingTicket,
                colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed)
                ) {
                if (isCreatingTicket) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White)
                else Text("ENVIAR SOLICITUD")
                }
            }
        )
    }
}

@Composable
fun TicketStatusCard(ticket: ServicioTecnico) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F0F)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = when(ticket.estado) {
                        "Recibido" -> Color.Gray.copy(alpha = 0.2f)
                        "En Diagnostico" -> Color.Yellow.copy(alpha = 0.2f)
                        "Reparando" -> Color.Cyan.copy(alpha = 0.2f)
                        "Listo" -> Color(0xFF00FF41).copy(alpha = 0.2f)
                        "Entregado" -> Color.White.copy(alpha = 0.2f)
                        else -> Color.Gray.copy(alpha = 0.2f)
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when(ticket.estado) {
                                "Listo" -> Icons.Default.CheckCircle
                                "Reparando" -> Icons.Default.Build
                                "En Diagnostico" -> Icons.Default.Search
                                else -> Icons.Default.Inbox
                            },
                            null,
                            tint = when(ticket.estado) {
                                "Listo" -> Color(0xFF00FF41)
                                "Reparando" -> Color.Cyan
                                "En Diagnostico" -> Color.Yellow
                                else -> Color.White
                            }
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(ticket.equipoModelo ?: "Equipo en Reparación", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    if (!ticket.falla.isNullOrBlank()) {
                        Text(ticket.falla, color = Color.LightGray, fontSize = 13.sp, modifier = Modifier.padding(vertical = 2.dp))
                    }
                    Surface(
                        color = when(ticket.estado) {
                            "Listo" -> Color(0xFF00FF41).copy(alpha = 0.2f)
                            "Reparando" -> Color.Cyan.copy(alpha = 0.2f)
                            "En Diagnostico" -> Color.Yellow.copy(alpha = 0.2f)
                            else -> ColorGalyRed.copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = if (ticket.estado.isNullOrBlank()) "Recibido" else ticket.estado,
                            color = when(ticket.estado) {
                                "Listo" -> Color(0xFF00FF41)
                                "Reparando" -> Color.Cyan
                                "En Diagnostico" -> Color.Yellow
                                else -> ColorGalyRed
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                val costo = ticket.costoEstimado ?: 0.0
                if (costo > 0) {
                    Text("S/ ${costo.toInt()}", fontWeight = FontWeight.Black, color = Color.White)
                }
            }
            
            // MOSTRAR FOTOS DE EVIDENCIA
            if (!ticket.imagenIngreso.isNullOrEmpty() || !ticket.imagenListo.isNullOrEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!ticket.imagenIngreso.isNullOrEmpty()) {
                        EvidencePhotoItem("Ingreso", "${RetrofitClient.getEffectiveBaseUrl()}img/${ticket.imagenIngreso}")
                    }
                    if (!ticket.imagenListo.isNullOrEmpty()) {
                        EvidencePhotoItem("Listo", "${RetrofitClient.getEffectiveBaseUrl()}img/${ticket.imagenListo}", Color(0xFF00FF41))
                    }
                }
            }
        }
    }
}

@Composable
fun EvidencePhotoItem(label: String, url: String, color: Color = ColorGalyRed) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = url,
            contentDescription = null,
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .background(Color.Black),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 9.sp, color = color, fontWeight = FontWeight.Bold)
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
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
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
