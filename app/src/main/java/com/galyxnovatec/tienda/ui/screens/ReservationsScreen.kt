package com.galyxnovatec.tienda.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.galyxnovatec.tienda.data.manager.AuthManager
import com.galyxnovatec.tienda.data.model.ReservaProducto
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationsScreen(alVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var reservations by remember { mutableStateOf<List<ReservaProducto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val userId = AuthManager.currentUser?.id

    fun loadReservations() {
        scope.launch {
            isLoading = true
            error = null
            try {
                reservations = if (userId == null) emptyList() else ProductRepository.obtenerReservasUsuario(userId)
            } catch (exception: Exception) {
                error = exception.message ?: "No se pudieron cargar las reservas."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(userId) { loadReservations() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis reservas", color = Color.White, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { loadReservations() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding).background(Color.Black),
            contentAlignment = if (isLoading || error != null || reservations.isEmpty()) Alignment.Center else Alignment.TopCenter
        ) {
            when {
                isLoading -> CircularProgressIndicator(color = ColorGalyRed)
                error != null -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text(error.orEmpty(), color = Color.White)
                    TextButton(onClick = { loadReservations() }) { Text("Reintentar") }
                }
                reservations.isEmpty() -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(42.dp))
                    Text("Todavía no tienes productos apartados.", color = Color.LightGray, modifier = Modifier.padding(top = 12.dp))
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reservations, key = { it.id }) { reservation ->
                        ReservationCard(
                            reservation = reservation,
                            onCancel = {
                                scope.launch {
                                    val message = userId?.let { ProductRepository.cancelarReserva(it, reservation.id) }
                                    if (message == null) {
                                        Toast.makeText(context, "Reserva cancelada; el stock está disponible.", Toast.LENGTH_LONG).show()
                                        loadReservations()
                                    } else {
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationCard(reservation: ReservaProducto, onCancel: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = reservation.getImageUrl(),
                contentDescription = reservation.productoNombre,
                modifier = Modifier.size(72.dp)
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(reservation.productoNombre, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${reservation.cantidad} unidad(es)", color = Color.LightGray, fontSize = 12.sp)
                Text(
                    if (reservation.estado == "activa") "Vence: ${reservation.expiraAt}" else "Estado: ${reservation.estado}",
                    color = if (reservation.estado == "activa") Color(0xFFFFD54F) else Color.Gray,
                    fontSize = 11.sp
                )
                if (reservation.pedidoId != null) Text("Pedido #${reservation.pedidoId}", color = Color.LightGray, fontSize = 11.sp)
            }
            if (reservation.estado == "activa") {
                TextButton(onClick = onCancel) { Text("Cancelar", color = ColorGalyRed) }
            }
        }
    }
}
