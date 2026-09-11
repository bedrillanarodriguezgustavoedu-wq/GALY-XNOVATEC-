package com.galyxnovatec.tienda.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
    val items = CartManager.items

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Mi Carrito", fontWeight = FontWeight.Black, color = Color.White)
                        Text("${items.size} artículos seleccionados", fontSize = 11.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF0A0A0A),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .navigationBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Subtotal", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(
                                    "S/ ${CartManager.obtenerTotal().toInt()}",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Button(
                                onClick = alIrACheckout,
                                modifier = Modifier.height(56.dp).width(200.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed)
                            ) {
                                Text("PAGAR AHORA", fontWeight = FontWeight.Black, fontSize = 14.sp)
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
                // Banner de Envío Gratis
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF002200),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ShoppingCart, null, tint = Color(0xFF00FF41), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("¡Felicidades! Tienes envío GRATIS en este pedido.", color = Color(0xFF00FF41), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(items) { item ->
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
                                    onValueChange = { codigoCupon = it },
                                    placeholder = { Text("GALY2026", fontSize = 13.sp) },
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
                                    onClick = { if(CartManager.aplicarCupon(codigoCupon)) codigoCupon = "" },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                                ) {
                                    Text("APLICAR", fontWeight = FontWeight.Bold)
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
    val producto = item.producto
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF111111),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
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
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.marca.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = ColorGalyRed, letterSpacing = 1.sp)
                Text(producto.nombre, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("S/ ${producto.precioOferta.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFF1A1A1A), CircleShape)
                            .padding(4.dp)
                    ) {
                        IconButton(onClick = { if(item.cantidad > 1) CartManager.cambiarCantidad(producto, item.cantidad - 1) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Remove, null, Modifier.size(14.dp), tint = Color.White)
                        }
                        Text(item.cantidad.toString(), fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp))
                        IconButton(onClick = { CartManager.cambiarCantidad(producto, item.cantidad + 1) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Add, null, Modifier.size(14.dp), tint = Color.White)
                        }
                    }
                }
            }
            
            IconButton(
                onClick = { CartManager.eliminarProducto(producto) },
                modifier = Modifier.align(Alignment.Top).offset(x = 8.dp, y = -8.dp)
            ) {
                Icon(Icons.Default.Delete, null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun EmptyCartView(alVolver: () -> Unit, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Color.Black)
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(160.dp),
            shape = CircleShape,
            color = Color(0xFF111111)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(60.dp), tint = Color.DarkGray)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Tu carrito está vacío", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text(
            "Encuentra los mejores productos tecnológicos en nuestra tienda.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, bottom = 32.dp)
        )
        
        Button(
            onClick = alVolver,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("EXPLORAR TIENDA", fontWeight = FontWeight.ExtraBold)
        }
    }
}
