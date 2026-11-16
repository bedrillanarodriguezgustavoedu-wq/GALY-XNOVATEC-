package com.galyxnovatec.tienda.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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
import com.galyxnovatec.tienda.data.model.MarcaCompartida
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.ui.components.MarcaLogo
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBrandsScreen(alVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val brands = ProductRepository.marcas
    var marcaEditando by remember { mutableStateOf<MarcaCompartida?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(true) }

    fun cargarMarcas() {
        scope.launch {
            cargando = true
            val ok = ProductRepository.sincronizarMarcas()
            cargando = false
            if (!ok) Toast.makeText(context, "No se pudieron cargar las marcas.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) { cargarMarcas() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Marcas compartidas", color = Color.White, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { cargarMarcas() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    marcaEditando = null
                    mostrarDialogo = true
                },
                containerColor = ColorGalyRed,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar marca")
            }
        }
    ) { padding ->
        if (cargando && brands.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding).background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ColorGalyRed)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "Los cambios se reflejan en la web y en la app. Las marcas con productos no se borran; puedes desactivarlas.",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(brands, key = { it.id }) { marca ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MarcaLogo(
                                marca = marca,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(marca.nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    "${marca.productosCount} productos · ${if (marca.activo) "Activa" else "Inactiva"}",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                            IconButton(onClick = {
                                marcaEditando = marca
                                mostrarDialogo = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar ${marca.nombre}", tint = Color.White)
                            }
                            if (marca.activo) {
                                TextButton(onClick = {
                                    scope.launch {
                                        val error = ProductRepository.desactivarMarca(marca.id)
                                        Toast.makeText(
                                            context,
                                            error ?: "Marca desactivada; sus productos se conservaron.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }) {
                                    Text("Desactivar", color = ColorGalyRed)
                                }
                            } else {
                                TextButton(onClick = {
                                    scope.launch {
                                        val error = ProductRepository.guardarMarca(marca.id, marca.nombre, true)
                                        Toast.makeText(context, error ?: "Marca activada.", Toast.LENGTH_LONG).show()
                                    }
                                }) {
                                    Text("Activar", color = Color(0xFF00FF41))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        var nombre by remember(marcaEditando?.id) { mutableStateOf(marcaEditando?.nombre.orEmpty()) }
        var guardando by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { if (!guardando) mostrarDialogo = false },
            containerColor = Color(0xFF1A1A1A),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray,
            title = { Text(if (marcaEditando == null) "Nueva marca" else "Editar marca") },
            text = {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ColorGalyRed,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.25f)
                    )
                )
            },
            confirmButton = {
                Button(
                    enabled = !guardando && nombre.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed),
                    onClick = {
                        guardando = true
                        scope.launch {
                            val error = ProductRepository.guardarMarca(
                                marcaEditando?.id,
                                nombre.trim(),
                                marcaEditando?.activo ?: true
                            )
                            guardando = false
                            if (error == null) {
                                mostrarDialogo = false
                                Toast.makeText(context, "Marca guardada en el catálogo compartido.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) {
                    if (guardando) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                    else Text("GUARDAR", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(enabled = !guardando, onClick = { mostrarDialogo = false }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        )
    }
}
