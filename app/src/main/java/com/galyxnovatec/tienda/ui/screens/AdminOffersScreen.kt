package com.galyxnovatec.tienda.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.data.model.Producto
import com.galyxnovatec.tienda.data.network.Oferta
import com.galyxnovatec.tienda.data.network.OfertaRequest
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOffersScreen(alVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var offers by remember { mutableStateOf<List<Oferta>>(emptyList()) }
    var products by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var editedOffer by remember { mutableStateOf<Oferta?>(null) }
    var editorOpen by remember { mutableStateOf(false) }

    fun loadOffers() {
        scope.launch {
            loading = true
            try {
                val result = RetrofitClient.instance.getOfertas()
                offers = result
                if (products.isEmpty()) products = RetrofitClient.instance.getProductos()
            } catch (exception: Exception) {
                Toast.makeText(context, "No se pudieron cargar las ofertas: ${exception.message}", Toast.LENGTH_LONG).show()
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadOffers() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ofertas compartidas", color = Color.White, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editedOffer = null; editorOpen = true },
                containerColor = ColorGalyRed,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear oferta")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black)
        ) {
            when {
                loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = ColorGalyRed)
                offers.isEmpty() -> Text(
                    "Todavía no hay campañas. Crea una oferta para que se refleje en la web y GALY.",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    color = Color.LightGray
                )
                else -> Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Estas campañas se guardan junto con las ofertas del administrador web.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    offers.forEach { offer ->
                        OfferCard(
                            offer = offer,
                            onEdit = { editedOffer = offer; editorOpen = true },
                            onDelete = {
                                scope.launch {
                                    try {
                                        val response = RetrofitClient.instance.deleteOferta(mapOf("id" to offer.id))
                                        if (response["success"] == true) {
                                            Toast.makeText(context, "Oferta eliminada", Toast.LENGTH_SHORT).show()
                                            loadOffers()
                                        } else {
                                            Toast.makeText(context, response["message"]?.toString() ?: "No se pudo eliminar la oferta", Toast.LENGTH_LONG).show()
                                        }
                                    } catch (exception: Exception) {
                                        Toast.makeText(context, "No se pudo eliminar la oferta: ${exception.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (editorOpen) {
        OfferEditorDialog(
            existing = editedOffer,
            products = products,
            onDismiss = { editorOpen = false },
            onSave = { request ->
                scope.launch {
                    try {
                        val response = if (request.id == null) {
                            RetrofitClient.instance.addOferta(request)
                        } else {
                            RetrofitClient.instance.updateOferta(request)
                        }
                        if (response["success"] == true) {
                            Toast.makeText(context, response["message"]?.toString() ?: "Oferta guardada", Toast.LENGTH_SHORT).show()
                            editorOpen = false
                            loadOffers()
                        } else {
                            Toast.makeText(context, response["message"]?.toString() ?: "No se pudo guardar la oferta", Toast.LENGTH_LONG).show()
                        }
                    } catch (exception: Exception) {
                        Toast.makeText(context, "No se pudo guardar la oferta: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}

@Composable
private fun OfferCard(offer: Oferta, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(offer.nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(
                        if (offer.tipoDescuento == "porcentaje") "${offer.valorDescuento}% de descuento" else "S/ ${offer.valorDescuento} de descuento",
                        color = ColorGalyRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(if (offer.activo) "ACTIVA" else "PAUSADA", color = if (offer.activo) Color(0xFF00FF41) else Color.Gray, fontSize = 11.sp)
            }
            Text("${offer.fechaInicio} — ${offer.fechaFin}", color = Color.Gray, fontSize = 11.sp)
            Text(
                offer.nombresProductos.joinToString().ifBlank { "${offer.productos.size} producto(s)" },
                color = Color.LightGray,
                fontSize = 12.sp
            )
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar oferta", tint = Color.White)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar oferta", tint = ColorGalyRed)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OfferEditorDialog(
    existing: Oferta?,
    products: List<Producto>,
    onDismiss: () -> Unit,
    onSave: (OfertaRequest) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US) }
    val now = remember { Calendar.getInstance() }
    val defaultStart = remember { dateFormat.format(now.time) }
    val defaultEnd = remember {
        val end = now.clone() as Calendar
        end.add(Calendar.DAY_OF_YEAR, 7)
        dateFormat.format(end.time)
    }
    var name by remember(existing?.id) { mutableStateOf(existing?.nombre.orEmpty()) }
    var discountType by remember(existing?.id) { mutableStateOf(existing?.tipoDescuento ?: "porcentaje") }
    var discountValue by remember(existing?.id) { mutableStateOf(existing?.valorDescuento?.toString().orEmpty()) }
    var startDate by remember(existing?.id) { mutableStateOf(existing?.fechaInicio ?: defaultStart) }
    var endDate by remember(existing?.id) { mutableStateOf(existing?.fechaFin ?: defaultEnd) }
    var active by remember(existing?.id) { mutableStateOf(existing?.activo ?: true) }
    val selectedProductIds = remember(existing?.id) {
        mutableStateListOf<Int>().apply { addAll(existing?.productos.orEmpty()) }
    }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF111111),
        title = { Text(if (existing == null) "Nueva oferta" else "Editar oferta", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OfferTextField(name, { name = it }, "Nombre de campaña")
                ExposedDropdownMenuBox(
                    expanded = typeMenuExpanded,
                    onExpandedChange = { typeMenuExpanded = !typeMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = if (discountType == "porcentaje") "Porcentaje (%)" else "Monto fijo (S/)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de descuento") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                        DropdownMenuItem(text = { Text("Porcentaje (%)") }, onClick = { discountType = "porcentaje"; typeMenuExpanded = false })
                        DropdownMenuItem(text = { Text("Monto fijo (S/)") }, onClick = { discountType = "monto"; typeMenuExpanded = false })
                    }
                }
                OfferTextField(discountValue, { discountValue = it }, "Valor del descuento")
                OfferTextField(startDate, { startDate = it }, "Inicio (yyyy-MM-dd HH:mm:ss)")
                OfferTextField(endDate, { endDate = it }, "Fin (yyyy-MM-dd HH:mm:ss)")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = active, onCheckedChange = { active = it })
                    Text("Campaña activa", color = Color.White)
                }
                Text("Productos de la oferta", color = Color.White, fontWeight = FontWeight.Bold)
                if (products.isEmpty()) {
                    Text("No se pudieron cargar productos para seleccionar.", color = Color.LightGray)
                } else {
                    products.forEach { product ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = product.id in selectedProductIds,
                                onCheckedChange = { checked ->
                                    if (checked) selectedProductIds.add(product.id) else selectedProductIds.remove(product.id)
                                }
                            )
                            Text(product.nombre, color = Color.LightGray, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val value = discountValue.toDoubleOrNull()
                    if (name.isBlank() || value == null || value <= 0 || selectedProductIds.isEmpty()) return@Button
                    if (discountType == "porcentaje" && value > 100) return@Button
                    onSave(
                        OfertaRequest(
                            nombre = name.trim(),
                            tipoDescuento = discountType,
                            valorDescuento = value,
                            fechaInicio = startDate.trim(),
                            fechaFin = endDate.trim(),
                            activo = active,
                            productos = selectedProductIds.toList(),
                            id = existing?.id
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed)
            ) {
                Text("GUARDAR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.LightGray) }
        }
    )
}

@Composable
private fun OfferTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}
