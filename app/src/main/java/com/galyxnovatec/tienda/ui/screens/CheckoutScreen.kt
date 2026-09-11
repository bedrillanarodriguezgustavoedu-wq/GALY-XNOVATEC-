package com.galyxnovatec.tienda.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.manager.AddressManager
import com.galyxnovatec.tienda.data.manager.AuthManager
import com.galyxnovatec.tienda.data.manager.CartManager
import com.galyxnovatec.tienda.data.manager.OrderManager
import com.galyxnovatec.tienda.data.model.Direccion
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    alVolver: () -> Unit,
    alIrADirecciones: () -> Unit,
    alFinalizarCompra: () -> Unit
) {
    val context = LocalContext.current
    val user = AuthManager.currentUser
    val numeroYape = "978104136"
    var selectedAddress by remember { mutableStateOf(CartManager.direccionSeleccionada) }
    var metodoSeleccionado by remember { mutableStateOf("YAPE") }
    var tipoComprobante by remember { mutableStateOf("Boleta") }
    var aceptoTerminos by remember { mutableStateOf(true) }
    var codigoCupon by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        user?.id?.let { 
            AddressManager.fetchAddresses(it)
            if (AddressManager.addresses.isNotEmpty() && CartManager.direccionSeleccionada == null) {
                selectedAddress = AddressManager.addresses.first()
                CartManager.direccionSeleccionada = selectedAddress
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Pagar (${CartManager.items.size})", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
                        Spacer(Modifier.weight(1f))
                        Image(
                            painter = painterResource(id = R.drawable.logo_2),
                            contentDescription = "GALY",
                            modifier = Modifier.height(30.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text(" GALY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
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
            // BANNERS SUPERIORES
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF001A00),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF00FF41).copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF00FF41), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Envío gratis especial", color = Color(0xFF00FF41), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF111111),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Datos protegidos", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // SECCIÓN DIRECCIÓN
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                color = Color(0xFF111111),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = Color(0xFF6200EE).copy(alpha = 0.2f)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocationOn, null, tint = Color(0xFFBB86FC), modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dirección de entrega", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(user?.nombre ?: "Cliente GALY", color = Color.Gray, fontSize = 13.sp)
                            Text(CartManager.direccionSeleccionada?.direccion ?: user?.direccion ?: "Añadir dirección", color = Color.Gray, fontSize = 13.sp)
                        }
                        TextButton(onClick = alIrADirecciones) {
                            Text("Cambiar >", color = Color(0xFFBB86FC), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = alIrADirecciones,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.dp, Color(0xFFBB86FC)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color(0xFFBB86FC))
                        Spacer(Modifier.width(8.dp))
                        Text("Agregar nueva dirección", color = Color(0xFFBB86FC), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // SECCIÓN ENVÍO
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                color = Color(0xFF111111),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.1f)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocalShipping, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Envío gratis por GALY", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Entrega estimada: 1 - 3 días", color = Color(0xFF00FF41), fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // MÉTODOS DE PAGO
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                color = Color(0xFF111111),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Métodos de pago", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(20.dp))
                    
                    PaymentRow("Tarjeta de Crédito/Débito", "Visa, Mastercard", listOf(R.drawable.logo_2), metodoSeleccionado == "CARD") { metodoSeleccionado = "CARD" }
                    PaymentRow("Yape / Plin", "Paga rápido y seguro", listOf(R.drawable.yape), metodoSeleccionado == "YAPE") { metodoSeleccionado = "YAPE" }
                    PaymentRow("Transferencia", "BCP / BBVA / Otros", emptyList(), metodoSeleccionado == "BANK") { metodoSeleccionado = "BANK" }
                    PaymentRow("Pago contra entrega", "Efectivo al recibir", emptyList(), metodoSeleccionado == "CASH") { metodoSeleccionado = "CASH" }
                }
            }

            Spacer(Modifier.height(16.dp))

            // CUPÓN
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                color = Color(0xFF111111),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.ConfirmationNumber, null, tint = Color.White)
                        Spacer(Modifier.width(12.dp))
                        Text("¿Tienes un cupón?", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row {
                        OutlinedTextField(
                            value = codigoCupon, 
                            onValueChange = { codigoCupon = it }, 
                            placeholder = { Text("Ingresa tu código", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { 
                                if(CartManager.aplicarCupon(codigoCupon)) {
                                    Toast.makeText(context, "✅ Cupón Aplicado", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "❌ Cupón Inválido", Toast.LENGTH_SHORT).show()
                                }
                            }, 
                            colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed), 
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Aplicar")
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // RESUMEN
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                color = Color(0xFF111111),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Resumen del pedido", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    CostRow("Subtotal", "S/ ${CartManager.obtenerSubtotal().toInt()}")
                    if (CartManager.descuentoPorCupon > 0) {
                        CostRow("Descuento Cupón", "- S/ ${CartManager.descuentoPorCupon.toInt()}", color = ColorGalyRed)
                    }
                    CostRow("Envío", "GRATIS", color = Color(0xFF00FF41))
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total a pagar", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                        Text("S/ ${CartManager.obtenerTotal().toInt()}", fontWeight = FontWeight.Black, fontSize = 22.sp, color = ColorGalyRed)
                    }
                }
            }

            // TÉRMINOS
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = aceptoTerminos, onCheckedChange = { aceptoTerminos = it }, colors = CheckboxDefaults.colors(checkedColor = ColorGalyRed))
                Text("Acepto los términos y política de privacidad.", color = Color.Gray, fontSize = 12.sp)
            }

    // BOTÓN PAGAR GIGANTE Y FUNCIONAL
    Button(
        onClick = { 
            scope.launch {
                val totalFinal = CartManager.obtenerTotal()
                val result = OrderManager.placeOrder(
                    userId = user?.id, 
                    items = CartManager.items, 
                    total = totalFinal,
                    metodoPago = metodoSeleccionado,
                    direccion = CartManager.direccionSeleccionada
                )
                
                if (result == true) {
                    val phoneGaly = "51978104136" // Formato internacional
                    val mensaje = CartManager.generarMensajeWhatsApp(CartManager.direccionSeleccionada)
                    
                    when (metodoSeleccionado) {
                        "YAPE" -> {
                            // 1. Copiar número al portapapeles
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Yape GALY", "978104136"))
                            Toast.makeText(context, "✅ Número 978104136 copiado. ¡Abre Yape para pagar!", Toast.LENGTH_LONG).show()
                            
                            // 2. Intentar abrir Yape
                            val intentYape = context.packageManager.getLaunchIntentForPackage("com.bcp.innovabcp.yape")
                            if (intentYape != null) {
                                context.startActivity(intentYape)
                            } else {
                                // Si no tiene Yape, abrir WhatsApp para coordinar
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phoneGaly?text=${Uri.encode(mensaje)}")))
                            }
                        }
                        "CARD" -> {
                            // Para tarjeta, abrimos WhatsApp para que el admin envíe el link de pago (Niubiz/Izipay)
                            val msgCard = "Hola GALY, deseo pagar con Tarjeta mi pedido de S/ ${totalFinal.toInt()}. ¿Me envían el link?"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phoneGaly?text=${Uri.encode(msgCard)}")))
                        }
                        "BANK" -> {
                            // Para transferencia, enviamos el detalle por WhatsApp
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phoneGaly?text=${Uri.encode(mensaje)}")))
                        }
                        else -> {
                            // Pago contra entrega o cualquier otro
                            Toast.makeText(context, "📦 Pedido registrado. Nos contactaremos pronto.", Toast.LENGTH_LONG).show()
                        }
                    }
                    
                    delay(500)
                    CartManager.limpiarCarrito()
                    alFinalizarCompra()
                } else {
                    val errorMsg = if (result is String) result else "Error al procesar el pedido"
                    Toast.makeText(context, "❌ $errorMsg", Toast.LENGTH_LONG).show()
                }
            }
        },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed),
                enabled = aceptoTerminos && CartManager.items.isNotEmpty() && !OrderManager.isLoading
            ) {
                if (OrderManager.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("PAGAR S/ ${CartManager.obtenerTotal().toInt()}", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PaymentRow(title: String, desc: String, icons: List<Int>, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = ColorGalyRed))
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(desc, color = Color.Gray, fontSize = 11.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icons.forEach { Image(painter = painterResource(id = it), contentDescription = null, modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun CostRow(label: String, value: String, color: Color = Color.Gray) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
