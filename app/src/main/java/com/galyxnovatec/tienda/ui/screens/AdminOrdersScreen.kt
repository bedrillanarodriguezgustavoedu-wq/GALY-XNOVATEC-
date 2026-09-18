package com.galyxnovatec.tienda.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.galyxnovatec.tienda.data.manager.OrderManager
import com.galyxnovatec.tienda.data.manager.PdfManager
import com.galyxnovatec.tienda.data.model.Pedido
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(alVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val orders = OrderManager.orders
    val isLoading = OrderManager.isLoading

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("TODOS") }
    val statuses = listOf("TODOS", "PENDIENTE", "PAGADO", "PREPARANDO", "EN CAMINO", "ENTREGADO")

    val filteredOrders = remember(orders, searchQuery, selectedStatus) {
        orders.filter { order ->
            val query = searchQuery.lowercase()
            val matchesSearch = (order.clienteNombre?.lowercase()?.contains(query) ?: false) ||
                               (order.clienteEmail?.lowercase()?.contains(query) ?: false) ||
                               order.id.toString().contains(query)
            
            val matchesStatus = if (selectedStatus == "TODOS") true else order.estado.equals(selectedStatus, ignoreCase = true)
            matchesSearch && matchesStatus
        }
    }

    val totalVentas = remember(filteredOrders) { filteredOrders.sumOf { it.total } }

    LaunchedEffect(Unit) {
        OrderManager.fetchAllOrders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Pedidos", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        PdfManager.generarReportePedidos(context, filteredOrders)
                    }) {
                        Icon(Icons.Default.PictureAsPdf, "Reporte PDF", tint = Color.White)
                    }
                    IconButton(onClick = {
                        // Export to CSV
                        val csvData = StringBuilder()
                        csvData.append("ID,Fecha,Cliente,Total,Estado\n")
                        filteredOrders.forEach { o ->
                            csvData.append("${o.id},${o.fecha},${o.clienteNombre},${o.total},${o.estado}\n")
                        }
                        try {
                            val file = File(context.cacheDir, "Ventas_GALY.csv")
                            file.writeText(csvData.toString())
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Descargar Reporte"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error al exportar", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.FileDownload, "Excel", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black)) {
            // Barra de Búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Buscar cliente o # orden", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = ColorGalyRed) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = ColorGalyRed,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                )
            )

            // Filtros
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(items = statuses) { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        label = { Text(status) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ColorGalyRed,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.05f),
                            labelColor = Color.Gray
                        )
                    )
                }
            }

            // Banner Resumen
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F0F)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("TOTAL FILTRADO", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text("S/ ${totalVentas.toInt()}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                    Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Color(0xFF00FF41), modifier = Modifier.size(32.dp))
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading && orders.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = ColorGalyRed)
                } else {
                    val pullToRefreshState = rememberPullToRefreshState()
                    PullToRefreshBox(
                        isRefreshing = isLoading,
                        onRefresh = { scope.launch { OrderManager.fetchAllOrders() } },
                        state = pullToRefreshState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(items = filteredOrders, key = { it.id }) { order ->
                                AdminOrderCard(order) { newStatus ->
                                    scope.launch {
                                        val success = OrderManager.updateOrderStatus(order.id, newStatus)
                                        if (success) {
                                            Toast.makeText(context, "Actualizado a $newStatus", Toast.LENGTH_SHORT).show()
                                            OrderManager.fetchAllOrders()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrderCard(order: Pedido, onStatusChange: (String) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    var showDetail by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDetail = true },
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF0F0F0F),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = ColorGalyRed.copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = ColorGalyRed, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = order.clienteNombre ?: "Usuario GALY",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        Text(
                            "Orden #00${order.id} • ${order.fecha.take(10)}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF111111))
                    ) {
                        DropdownMenuItem(
                            text = { Text("M. PAGADO", color = Color.White) },
                            onClick = { onStatusChange("PAGADO"); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("M. ENTREGADO", color = Color.White) },
                            onClick = { onStatusChange("ENTREGADO"); showMenu = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("TOTAL VENTA", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text("S/ ${order.total.toInt()}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
                StatusBadge(order.estado)
            }

            Row(
                modifier = Modifier.padding(top = 20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallActionButton(Icons.Default.Email, Color.Yellow) { PdfManager.enviarBoletaPorEmail(context, order) }
                SmallActionButton(Icons.AutoMirrored.Filled.Chat, Color(0xFF00FF41)) { PdfManager.enviarBoletaPorWhatsApp(context, order) }
                SmallActionButton(Icons.Default.RemoveRedEye, Color.Cyan) { showDetail = true }
            }
        }
    }

    if (showDetail) {
        AlertDialog(
            onDismissRequest = { showDetail = false },
            containerColor = Color(0xFF0A0A0A),
            title = { Text("Detalle Orden #00${order.id}", color = Color.White, fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Dirección: ${order.direccion}", color = Color.LightGray)
                    Text("Método: ${order.metodoPago}", color = Color.LightGray)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                    order.productos?.forEach { prod ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(prod.nombre ?: "Producto", color = Color.White, modifier = Modifier.weight(1f))
                            Text("x${prod.cantidad}", color = ColorGalyRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetail = false }) { Text("CERRAR", color = Color.White) }
            }
        )
    }
}

@Composable
fun SmallActionButton(icon: ImageVector, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(40.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }
    }
}
