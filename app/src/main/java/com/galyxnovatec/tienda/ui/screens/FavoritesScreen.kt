package com.galyxnovatec.tienda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.galyxnovatec.tienda.data.manager.FavoriteManager
import com.galyxnovatec.tienda.data.model.Producto
import com.galyxnovatec.tienda.ui.components.TarjetaProducto

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.data.manager.CartManager
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    alSeleccionarProducto: (Producto) -> Unit,
    alVolver: () -> Unit
) {
    val items = FavoriteManager.items

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deseos Tecnológicos", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(modifier = Modifier.size(120.dp), shape = CircleShape, color = Color(0xFF1A1A1A)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Favorite, null, tint = Color.DarkGray, modifier = Modifier.size(60.dp))
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text("¿Aún no te has enamorado?", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
                Text(
                    "Guarda aquí los equipos que más te gustan para comprarlos después.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 40.dp, vertical = 8.dp),
                    fontSize = 14.sp
                )
                Button(
                    onClick = alVolver,
                    modifier = Modifier.padding(top = 20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("EXPLORAR TIENDA")
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(span = { GridItemSpan(2) }) {
                    Text("Tienes ${items.size} artículos guardados", fontSize = 14.sp, color = Color.Gray)
                }
                items(items) { producto ->
                    TarjetaProducto(
                        producto = producto,
                        alHacerClick = { alSeleccionarProducto(producto) },
                        alAgregarRapido = { CartManager.agregarProducto(producto, 1) }
                    )
                }
            }
        }
    }
}
