package com.galyxnovatec.tienda.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import android.content.Intent
import android.net.Uri
import com.galyxnovatec.tienda.data.manager.AuthManager
import com.galyxnovatec.tienda.data.manager.PdfManager
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.galyxnovatec.tienda.data.network.SalesData
import com.galyxnovatec.tienda.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    alVolver: () -> Unit,
    alIrAAgregarProducto: () -> Unit,
    alIrACategorias: () -> Unit,
    alIrAPedidos: () -> Unit,
    alIrABanners: () -> Unit,
    alIrAUsuarios: () -> Unit,
    alIrAOfertas: () -> Unit,
    alIrACupones: () -> Unit,
    alIrANotificaciones: () -> Unit,
    alIrAGestionarCategorias: () -> Unit,
    alIrAGestionarMarcas: () -> Unit,
    alIrAContacto: () -> Unit,
    alIrANosotros: () -> Unit,
    alIrAGestionarProductos: () -> Unit,
    alIrAGestionarTickets: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var statsResponse by remember { mutableStateOf<Map<String, Any>?>(null) }
    var chartData by remember { mutableStateOf<List<SalesData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadData() {
        scope.launch {
            isLoading = true
            try {
                val res = RetrofitClient.instance.getAdminStats()
                if (res["success"] == true) {
                    statsResponse = res
                }
                chartData = RetrofitClient.instance.getSalesChartData()
            } catch (e: Exception) { }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadData() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("GALY MAESTRO", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = Color(0xFF00FF41), shape = CircleShape, modifier = Modifier.size(6.dp)) {}
                            Spacer(Modifier.width(6.dp))
                            Text("SERVIDOR ONLINE", fontSize = 9.sp, color = Color(0xFF00FF41), fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            try {
                                val response = RetrofitClient.instance.createAdminWebLink()
                                val relativeUrl = response["url"] as? String
                                if (response["success"] == true && !relativeUrl.isNullOrBlank()) {
                                    val baseUrl = RetrofitClient.getAdminWebBaseUrl().trimEnd('/') + "/"
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(baseUrl + relativeUrl.removePrefix("/"))
                                        )
                                    )
                                } else {
                                    Toast.makeText(
                                        context,
                                        response["message"]?.toString() ?: "No se pudo abrir el panel web.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } catch (exception: Exception) {
                                Toast.makeText(
                                    context,
                                    "No se pudo conectar con el panel web: ${exception.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Language, contentDescription = "Abrir panel web", tint = Color.White)
                    }
                    IconButton(onClick = { loadData() }) {
                        Icon(Icons.Default.Refresh, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
            )
        },
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding).background(Color.Black), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColorGalyRed)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // SALUDO MAESTRO
                item {
                    Column {
                        Text("¡Hola, Maestro Gustavo!", color = Color.Gray, fontSize = 14.sp)
                        Text("Rendimiento GALY", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                }

                // SECCIÓN 1: CARTAS DE ESTADO FINANCIERO (4 CARDS)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCardModern(
                                modifier = Modifier.weight(1f), 
                                title = "Ventas Totales", 
                                value = "S/ ${statsResponse?.get("ventas_monto")?.toString()?.toDoubleOrNull()?.toInt() ?: 0}", 
                                icon = Icons.Default.MonetizationOn, 
                                color = Color(0xFF00FF41)
                            )
                            StatCardModern(
                                modifier = Modifier.weight(1f), 
                                title = "Valor Almacén", 
                                value = "S/ ${statsResponse?.get("inventario_costo")?.toString()?.toDoubleOrNull()?.toInt() ?: 0}", 
                                icon = Icons.Default.Inventory2, 
                                color = Color.Cyan
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCardModern(
                                modifier = Modifier.weight(1f), 
                                title = "Utilidad Potencial", 
                                value = "S/ ${statsResponse?.get("utilidad_potencial")?.toString()?.toDoubleOrNull()?.toInt() ?: 0}", 
                                icon = Icons.AutoMirrored.Filled.TrendingUp, 
                                color = Color.Yellow
                            )
                            StatCardModern(
                                modifier = Modifier.weight(1f), 
                                title = "Clientes Totales", 
                                value = statsResponse?.get("clientes_conteo")?.toString() ?: "0", 
                                icon = Icons.Default.Group, 
                                color = Color.White
                            )
                        }
                    }
                }

                // SECCIÓN 2: BARRA DE META MENSUAL
                val porcentaje = statsResponse?.get("porcentaje_meta")?.toString()?.toDoubleOrNull() ?: 0.0
                val metaVal = statsResponse?.get("meta_mensual")?.toString()?.toDoubleOrNull() ?: 100000.0
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Meta de Ventas: S/ ${metaVal.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Surface(color = ColorGalyRed, shape = RoundedCornerShape(8.dp)) {
                                    Text("${porcentaje.toInt()}%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { (porcentaje / 100.0).toFloat() },
                                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(10.dp)),
                                color = ColorGalyRed,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        }
                    }
                }

                // SECCIÓN 3: GRÁFICA DE TENDENCIA
                if (chartData.isNotEmpty()) {
                    item {
                        SalesChartCard(chartData)
                    }
                }

                // SECCIÓN: VENTAS POR DISTRITO (NUEVO)
                val distritosData = statsResponse?.get("ventas_distrito") as? List<Map<String, Any>>
                if (!distritosData.isNullOrEmpty()) {
                    item {
                        DistrictChartCard(distritosData)
                    }
                }

                // SECCIÓN 4: STOCK CRÍTICO (LISTA)
                val lowStockList = statsResponse?.get("stock_critico") as? List<Map<String, Any>>
                if (!lowStockList.isNullOrEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF150505)), // Rojo muy oscuro
                            border = BorderStroke(1.dp, ColorGalyRed.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Error, null, tint = ColorGalyRed, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("STOCK CRÍTICO", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                }
                                Spacer(Modifier.height(16.dp))
                                lowStockList.forEach { item ->
                                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(item["nombre"].toString(), color = Color.LightGray, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                        Surface(color = ColorGalyRed, shape = RoundedCornerShape(4.dp)) {
                                            Text("${item["stock"]} UN.", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SECCIÓN 5: ACCIONES DE GESTIÓN
                item {
                    Text("Gestión de Negocio", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(top = 8.dp))
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AdminFullActionItem("Agregar producto", "Añadir al catálogo compartido", Icons.Default.AddBox, ColorGalyRed, alIrAAgregarProducto)
                        AdminFullActionItem("Mi Inventario", "Productos y Stock", Icons.Default.Inventory, Color.White, alIrAGestionarProductos)
                        AdminFullActionItem("Categorías", "Catálogo compartido con la web", Icons.Default.Category, Color.Yellow, alIrAGestionarCategorias)
                        AdminFullActionItem("Marcas", "Catálogo compartido con la web", Icons.Default.Business, Color.Cyan, alIrAGestionarMarcas)
                        AdminFullActionItem("Banners", "Portada compartida web y GALY", Icons.Default.Image, ColorGalyRed, alIrABanners)
                        AdminFullActionItem("Contacto", "Teléfono, WhatsApp, correo y redes", Icons.Default.ContactSupport, Color.Cyan, alIrAContacto)
                        AdminFullActionItem("Nosotros", "Información pública compartida con la web", Icons.Default.Info, Color.Yellow, alIrANosotros)
                        AdminFullActionItem("Ventas", "Historial de Pedidos", Icons.AutoMirrored.Filled.ReceiptLong, Color(0xFF00FF41), alIrAPedidos)
                        AdminFullActionItem("Soporte Técnico", "Tickets y Reparaciones", Icons.Default.Handyman, Color.Cyan, alIrAGestionarTickets)
                        AdminFullActionItem("Clientes", "Puntos y Rango Admin", Icons.Default.People, Color.Yellow, alIrAUsuarios)
                        AdminFullActionItem("Notificaciones", "Enviar avisos a clientes", Icons.Default.NotificationsActive, ColorGalyRed, alIrANotificaciones)
                        AdminFullActionItem("Ofertas", "Campañas y descuentos por producto", Icons.Default.LocalOffer, ColorGalyRed, alIrAOfertas)
                        AdminFullActionItem("Cupones", "Códigos promocionales", Icons.Default.ConfirmationNumber, Color.White, alIrACupones)
                    }
                }

                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun AdminFullActionItem(title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F0F0F),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(22.dp)) }
            }
            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.DarkGray)
        }
    }
}

@Composable
fun DistrictChartCard(data: List<Map<String, Any>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Ventas por Distrito", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))
            
            val maxMonto = data.map { it["monto"].toString().toDoubleOrNull() ?: 1.0 }.maxOrNull() ?: 1.0
            
            data.forEach { item ->
                val label = item["label"]?.toString() ?: "Otros"
                val monto = item["monto"]?.toString()?.toDoubleOrNull() ?: 0.0
                val progress = (monto / maxMonto).toFloat()
                
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(label, color = Color.LightGray, fontSize = 12.sp)
                        Text("S/ ${monto.toInt()}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = ColorGalyRed,
                        trackColor = Color.White.copy(alpha = 0.05f)
                    )
                }
            }
        }
    }
}

@Composable
fun SalesChartCard(data: List<SalesData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Tendencia de Ventas (Soles)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) { SalesCanvas(data) }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                data.forEach { Text(it.label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun SalesCanvas(data: List<SalesData>) {
    val maxSales = (data.maxOfOrNull { it.total } ?: 1.0).coerceAtLeast(1.0)
    val color = Color(0xFF00FF41)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val spacing = if (data.size > 1) width / (data.size - 1) else 0f
        val points = data.mapIndexed { index, item ->
            Offset(index * spacing, height - (item.total.toFloat() / maxSales.toFloat() * height))
        }
        val path = Path().apply { if (points.isNotEmpty()) { moveTo(points[0].x, points[0].y); points.forEach { lineTo(it.x, it.y) } } }
        drawPath(path = path, color = color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        val fillPath = Path().apply { if (points.isNotEmpty()) { moveTo(points[0].x, height); points.forEach { lineTo(it.x, it.y) }; lineTo(points.last().x, height); close() } }
        drawPath(path = fillPath, brush = Brush.verticalGradient(colors = listOf(color.copy(alpha = 0.2f), Color.Transparent)))
    }
}

@Composable
fun StatCardModern(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = color.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(18.dp)) }
            }
            Spacer(Modifier.height(16.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
            Text(title, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}
