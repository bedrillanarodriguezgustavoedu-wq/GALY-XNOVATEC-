package com.galyxnovatec.tienda.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.manager.CartManager
import com.galyxnovatec.tienda.data.model.CartItem
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    alVolver: () -> Unit,
    alIrACheckout: () -> Unit
) {
    val context = LocalContext.current
    val items = CartManager.items
    val todosSeleccionados = items.isNotEmpty() && items.all { it.isSelected }
    val cantSeleccionados = CartManager.items.count { it.isSelected }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Mi Carrito", fontWeight = FontWeight.Black, color = Color.White)
                        Text("$cantSeleccionados artículos para comprar", fontSize = 11.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    if (items.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                            Text("Todo", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Checkbox(
                                checked = todosSeleccionados,
                                onCheckedChange = { CartManager.seleccionarTodo(it) },
                                colors = CheckboxDefaults.colors(checkedColor = ColorGalyRed, uncheckedColor = Color.Gray)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                            .navigationBarsPadding()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal ($cantSeleccionados items)", color = Color.Gray, fontSize = 14.sp)
                            Text("S/ ${CartManager.obtenerSubtotal().toInt()}", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        
                        if (CartManager.descuentoPorCupon > 0) {
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Descuento Cupón", color = ColorGalyRed, fontSize = 14.sp)
                                Text("- S/ ${CartManager.descuentoPorCupon.toInt()}", color = ColorGalyRed, fontWeight = FontWeight.Bold)
                            }
                        }

                        val metaEnvioGratis = 1000.0
                        val subtotal = CartManager.obtenerSubtotal()
                        val esGratis = subtotal >= metaEnvioGratis && subtotal > 0
                        
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Envío", color = Color.Gray, fontSize = 14.sp)
                            Text(
                                text = if(subtotal == 0.0) "---" else if(esGratis) "GRATIS" else "S/ 15", 
                                color = if(esGratis) Color(0xFF00FF41) else Color.White, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.1f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("TOTAL A PAGAR", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Black)
                                Text(
                                    "S/ ${CartManager.obtenerTotal().toInt()}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Button(
                                onClick = alIrACheckout,
                                modifier = Modifier.height(60.dp).width(180.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed),
                                enabled = cantSeleccionados > 0
                            ) {
                                Text("COMPRAR ($cantSeleccionados)", fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            EmptyCartView(alVolver, padding)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Barra de Progreso de Envío Gratis
                item {
                    val subtotal = CartManager.obtenerSubtotal()
                    val metaEnvioGratis = 1000.0
                    val progreso = (subtotal / metaEnvioGratis).coerceAtMost(1.0).toFloat()
                    val faltaParaGratis = (metaEnvioGratis - subtotal).coerceAtLeast(0.0)

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF111111),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocalShipping, 
                                    null, 
                                    tint = if (progreso >= 1f) Color(0xFF00FF41) else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = if (progreso >= 1f) "¡ENVÍO GRATIS ACTIVADO!" else "Envío gratis desde S/ 1,000",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { progreso },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                color = if (progreso >= 1f) Color(0xFF00FF41) else ColorGalyRed,
                                trackColor = Color.White.copy(alpha = 0.1f),
                            )
                            if (progreso < 1f && subtotal > 0) {
                                Text(
                                    "Agrega S/ ${faltaParaGratis.toInt()} más para envío gratis",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }

                items(items = items, key = { it.producto.id }) { item ->
                    ModernCartItemPremium(item = item)
                }

                // Sección de Cupón
                item {
                    var codigoCupon by remember { mutableStateOf("") }
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF111111),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.ConfirmationNumber, null, tint = ColorGalyRed, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("Código de descuento", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = codigoCupon,
                                    onValueChange = { codigoCupon = it.uppercase() },
                                    placeholder = { Text("Ingresa tu código", fontSize = 13.sp) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                                    )
                                )
                                Spacer(Modifier.width(12.dp))
                                Button(
                                    onClick = {
                                        CartManager.aplicarCupon(codigoCupon) { exito, mensaje ->
                                            Toast.makeText(
                                                context,
                                                if (exito) "✅ $mensaje" else "❌ $mensaje",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            if (exito) codigoCupon = ""
                                        }
                                    },
                                    enabled = !CartManager.validandoCupon,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                                ) {
                                    Text(if (CartManager.validandoCupon) "…" else "APLICAR", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(120.dp)) }
            }
        }
    }
}

@Composable
fun ModernCartItemPremium(item: CartItem) {
    val context = LocalContext.current
    val producto = item.producto
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = if (item.isSelected) Color(0xFF0F0F0F) else Color(0xFF050505),
        border = BorderStroke(1.dp, if(item.isSelected) ColorGalyRed.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox de selección
            Checkbox(
                checked = item.isSelected,
                onCheckedChange = { CartManager.alternarSeleccion(producto.id) },
                colors = CheckboxDefaults.colors(checkedColor = ColorGalyRed, uncheckedColor = Color.Gray)
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = producto.getFullImageUrl(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    contentScale = ContentScale.Fit,
                    error = painterResource(R.drawable.logo_2)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.marca.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Black, color = ColorGalyRed)
                Text(producto.nombre, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("S/ ${producto.precioOferta.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFF1A1A1A), CircleShape)
                            .padding(2.dp)
                    ) {
                        IconButton(onClick = { if(item.cantidad > 1) CartManager.cambiarCantidad(context, producto, item.cantidad - 1) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Remove, null, Modifier.size(12.dp), tint = Color.White)
                        }
                        Text(item.cantidad.toString(), fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { CartManager.cambiarCantidad(context, producto, item.cantidad + 1) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Add, null, Modifier.size(12.dp), tint = Color.White)
                        }
                    }
                }
            }
            
            IconButton(
                onClick = { CartManager.eliminarProducto(context, producto) },
                modifier = Modifier.align(Alignment.Top)
            ) {
                Icon(Icons.Default.Delete, null, tint = Color.Gray.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun EmptyCartView(alVolver: () -> Unit, padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(ColorGalyRed.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier.padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "empty_cart")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "scale"
            )

            Surface(
                modifier = Modifier.size(140.dp).graphicsLayer(scaleX = scale, scaleY = scale),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.ShoppingCart, 
                        null, 
                        modifier = Modifier.size(50.dp), 
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "Tu carrito está vacío", 
                fontSize = 26.sp, 
                fontWeight = FontWeight.Black, 
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Text(
                "¡No te quedes atrás! Explora lo mejor en tecnología y armas tu setup hoy mismo.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp, bottom = 40.dp)
            )
            
            Button(
                onClick = alVolver,
                colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = ColorGalyRed),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RocketLaunch, null, tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text("EXPLORAR TIENDA", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}
