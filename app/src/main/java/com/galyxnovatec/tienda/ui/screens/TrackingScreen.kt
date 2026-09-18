package com.galyxnovatec.tienda.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.data.manager.OrderManager
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    orderId: Int,
    alVolver: () -> Unit
) {
    val order = OrderManager.orders.find { it.id == orderId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seguimiento de Pedido", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        if (order == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Cargando información...", color = Color.Gray)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.Black)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Cabecera Premium
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("ORDEN #${order.id}", color = ColorGalyRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Text(order.productos?.firstOrNull()?.nombre ?: "Pedido Tecnológico", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(vertical = 4.dp))
                        Text("Fecha: ${order.fecha}", color = Color.Gray, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Línea de Tiempo
                val currentStatus = order.estado.uppercase()
                
                TrackingStep(
                    title = "Pedido Recibido",
                    desc = "Estamos procesando tu orden.",
                    icon = Icons.Default.Receipt,
                    isCompleted = true,
                    isActive = currentStatus == "PENDIENTE"
                )
                
                TrackingConnector(isCompleted = currentStatus != "PENDIENTE")
                
                TrackingStep(
                    title = "Pago Verificado",
                    desc = "Tu pago ha sido validado con éxito.",
                    icon = Icons.Default.Payments,
                    isCompleted = listOf("PAGADO", "PREPARANDO", "EN CAMINO", "ENTREGADO").contains(currentStatus),
                    isActive = currentStatus == "PAGADO"
                )

                TrackingConnector(isCompleted = listOf("PREPARANDO", "EN CAMINO", "ENTREGADO").contains(currentStatus))

                TrackingStep(
                    title = "En Preparación",
                    desc = "Estamos empaquetando tus productos.",
                    icon = Icons.Default.Inventory,
                    isCompleted = listOf("PREPARANDO", "EN CAMINO", "ENTREGADO").contains(currentStatus),
                    isActive = currentStatus == "PREPARANDO"
                )

                TrackingConnector(isCompleted = listOf("EN CAMINO", "ENTREGADO").contains(currentStatus))

                TrackingStep(
                    title = "En Camino",
                    desc = "Tu pedido está rumbo a su destino.",
                    icon = Icons.Default.LocalShipping,
                    isCompleted = listOf("EN CAMINO", "ENTREGADO").contains(currentStatus),
                    isActive = currentStatus == "EN CAMINO"
                )

                TrackingConnector(isCompleted = currentStatus == "ENTREGADO")

                TrackingStep(
                    title = "Entregado",
                    desc = "¡Disfruta tu nueva tecnología!",
                    icon = Icons.Default.CheckCircle,
                    isCompleted = currentStatus == "ENTREGADO",
                    isActive = currentStatus == "ENTREGADO",
                    isLast = true
                )
            }
        }
    }
}

@Composable
fun TrackingStep(
    title: String,
    desc: String,
    icon: ImageVector,
    isCompleted: Boolean,
    isActive: Boolean,
    isLast: Boolean = false
) {
    Row(verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = if (isCompleted) ColorGalyRed else Color(0xFF1A1A1A),
                border = if (isActive) BorderStroke(2.dp, Color.White) else null
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isCompleted) Color.White else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        Spacer(Modifier.width(20.dp))
        
        Column {
            Text(
                text = title,
                color = if (isCompleted) Color.White else Color.Gray,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
            Text(
                text = desc,
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            if (!isLast) Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun TrackingConnector(isCompleted: Boolean) {
    Box(
        modifier = Modifier
            .padding(start = 20.dp)
            .width(2.dp)
            .height(30.dp)
            .background(if (isCompleted) ColorGalyRed else Color(0xFF1A1A1A))
    )
}
