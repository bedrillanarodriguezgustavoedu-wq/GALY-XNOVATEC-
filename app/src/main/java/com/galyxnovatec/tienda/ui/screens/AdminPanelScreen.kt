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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.galyxnovatec.tienda.data.network.SalesData
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    alVolver: () -> Unit,
    alIrAAgregarProducto: () -> Unit,
    alIrACategorias: () -> Unit,
    alIrAPedidos: () -> Unit,
    alIrABanners: () -> Unit,
    alIrAUsuarios: () -> Unit,
    alIrAGestionarCategorias: () -> Unit,
    alIrAGestionarProductos: () -> Unit,
) {
    var stats by remember { mutableStateOf(mapOf("ventas" to "---", "pedidos" to "---", "usuarios" to "---", "stock" to "---")) }
    var chartData by remember { mutableStateOf<List<SalesData>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val res = RetrofitClient.instance.getAdminStats()
            if (res["success"] == true) {
                stats = res.mapValues { it.value.toString() }
            }
            chartData = RetrofitClient.instance.getSalesChartData()
        } catch (e: Exception) { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Dashboard GALY", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
                        Text("Panel de Control Maestro", fontSize = 11.sp, color = ColorGalyRed, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
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
            // SECCIÓN: RESUMEN DE NEGOCIO (CARTAS DE ESTADO)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    StatCardModern(Modifier.weight(1f), "Ventas", stats["ventas"] ?: "S/ 0", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF00FF41))
                    StatCardModern(Modifier.weight(1f), "Pedidos", stats["pedidos"] ?: "0", Icons.Default.ShoppingCart, ColorGalyRed)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    StatCardModern(Modifier.weight(1f), "Usuarios", stats["usuarios"] ?: "0", Icons.Default.Group, Color.Cyan)
                    StatCardModern(Modifier.weight(1f), "Stock", stats["stock"] ?: "85%", Icons.Default.Inventory, Color.Yellow)
                }
            }

            // GRÁFICA DE VENTAS
            if (chartData.isNotEmpty()) {
                item {
                    SalesChartCard(chartData)
                }
            }

            item {
                Text(
                    "Operaciones Principales",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            // ACCIONES EN GRILLA (Simulada con Column)
            item {
                AdminActionRow(
                    items = listOf(
                        AdminActionItem("Inventario", Icons.Default.Inventory, Color.White, alIrAGestionarProductos),
                        AdminActionItem("Nuevo Prod.", Icons.Default.AddBox, Color.White, alIrAAgregarProducto)
                    )
                )
            }

            item {
                AdminActionRow(
                    items = listOf(
                        AdminActionItem("Pedidos", Icons.AutoMirrored.Filled.ReceiptLong, Color(0xFF00FF41), alIrAPedidos),
                        AdminActionItem("Banners", Icons.Default.AdUnits, ColorGalyRed, alIrABanners)
                    )
                )
            }

            item {
                AdminActionRow(
                    items = listOf(
                        AdminActionItem("Clientes", Icons.Default.People, Color.Cyan, alIrAUsuarios),
                        AdminActionItem("Categorías", Icons.Default.GridView, Color.LightGray, alIrAGestionarCategorias)
                    )
                )
            }
            
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF1A1A1A),
                    border = BorderStroke(1.dp, ColorGalyRed.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Asistente del Servidor", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Tu base de datos está sincronizada", color = Color.Gray, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.CloudDone, null, tint = Color(0xFF00FF41))
                    }
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
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
        val spacing = width / (data.size - 1)
        
        val points = data.mapIndexed { index, item ->
            val x = index * spacing
            val y = height - (item.total.toFloat() / maxSales.toFloat() * height)
            Offset(x, y)
        }
        
        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            points.forEach { lineTo(it.x, it.y) }
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Círculos en los puntos
        points.forEach { point ->
            drawCircle(color = color, radius = 4.dp.toPx(), center = point)
            drawCircle(color = Color.Black, radius = 2.dp.toPx(), center = point)
        }
        
        // Área rellena suave
        val fillPath = Path().apply {
            moveTo(points[0].x, height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, height)
            close()
        }
        
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.2f), Color.Transparent)
            )
        )
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

@Composable
fun AdminActionRow(items: List<AdminActionItem>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items.forEach { item ->
            AdminSquareAction(modifier = Modifier.weight(1f), item = item)
        }
    }
}

@Composable
fun AdminSquareAction(modifier: Modifier, item: AdminActionItem) {
    Surface(
        modifier = modifier
            .height(110.dp)
            .clickable { item.onClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF111111),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(item.icon, null, tint = item.color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(12.dp))
            Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}
