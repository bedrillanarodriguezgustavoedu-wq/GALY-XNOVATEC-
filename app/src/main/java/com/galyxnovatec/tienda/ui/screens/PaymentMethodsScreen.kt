package com.galyxnovatec.tienda.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.ui.theme.ColorGalyGold
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class PaymentMethodItem(
    val id: String,
    val alias: String, // ej. "Mi Tarjeta BCP", "Yape Personal"
    val type: String,  // "CARD", "YAPE", "PLIN"
    val lastFour: String,
    val cardHolder: String,
    val expiry: String = "",
    val isDefault: Boolean = false
)

object PaymentMethodManager {
    private const val PREF_NAME = "galy_payment_methods_pref"
    private const val KEY_METHODS = "saved_methods"
    private val gson = Gson()

    fun getSavedMethods(context: Context): List<PaymentMethodItem> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_METHODS, null) ?: return defaultInitialMethods()
        return try {
            val type = object : TypeToken<List<PaymentMethodItem>>() {}.type
            gson.fromJson(json, type) ?: defaultInitialMethods()
        } catch (e: Exception) {
            defaultInitialMethods()
        }
    }

    fun saveMethods(context: Context, list: List<PaymentMethodItem>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_METHODS, gson.toJson(list)).apply()
    }

    private fun defaultInitialMethods(): List<PaymentMethodItem> {
        return listOf(
            PaymentMethodItem(
                id = "1",
                alias = "Yape Móvil",
                type = "YAPE",
                lastFour = "YAPE",
                cardHolder = "Número vinculado",
                isDefault = true
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var methods by remember { mutableStateOf(PaymentMethodManager.getSavedMethods(context)) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Campos del formulario
    var alias by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var methodType by remember { mutableStateOf("YAPE") } // YAPE, PLIN

    fun updateAndSave(newList: List<PaymentMethodItem>) {
        methods = newList
        PaymentMethodManager.saveMethods(context, newList)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Métodos de Pago", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ColorGalyGold,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Método")
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Tus métodos de pago guardados para compras rápidas",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (methods.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No tienes métodos de pago guardados", color = Color.Gray, fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(methods, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
                            border = BorderStroke(
                                1.dp,
                                if (item.isDefault) ColorGalyGold else Color.White.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            if (item.type == "CARD") Color(0xFF2A2A2A) else ColorGalyGold.copy(alpha = 0.2f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (item.type) {
                                            "YAPE", "PLIN" -> Icons.Default.PhoneAndroid
                                            else -> Icons.Default.CreditCard
                                        },
                                        contentDescription = null,
                                        tint = if (item.type == "CARD") Color.White else ColorGalyGold
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            item.alias,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        if (item.isDefault) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                color = ColorGalyGold.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    "Predeterminado",
                                                    color = ColorGalyGold,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (item.type == "CARD") "**** **** **** ${item.lastFour}" else item.cardHolder,
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                    if (item.expiry.isNotBlank()) {
                                        Text("Vence: ${item.expiry}", color = Color.Gray, fontSize = 12.sp)
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        val updated = methods.map {
                                            it.copy(isDefault = it.id == item.id)
                                        }
                                        updateAndSave(updated)
                                        Toast.makeText(context, "Método predeterminado actualizado", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (item.isDefault) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Predeterminado",
                                        tint = if (item.isDefault) ColorGalyGold else Color.Gray
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val updated = methods.filter { it.id != item.id }
                                        updateAndSave(updated)
                                        Toast.makeText(context, "Método eliminado", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ColorGalyRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Agregar Método de Pago", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(
                            selected = methodType == "YAPE",
                            onClick = { methodType = "YAPE" },
                            label = { Text("Yape") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ColorGalyGold,
                                selectedLabelColor = Color.Black
                            )
                        )
                        FilterChip(
                            selected = methodType == "PLIN",
                            onClick = { methodType = "PLIN" },
                            label = { Text("Plin") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ColorGalyGold,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }

                    OutlinedTextField(
                        value = alias,
                        onValueChange = { alias = it },
                        label = { Text("Nombre o Alias (ej. Yape Principal)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorGalyGold,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = ColorGalyGold,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = cardHolder,
                        onValueChange = { cardHolder = it },
                        label = { Text("Número de celular o titular") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorGalyGold,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = ColorGalyGold,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (alias.isBlank()) {
                            Toast.makeText(context, "Ingresa un alias o nombre", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val last4 = methodType

                        val newItem = PaymentMethodItem(
                            id = System.currentTimeMillis().toString(),
                            alias = alias,
                            type = methodType,
                            lastFour = last4,
                            cardHolder = if (cardHolder.isBlank()) alias else cardHolder,
                            expiry = "",
                            isDefault = methods.isEmpty()
                        )

                        updateAndSave(methods + newItem)
                        Toast.makeText(context, "Método de pago guardado", Toast.LENGTH_SHORT).show()

                        // Reset
                        alias = ""
                        cardHolder = ""
                        methodType = "YAPE"
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorGalyGold, contentColor = Color.Black)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}
