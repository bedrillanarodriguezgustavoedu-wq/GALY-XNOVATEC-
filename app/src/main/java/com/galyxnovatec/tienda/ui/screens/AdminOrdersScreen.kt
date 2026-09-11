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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.data.manager.OrderManager
import com.galyxnovatec.tienda.data.model.Pedido
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(alVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val orders = OrderManager.orders
    val isLoading = OrderManager.isLoading

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("TODOS") }
    val statuses = listOf("TODOS", "PENDIENTE", "PAGADO", "ENVIADO", "ENTREGADO")

    val filteredOrders = remember(orders, searchQuery, selectedStatus) {
        orders.filter { order ->
            val matchesSearch = (order.clienteNombre?.contains(searchQuery, ignoreCase = true) ?: false) ||
                               order.id.toString().contains(searchQuery)
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black)) {
            // Barra de Búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar por cliente o # orden", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = ColorGalyRed) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = ColorGalyRed,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                ),
                singleLine = true
            )

            // Filtros de Estado
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(statuses) { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        label = { Text(status) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ColorGalyRed,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.05f),
                            labelColor = Color.Gray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedStatus == status,
                            borderColor = Color.White.copy(alpha = 0.1f),
                            selectedBorderColor = ColorGalyRed,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.dp
                        )
                    )
                }
            }

            // Banner de Resumen
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("TOTAL FILTRADO", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("S/ ${totalVentas.toInt()}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                    Surface(
                        color = ColorGalyRed.copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.TrendingUp, null, tint = ColorGalyRed)
                        }
                    }
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
                            items(filteredOrders) { order ->
                                AdminOrderCard(order) { newStatus ->
                                    scope.launch {
                                        val success = OrderManager.updateOrderStatus(order.id, newStatus)
                                        if (success) {
                                            Toast.makeText(context, "Estado actualizado a $newStatus", Toast.LENGTH_SHORT).show()
                                            OrderManager.fetchAllOrders()
                                        } else {
                                            Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                            if (filteredOrders.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                                        Text("No se encontraron pedidos", color = Color.Gray)
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
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Cliente: ${order.clienteNombre ?: "Usuario GALY"}", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                    Text("Orden #${order.id} - ${order.fecha}", color = Color.Gray, fontSize = 12.sp)
                    
                    if (!order.clienteTelefono.isNullOrEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp).clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.clienteTelefono}"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {}
                            }
                        ) {
                            Icon(Icons.Default.Phone, null, tint = Color.Cyan, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(order.clienteTelefono, color = Color.Cyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF222222))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Marcar PAGADO", color = Color.White) },
                            onClick = { onStatusChange("PAGADO"); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Assignment, null, tint = Color.Green) }
                        )
                        DropdownMenuItem(
                            text = { Text("Marcar ENVIADO", color = Color.White) },
                            onClick = { onStatusChange("ENVIADO"); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.LocalShipping, null, tint = Color.Yellow) }
                        )
                        DropdownMenuItem(
                            text = { Text("Marcar ENTREGADO", color = Color.White) },
                            onClick = { onStatusChange("ENTREGADO"); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, null, tint = Color.Cyan) }
                        )
                    }
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
            
            // DIRECCIÓN DE ENTREGA (NUEVO)
            if (!order.direccion.isNullOrEmpty()) {
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = ColorGalyRed, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Entrega en:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "${order.direccion}, ${order.distrito ?: ""} ${order.ciudad ?: ""}",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            if (!order.referencia.isNullOrEmpty()) {
                                Text("Ref: ${order.referencia}", color = Color.LightGray, fontSize = 11.sp)
                            }
                            
                            // BOTÓN DE MAPA (MEJORADO)
                            val query = Uri.encode("${order.direccion}, ${order.distrito ?: ""}, ${order.ciudad ?: ""}")
                            val mapUrl = if (!order.mapsUrl.isNullOrEmpty()) order.mapsUrl 
                                         else "https://www.google.com/maps/search/?api=1&query=$query"

                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No se pudo abrir el mapa", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, ColorGalyRed),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Map, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("VER EN MAPA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            order.productos?.forEach { prod ->
                Text("• ${prod.nombre} (x${prod.cantidad})", color = Color.LightGray, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Método: ${order.metodoPago ?: "YAPE"}", color = Color.Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("S/ ${order.total.toInt()}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                StatusBadge(order.estado)
            }
        }
    }
}
