package com.galyxnovatec.tienda.ui.screens

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.data.manager.AuthManager
import com.galyxnovatec.tienda.data.manager.OrderManager
import com.galyxnovatec.tienda.data.manager.PdfManager
import com.galyxnovatec.tienda.data.model.Pedido
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    alVolver: () -> Unit
) {
    val context = LocalContext.current
    val orders = OrderManager.orders
    val user = AuthManager.currentUser

    LaunchedEffect(Unit) {
        user?.id?.let { OrderManager.fetchOrders(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Historial de Compras", fontWeight = FontWeight.Black, color = Color.White) },
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
            if (OrderManager.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ColorGalyRed)
                }
            } else if (orders.isEmpty()) {
                EmptyOrdersView(alVolver)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(orders) { order ->
                        OrderCardPremium(order) {
                            PdfManager.generarBoleta(context, order)
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun OrderCardPremium(order: Pedido, onDownloadPdf: () -> Unit) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = ColorGalyRed.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Receipt, null, tint = ColorGalyRed, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("Orden #${order.id}", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                }
                StatusBadge(order.estado)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Fecha de compra: ${order.fecha}", color = Color.Gray, fontSize = 13.sp)
            
            // DETALLE DE PRODUCTOS (NUEVO)
            if (!order.productos.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                order.productos.forEach { prod ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(prod.nombre ?: "Producto", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text("x${prod.cantidad}", color = ColorGalyRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // DIRECCIÓN DE ENTREGA (NUEVO)
            if (!order.direccion.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = ColorGalyRed, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Entrega en:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "${order.direccion}, ${order.distrito ?: ""} ${order.ciudad ?: ""}",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        if (!order.referencia.isNullOrEmpty()) {
                            Text("Ref: ${order.referencia}", color = Color.DarkGray, fontSize = 10.sp, fontStyle = FontStyle.Italic)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Total Pagado", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("S/ ${order.total.toInt()}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
                
                Button(
                    onClick = onDownloadPdf,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("BOLETA", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when(status.uppercase()) {
        "PAGADO", "ENTREGADO" -> Color(0xFF00FF41)
        "CANCELADO" -> Color.Red
        else -> Color.Yellow
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun EmptyOrdersView(onGoShopping: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = Color(0xFF111111)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ShoppingBag, null, Modifier.size(50.dp), tint = Color.DarkGray)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Aún no tienes pedidos", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text(
                "¡Es un buen momento para renovar tu tecnología!",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onGoShopping,
                colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("EXPLORAR CATÁLOGO", fontWeight = FontWeight.Black)
            }
        }
    }
}
