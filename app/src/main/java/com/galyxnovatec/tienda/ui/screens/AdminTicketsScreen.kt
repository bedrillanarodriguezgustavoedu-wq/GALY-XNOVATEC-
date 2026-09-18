package com.galyxnovatec.tienda.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.data.model.ServicioTecnico
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTicketsScreen(alVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tickets by remember { mutableStateOf<List<ServicioTecnico>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    var selectedTicket by remember { mutableStateOf<ServicioTecnico?>(null) }

    fun cargarTickets() {
        scope.launch {
            isLoading = true
            try {
                tickets = RetrofitClient.instance.getAllTickets()
            } catch (e: Exception) { }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { cargarTickets() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Soporte", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(Color.Black)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = ColorGalyRed)
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(tickets) { ticket ->
                        AdminTicketCard(ticket) { selectedTicket = it }
                    }
                }
            }
        }
    }

    if (selectedTicket != null) {
        var nuevoEstado by remember { mutableStateOf(selectedTicket!!.estado ?: "Recibido") }
        var nuevoCosto by remember { mutableStateOf(selectedTicket!!.costoEstimado?.toString() ?: "0") }
        
        AlertDialog(
            onDismissRequest = { selectedTicket = null },
            containerColor = Color(0xFF111111),
            title = { Text("Actualizar Ticket #${selectedTicket!!.id}", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Equipo: ${selectedTicket!!.equipoModelo}", color = Color.Gray)
                    
                    Text("Estado actual:", color = Color.White, fontSize = 12.sp)
                    val estados = listOf("Recibido", "En Diagnostico", "Reparando", "Listo", "Entregado")
                    
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        estados.forEach { est ->
                            FilterChip(
                                selected = nuevoEstado == est,
                                onClick = { nuevoEstado = est },
                                label = { Text(est) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ColorGalyRed)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = nuevoCosto,
                        onValueChange = { nuevoCosto = it },
                        label = { Text("Costo Estimado (S/) ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        val res = RetrofitClient.instance.updateTicketStatus(
                            mapOf("id" to selectedTicket!!.id, "estado" to nuevoEstado, "costo" to (nuevoCosto.toDoubleOrNull() ?: 0.0))
                        )
                        if (res["success"] == true) {
                            selectedTicket = null
                            cargarTickets()
                            Toast.makeText(context, "Actualizado con éxito", Toast.LENGTH_SHORT).show()
                        }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed)) {
                    Text("GUARDAR")
                }
            }
        )
    }
}

@Composable
fun AdminTicketCard(ticket: ServicioTecnico, onClick: (ServicioTecnico) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(ticket) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F0F)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = ColorGalyRed.copy(alpha = 0.1f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Build, null, tint = ColorGalyRed, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(ticket.equipoModelo ?: "Equipo", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Cliente: ${ticket.clienteNombre ?: "N/A"}", color = Color.Gray, fontSize = 12.sp)
                }
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(ticket.estado ?: "Pendiente", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, color = Color.White)
                }
            }
            if (!ticket.falla.isNullOrEmpty()) {
                Text(ticket.falla, color = Color.LightGray, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp), maxLines = 2)
            }
        }
    }
}
