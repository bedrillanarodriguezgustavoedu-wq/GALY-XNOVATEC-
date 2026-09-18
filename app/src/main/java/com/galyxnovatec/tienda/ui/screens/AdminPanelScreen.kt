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
import com.galyxnovatec.tienda.data.manager.PdfManager
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.galyxnovatec.tienda.data.network.SalesData
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    alVolver: () -> Unit,
    alIrAAgregarProducto: () -> Unit,
    alIrACategorias: () -> Unit,
    alIrAPedidos: () -> Unit,
    alIrABanners: () -> Unit,
    alIrAUsuarios: () -> Unit,
    alIrACupones: () -> Unit,
    alIrANotificaciones: () -> Unit,
    alIrAGestionarCategorias: () -> Unit,
    alIrAGestionarProductos: () -> Unit,
    alIrAGestionarTickets: () -> Unit,
) {
    val context = LocalContext.current
    var stats by remember { mutableStateOf(mapOf("ventas" to "S/ 0", "ganancia" to "S/ 0", "pedidos" to "0", "usuarios" to "0", "stock" to "0")) }
    var topProducts by remember { mutableStateOf<List<String>>(emptyList()) }
    var vipCustomers by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var chartData by remember { mutableStateOf<List<SalesData>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val res = RetrofitClient.instance.getAdminStats()
            if (res["success"] == true) {
                stats = res.filter { it.key != "top_productos" && it.key != "clientes_vip" }.mapValues { it.value.toString() }
                topProducts = (res["top_productos"] as? List<*>)?.map { it.toString() } ?: emptyList()
                vipCustomers = (res["clientes_vip"] as? List<*>)?.map { (it as Map<String, Any>).mapValues { entry -> entry.value.toString() } } ?: emptyList()
            }
            chartData = RetrofitClient.instance.getSalesChartData()
        } catch (e: Exception) { }
    }

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
                    IconButton(onClick = { /* Refresh logic */ }) {
                        Icon(Icons.Default.Refresh, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // SALUDO MAESTRO
            item {
                Column {
                    Text("¡Hola, Maestro!", color = Color.Gray, fontSize = 14.sp)
                    Text("Resumen del Negocio", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
            }

            // SECCIÓN: CARTAS DE ESTADO
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    StatCardModern(Modifier.weight(1f), "Ventas Hoy", stats["ventas"] ?: "S/ 0", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF00FF41))
                    StatCardModern(Modifier.weight(1f), "Ganancia Real", stats["ganancia"] ?: "S/ 0", Icons.Default.AccountBalanceWallet, Color.Yellow)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    StatCardModern(Modifier.weight(1f), "Clientes", stats["usuarios"] ?: "0", Icons.Default.Group, Color.Cyan)
                    StatCardModern(Modifier.weight(1f), "Nuevos Pedidos", stats["pedidos"] ?: "0", Icons.Default.ShoppingCart, ColorGalyRed)
                }
            }

            // GRÁFICA DE VENTAS
            if (chartData.isNotEmpty()) {
                item {
                    SalesChartCard(chartData)
                }
            }

            // TOP PRODUCTOS
            if (topProducts.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Stars, null, tint = Color.Yellow, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("Más Vendidos", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                            Spacer(Modifier.height(16.dp))
                            topProducts.forEach { prod ->
                                Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF00FF41), modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(prod, color = Color.LightGray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            // CLIENTES VIP
            if (vipCustomers.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WorkspacePremium, null, tint = Color.Yellow, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("Top Clientes VIP", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                            Spacer(Modifier.height(16.dp))
                            vipCustomers.forEach { customer ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(customer["nombre"] ?: "Desconocido", color = Color.LightGray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text(customer["total"] ?: "S/ 0", color = Color.Yellow, fontSize = 13.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }

            // ACCIONES PRINCIPALES
            item {
                Text(
                    "Gestión de Tienda",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminFullActionItem("Mi Inventario", "Gestionar productos y stock", Icons.Default.Inventory, Color.White, alIrAGestionarProductos)
                    AdminFullActionItem("Ventas Realizadas", "Ver pedidos y emitir boletas", Icons.AutoMirrored.Filled.ReceiptLong, Color(0xFF00FF41), alIrAPedidos)
                    AdminFullActionItem("Gestión de Soporte", "Reparaciones y Tickets", Icons.Default.Handyman, Color.Cyan, alIrAGestionarTickets)
                    AdminFullActionItem("Publicidad (Banners)", "Editar anuncios de la Home", Icons.Default.AdUnits, ColorGalyRed, alIrABanners)
                    AdminFullActionItem("Gestión de Clientes", "Ver lista de usuarios y puntos", Icons.Default.People, Color.Cyan, alIrAUsuarios)
                    AdminFullActionItem("Reporte Contable PDF", "Exportar ganancias y costos", Icons.Default.Assessment, Color.Yellow, {
                        scope.launch {
                            try {
                                val allOrders = RetrofitClient.instance.getAllOrders()
                                PdfManager.generarReporteGanancias(context, allOrders)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error al generar reporte", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                    AdminFullActionItem("Cupones GALY", "Crear ofertas especiales", Icons.Default.ConfirmationNumber, Color.Yellow, alIrACupones)
                    AdminFullActionItem("Anuncios Push", "Enviar notificaciones masivas", Icons.Default.Send, Color(0xFF00FF41), alIrANotificaciones)
                }
            }
            
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF1A1A1A),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sincronización de Datos", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Tu base de datos está conectada", color = Color.Gray, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.CloudDone, null, tint = Color(0xFF00FF41))
                    }
                }
            }

            item { Spacer(Modifier.height(120.dp)) }
        }
    }
}

@Composable
fun AdminFullActionItem(title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F0F0F),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
                }
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
fun SalesChartCard(data: List<SalesData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Tendencia de Ventas (7 días)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))
            Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                SalesCanvas(data)
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                data.forEach { 
                    Text(it.label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
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
            val x = index * spacing
            val y = height - (item.total.toFloat() / maxSales.toFloat() * height)
            Offset(x, y)
        }
        val path = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points[0].x, points[0].y)
                points.forEach { lineTo(it.x, it.y) }
            }
        }
        drawPath(path = path, color = color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        points.forEach { point ->
            drawCircle(color = color, radius = 4.dp.toPx(), center = point)
            drawCircle(color = Color.Black, radius = 2.dp.toPx(), center = point)
        }
        val fillPath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points[0].x, height)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, height)
                close()
            }
        }
        drawPath(path = fillPath, brush = Brush.verticalGradient(colors = listOf(color.copy(alpha = 0.2f), Color.Transparent)))
    }
}

@Composable
fun StatCardModern(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
            Text(title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

data class AdminActionItem(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)
