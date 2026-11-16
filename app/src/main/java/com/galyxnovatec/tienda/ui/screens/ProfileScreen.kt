package com.galyxnovatec.tienda.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.openContactWhatsApp
import com.galyxnovatec.tienda.data.manager.AuthManager
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import com.galyxnovatec.tienda.ui.theme.ColorGalyDarkRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    alIrAMisDatos: () -> Unit = {},
    alIrAMisPedidos: () -> Unit,
    alIrAMisFavoritos: () -> Unit = {},
    alIrAComprasRecientes: () -> Unit = {},
    alIrAMisDirecciones: () -> Unit,
    alIrAMetodosPago: () -> Unit = {},
    alIrACupones: () -> Unit = {},
    alIrANotificaciones: () -> Unit = {},
    alIrAServicioTecnico: () -> Unit,
    alIrANosotros: () -> Unit = {},
    alIrAReservas: () -> Unit = {},
    alIrAPanelAdmin: () -> Unit,
    alCerrarSesion: () -> Unit,
    alVolver: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    val user = AuthManager.currentUser
    val contactInfo = ProductRepository.informacionContacto

    LaunchedEffect(Unit) {
        ProductRepository.sincronizarInformacionContacto()
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isUploading = true
                val file = ProductRepository.uriToFile(context, it)
                if (file != null) {
                    val uploadedName = ProductRepository.subirImagen(file)
                    if (uploadedName != null) {
                        val success = AuthManager.updateProfileImage(uploadedName)
                        if (success) {
                            Toast.makeText(context, "¡Foto actualizada!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error al actualizar perfil", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                isUploading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Black, color = Color.White) },
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
            // Banner Informativo Premium (Como en el screenshot)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF001A00) // Verde oscuro profundo
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, null, tint = Color(0xFF00FF41), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Envío gratis especial para ti · Todo los datos protegidos",
                        color = Color(0xFF00FF41),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Información de Conexión de Google (Como en el screenshot)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { alIrAMisDirecciones() }
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                color = Color.Transparent
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn, 
                        null, 
                        tint = Color.White, 
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (user != null) {
                                "${user.nombre} ${user.telefono ?: "999999999"}"
                            } else "Usuario de Google 999999999",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = user?.direccion ?: "Conexión vía Google",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                }
            }

            // Header del Perfil Premium original (mejorado)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(
                        Brush.linearGradient(colors = listOf(Color(0xFF121212), Color(0xFF1A1A1A))),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier
                            .size(80.dp)
                            .clickable { if (!isUploading) launcher.launch("image/*") },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.1f),
                        border = BorderStroke(2.dp, ColorGalyRed)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (user?.imagenUrl != null) {
                                AsyncImage(
                                    model = user.getFullImageUrl(),
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.logo_2)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_2),
                                    contentDescription = "Avatar",
                                    modifier = Modifier.padding(12.dp).fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            
                            if (isUploading) {
                                CircularProgressIndicator(color = ColorGalyRed, modifier = Modifier.size(30.dp))
                            } else {
                                // Overlay pequeño indicando que es editable
                                Box(
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape).padding(4.dp)
                                ) {
                                    Icon(Icons.Default.PhotoCamera, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(
                            text = user?.nombre ?: "Usuario Galy",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stars, null, tint = Color.Yellow, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "${user?.puntos ?: 0} Puntos Galy",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Yellow
                            )
                            Spacer(Modifier.width(12.dp))
                            // BADGE DE NIVEL
                            val nivel = user?.getNivelNombre() ?: "BRONCE"
                            val nivelColor = when(nivel) {
                                "ORO" -> Color(0xFFFFD700)
                                "PLATA" -> Color(0xFFC0C0C0)
                                else -> Color(0xFFCD7F32)
                            }
                            Surface(
                                color = nivelColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, nivelColor)
                            ) {
                                Text(
                                    nivel,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = nivelColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier.padding(top = 8.dp),
                            color = ColorGalyRed,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                if(user?.esAdmin == true) "ADMINISTRADOR MAESTRO" else "MIEMBRO PREMIUM",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                
                // --- SECCIÓN: MI ACTIVIDAD ---
                Text("Mi Actividad", fontWeight = FontWeight.Black, fontSize = 16.sp, color = ColorGalyRed, modifier = Modifier.padding(start = 8.dp, top = 24.dp, bottom = 12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column {
                        ProfileRowItem(icon = Icons.Default.Person, title = "Mis Datos", onClick = alIrAMisDatos)
                        DividerCustom()
                        ProfileRowItem(icon = Icons.Default.Inventory2, title = "Mis Pedidos", onClick = alIrAMisPedidos)
                        DividerCustom()
                        ProfileRowItem(icon = Icons.Default.Favorite, title = "Mis Favoritos", onClick = alIrAMisFavoritos)
                        DividerCustom()
                        ProfileRowItem(icon = Icons.Default.History, title = "Compras Recientes", onClick = alIrAComprasRecientes)
                        DividerCustom()
                        ProfileRowItem(icon = Icons.Default.Bookmark, title = "Mis reservas", onClick = alIrAReservas)
                    }
                }

                // --- SECCIÓN: PAGOS Y ENVÍO ---
                Text("Pagos y Envío", fontWeight = FontWeight.Black, fontSize = 16.sp, color = ColorGalyRed, modifier = Modifier.padding(start = 8.dp, top = 24.dp, bottom = 12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column {
                        ProfileRowItem(icon = Icons.Default.LocationOn, title = "Mis Direcciones", onClick = alIrAMisDirecciones)
                        DividerCustom()
                        ProfileRowItem(icon = Icons.Default.CreditCard, title = "Métodos de Pago", onClick = alIrAMetodosPago)
                        DividerCustom()
                        ProfileRowItem(icon = Icons.Default.ConfirmationNumber, title = "Cupones", onClick = alIrACupones)
                        DividerCustom()
                        ProfileRowItem(icon = Icons.Default.Info, title = "Centro de Ayuda (FAQ)", onClick = {
                            Toast.makeText(
                                context,
                                contactInfo?.telefonoPrincipal?.let { "Dudas: $it" }
                                    ?: "El teléfono de contacto no está disponible.",
                                Toast.LENGTH_LONG
                            ).show()
                        })
                        DividerCustom()
                        ProfileRowItem(icon = Icons.Default.Map, title = "Nuestras Tiendas", onClick = {
                            val storeQuery = listOfNotNull(
                                contactInfo?.nombreEmpresa,
                                contactInfo?.direccion,
                                contactInfo?.provincia,
                                contactInfo?.departamento,
                                contactInfo?.pais
                            ).filter(String::isNotBlank).joinToString(", ")
                            if (storeQuery.isBlank()) {
                                Toast.makeText(context, "La dirección de la tienda no está configurada.", Toast.LENGTH_LONG).show()
                                return@ProfileRowItem
                            }
                            val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(storeQuery)}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            context.startActivity(mapIntent)
                        })
                        DividerCustom()
                        ProfileRowItem(icon = Icons.Default.Info, title = "Sobre GALY", onClick = alIrANosotros)
                    }
                }

                // --- SECCIÓN: PREFERENCIAS ---
                Text("Preferencias", fontWeight = FontWeight.Black, fontSize = 16.sp, color = ColorGalyRed, modifier = Modifier.padding(start = 8.dp, top = 24.dp, bottom = 12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column {
                        ProfileRowItem(icon = Icons.Default.Notifications, title = "Notificaciones", onClick = alIrANotificaciones)
                        DividerCustom()
                        ProfileRowItem(icon = Icons.Default.Share, title = "Invitar a un amigo", onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "🚀 ¡Descarga la App de GALY XNOVATEC y obtén descuentos exclusivos en tecnología! https://galyxnovatec.com/download")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir App GALY"))
                        })
                        DividerCustom()
                        ProfileRowItem(icon = Icons.AutoMirrored.Filled.Chat, title = "Soporte Técnico", onClick = alIrAServicioTecnico)
                        DividerCustom()
                        ProfileRowItem(icon = Icons.Default.Star, title = "Calificar App", onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
                            context.startActivity(intent)
                        })
                        DividerCustom()
                        ProfileRowItem(icon = Icons.Default.Settings, title = "Configuración")
                        DividerCustom()
                        ProfileRowItem(icon = Icons.AutoMirrored.Filled.Chat, title = "Hablar con Gustavo (CEO)", onClick = {
                            context.openContactWhatsApp(
                                contactInfo?.whatsapp,
                                "Hola, soy cliente de GALY y me gustaría contactarte."
                            )
                        }, tint = Color(0xFF25D366))
                    }
                }
                
                if (user?.esAdmin == true) {
                    Text("Administración", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White, modifier = Modifier.padding(start = 8.dp, top = 24.dp, bottom = 12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                        border = BorderStroke(1.dp, ColorGalyRed.copy(alpha = 0.3f))
                    ) {
                        ProfileRowItem(
                            icon = Icons.Default.AdminPanelSettings, 
                            title = "Panel de Administrador", 
                            onClick = alIrAPanelAdmin,
                            tint = ColorGalyRed
                        )
                    }
                }

                // --- PROGRAMA DE EMBAJADORES GALY (NUEVO) ---
                Spacer(modifier = Modifier.height(24.dp))
                AmbassadorCard(user?.referralCode ?: "GALY-PROMO")

                Spacer(modifier = Modifier.height(40.dp))
                
                Button(
                    onClick = { mostrarDialogoConfirmacion = true },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Logout, null, tint = ColorGalyRed)
                    Spacer(Modifier.width(12.dp))
                    Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
                
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    if (mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacion = false },
            title = { Text("¿Ya te vas?", fontWeight = FontWeight.Black) },
            text = { Text("Esperamos verte pronto de regreso en GALY.") },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoConfirmacion = false
                        AuthManager.logout(context)
                        alCerrarSesion()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SÍ, SALIR")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmacion = false }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun AmbassadorCard(code: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A0F)), // Verde oscuro sutil
        border = BorderStroke(1.dp, Color(0xFF00FF41).copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = Color(0xFF00FF41).copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.GroupAdd, null, tint = Color(0xFF00FF41), modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Programa de Embajadores", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text("Gana puntos invitando amigos", color = Color.Gray, fontSize = 12.sp)
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            Text("Tu código de referido:", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = Color.Black,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(code, color = Color(0xFF00FF41), fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 2.sp)
                    Icon(
                        Icons.Default.ContentCopy, 
                        null, 
                        tint = Color.Gray, 
                        modifier = Modifier.size(18.dp).clickable { /* Copy logic */ }
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            Text(
                "Comparte este código y obtén 50 Puntos Galy por cada amigo que realice su primera compra.",
                color = Color.Gray,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun DividerCustom() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = Color.White.copy(alpha = 0.05f),
        thickness = 1.dp
    )
}

@Composable
fun ProfileRowItem(
    icon: ImageVector, 
    title: String, 
    onClick: () -> Unit = {},
    tint: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint.copy(alpha = 0.8f), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color.White, modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(20.dp))
    }
}
