package com.galyxnovatec.tienda.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.manager.FavoriteManager
import com.galyxnovatec.tienda.data.model.Producto
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaProducto(
    producto: Producto,
    alHacerClick: (Producto) -> Unit,
    alAgregarRapido: (Producto) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val esFavorito = FavoriteManager.esFavorito(producto)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { alHacerClick(producto) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                AsyncImage(
                    model = producto.getFullImageUrl(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(12.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit,
                    error = painterResource(R.drawable.logo_2), // Logo de respaldo si falla
                    placeholder = painterResource(R.drawable.logo_2) // Mientras carga
                )
                
                // Botón Favorito
                IconButton(
                    onClick = { FavoriteManager.toggleFavorito(producto) },
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (esFavorito) ColorGalyRed else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (producto.descuento != null && producto.descuento > 0) {
                    Surface(
                        color = ColorGalyRed,
                        shape = CircleShape,
                        modifier = Modifier.align(Alignment.TopStart).padding(12.dp).size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "-${producto.descuento}%",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFB400), modifier = Modifier.size(10.dp))
                    Text(" 4.8", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(" (2.3k)", fontSize = 10.sp, color = Color.DarkGray)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = producto.marca, 
                    fontSize = 11.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = Color.Gray
                )
                Text(
                    text = producto.nombre, 
                    fontSize = 13.sp, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis, 
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        if (producto.descuento != null && producto.descuento > 0) {
                            Text(
                                "S/ ${producto.precioNormal.toInt()}",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                style = TextStyle(textDecoration = TextDecoration.LineThrough)
                            )
                        }
                        Text(
                            producto.getPrecioOfertaFormateado(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    
                    // Botón Carrito Circular Estilo Temu
                    Surface(
                        onClick = { alAgregarRapido(producto) },
                        modifier = Modifier.size(34.dp),
                        shape = CircleShape,
                        color = ColorGalyRed
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
