package com.galyxnovatec.tienda.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.galyxnovatec.tienda.BuildConfig
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.manager.AddressManager
import com.galyxnovatec.tienda.data.manager.AuthManager
import com.galyxnovatec.tienda.data.manager.CartManager
import com.galyxnovatec.tienda.data.manager.OrderManager
import com.galyxnovatec.tienda.data.model.User
import com.galyxnovatec.tienda.data.network.ManualPaymentMethod
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.data.PaymentAppLauncher
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    alVolver: () -> Unit,
    alIrADirecciones: () -> Unit,
    alIrAPagoWeb: (String) -> Unit,
    alFinalizarCompra: () -> Unit
) {
    val context = LocalContext.current
    val user = AuthManager.currentUser
    var metodoEntrega by remember { mutableStateOf("DOMICILIO") } // DOMICILIO o RECOJO
    var metodoSeleccionado by remember { mutableStateOf("") }
    var paymentMethods by remember { mutableStateOf<List<ManualPaymentMethod>>(emptyList()) }
    var paymentMethodsLoading by remember { mutableStateOf(true) }
    var paymentMethodsError by remember { mutableStateOf<String?>(null) }
    var receiptUri by remember { mutableStateOf<Uri?>(null) }
    var pendingOrderId by remember { mutableStateOf<Int?>(null) }
    var isOpeningYape by remember { mutableStateOf(false) }
    var usarPuntos by remember { mutableStateOf(false) }
    var dniRuc by remember { mutableStateOf("") }
    var numOperacion by remember { mutableStateOf("") }
    var aceptoTerminos by remember { mutableStateOf(false) }
    var codigoCupon by remember { mutableStateOf("") }
    
    // Datos obligatorios del cliente
    var nombreCliente by remember { mutableStateOf(user?.nombre ?: "") }
    var emailCliente by remember { mutableStateOf(user?.email ?: "") }
    var telefonoCliente by remember { mutableStateOf(user?.telefono ?: "") }
    
    // Selección de Moneda (PEN: Soles, USD: Dólares)
    var moneda by remember { mutableStateOf("PEN") }
    val tipoCambioUsd = 3.75

    fun formatMonto(montoSoles: Double): String {
        return if (moneda == "PEN") {
            "S/ ${montoSoles.toInt()}"
        } else {
            val usd = montoSoles / tipoCambioUsd
            "$ ${String.format(Locale.US, "%.2f", usd)} USD"
        }
    }

    val scope = rememberCoroutineScope()
    val receiptPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        receiptUri = uri
    }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.webInstance.getPaymentMethods()
            if (response.success) {
                paymentMethods = response.methods.filter { it.method in setOf("YAPE", "PLIN", "BCP") }
                if (paymentMethods.none { it.method == metodoSeleccionado }) {
                    metodoSeleccionado = paymentMethods.firstOrNull()?.method.orEmpty()
                }
            } else {
                paymentMethodsError = response.message ?: "No se pudieron cargar los métodos de pago."
            }
        } catch (exception: Exception) {
            paymentMethodsError = "No se pudieron cargar los métodos de pago de la tienda."
        } finally {
            paymentMethodsLoading = false
        }

        ProductRepository.sincronizarInformacionContacto()
        user?.id?.let { 
            AddressManager.fetchAddresses(it)
            CartManager.sincronizarConServidor(context)
            
            if (AddressManager.addresses.isNotEmpty() && CartManager.direccionSeleccionada == null) {
                CartManager.direccionSeleccionada = AddressManager.addresses.first()
            }
        }
    }

    val manualPayment = metodoSeleccionado in setOf("YAPE", "PLIN", "BCP")
    val selectedPaymentConfigured = !manualPayment || paymentMethods.any { it.method == metodoSeleccionado }

    fun checkoutValidationError(): String? = when {
        nombreCliente.isBlank() -> "Por favor, ingresa tu Nombre y Apellidos."
        emailCliente.isBlank() || !emailCliente.contains("@") -> "Por favor, ingresa tu correo Gmail válido."
        telefonoCliente.isBlank() -> "Por favor, ingresa tu número de teléfono."
        dniRuc.isBlank() -> "Por favor, ingresa tu DNI o RUC."
        metodoEntrega == "DOMICILIO" && CartManager.direccionSeleccionada == null ->
            "Por favor, ingresa tu dirección de entrega."
        !aceptoTerminos -> "Acepta los términos y la política de privacidad para continuar."
        CartManager.items.none { it.isSelected } -> "Selecciona al menos un producto para continuar."
        else -> null
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
            // SELECTOR DE MONEDA (SOLES S/ vs DÓLARES $)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Moneda de Pago:", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF161616),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (moneda == "PEN") ColorGalyRed else Color.Transparent,
                            modifier = Modifier.clickable { moneda = "PEN" }
                        ) {
                            Text(
                                "🇵🇪 SOLES (S/)",
                                color = if (moneda == "PEN") Color.White else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (moneda == "USD") ColorGalyRed else Color.Transparent,
                            modifier = Modifier.clickable { moneda = "USD" }
                        ) {
                            Text(
                                "🇺🇸 DÓLARES ($)",
                                color = if (moneda == "USD") Color.White else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // BANNERS SUPERIORES
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF001A00),
                    shape = RoundedCornerShape(12.dp),
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
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Datos protegidos", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // SECCIÓN MÉTODO DE ENTREGA
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color(0xFF111111),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("¿Cómo quieres recibir tu pedido?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DeliveryOptionCard(
                            title = "Domicilio",
                            icon = Icons.Default.Home,
                            isSelected = metodoEntrega == "DOMICILIO",
                            modifier = Modifier.weight(1f)
                        ) { metodoEntrega = "DOMICILIO" }
                        
                        DeliveryOptionCard(
                            title = "Tienda",
                            icon = Icons.Default.Storefront,
                            isSelected = metodoEntrega == "RECOJO",
                            modifier = Modifier.weight(1f)
                        ) { metodoEntrega = "RECOJO" }
                    }
                }
            }

            // SECCIÓN DIRECCIÓN (SÓLO SI ES DOMICILIO)
            if (metodoEntrega == "DOMICILIO") {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
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
                                val currentDir = CartManager.direccionSeleccionada
                                Text(
                                    text = if (currentDir != null) "${currentDir.direccion}, ${currentDir.ciudad}" else "Añadir dirección",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
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
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color(0xFF111111),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, ColorGalyRed.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, null, tint = ColorGalyRed, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Recojo en Sede Central GALY", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Av. Principal 123, Lima", color = Color.Gray, fontSize = 12.sp)
                            Text("Horario: 9am - 8pm", color = ColorGalyRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // SECCIÓN DATOS PERSONALES, GMAIL Y FACTURACIÓN
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color(0xFF111111),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = ColorGalyRed, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Datos personales y Gmail", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = nombreCliente,
                        onValueChange = { nombreCliente = it },
                        label = { Text("Nombre y Apellidos completos *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = ColorGalyRed, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        singleLine = true
                    )
                    
                    Spacer(Modifier.height(10.dp))
                    
                    OutlinedTextField(
                        value = emailCliente,
                        onValueChange = { emailCliente = it },
                        label = { Text("Correo Gmail (Boleta y Seguimiento) *") },
                        placeholder = { Text("ejemplo@gmail.com") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = ColorGalyRed, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = telefonoCliente,
                        onValueChange = { if(it.length <= 15) telefonoCliente = it },
                        label = { Text("Teléfono de contacto / WhatsApp *") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = ColorGalyRed, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = dniRuc,
                        onValueChange = { if (it.length <= 11) dniRuc = it },
                        label = { Text("DNI o RUC para comprobante *") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = ColorGalyRed, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        singleLine = true
                    )
                }
            }

            // MÉTODOS DE PAGO DIVERSIFICADOS CON TARJETAS ÚNICAS
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color(0xFF111111),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Selecciona tu aplicación de pago", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    PaymentRow("Yape", "Abrir Yape para transferir", listOf(R.drawable.yape), metodoSeleccionado == "YAPE", true) { metodoSeleccionado = "YAPE" }
                    PaymentRow("Plin", "Abrir Plin / Interbank para usar Plin", listOf(R.drawable.pling), metodoSeleccionado == "PLIN", true) { metodoSeleccionado = "PLIN" }
                    PaymentRow("BCP / Banca Móvil", "Transferencia a cuenta BCP", listOf(R.drawable.bsp), metodoSeleccionado == "BCP", true) { metodoSeleccionado = "BCP" }
                    PaymentRow("BBVA / Interbank", "Otras bancas móviles", listOf(R.drawable.interbnack), metodoSeleccionado == "OTHER_BANK", true) { metodoSeleccionado = "OTHER_BANK" }
                    PaymentRow("Banco de la Nación", "Cuenta Corriente BN", listOf(R.drawable.bancodelanancion), metodoSeleccionado == "BN", true) { metodoSeleccionado = "BN" }
                    PaymentRow("Pago contra entrega", "Efectivo al recibir pedido o tienda", emptyList(), metodoSeleccionado == "CASH", true) { metodoSeleccionado = "CASH" }

                    // TARJETA PERSONALIZADA SEGÚN LA APLICACIÓN SELECCIONADA
                    when (metodoSeleccionado) {
                        "YAPE" -> {
                            val yapeMethod = paymentMethods.firstOrNull { it.method == "YAPE" }
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                color = Color(0xFF1F0D24), // Morado característico Yape
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.dp, Color(0xFFA1238E))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.QrCode2, null, tint = Color(0xFFFF007A), modifier = Modifier.size(22.dp))
                                        Spacer(Modifier.width(10.dp))
                                        Text("Escanea el QR de Yape", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                    }
                                    yapeMethod?.let { method ->
                                        WalletQrPanel(
                                            ayuda = "Abre Yape en otro dispositivo o guarda el QR para escanearlo. Comprueba que el titular coincida antes de pagar.",
                                            numero = numeroParaMostrar(method.number),
                                            titular = method.holder,
                                            total = formatMonto(CartManager.obtenerTotal(if (usarPuntos) (user?.puntos ?: 0) else 0)),
                                            qrUrl = method.qr?.let { "${RetrofitClient.getEffectiveBaseUrl()}$it" },
                                            acento = Color(0xFFFF007A)
                                        )
                                    } ?: Text(
                                        paymentMethodsError ?: "La tienda aún no configuró el número y titular de Yape para la app.",
                                        color = Color(0xFFFFB4AB),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 6.dp)
                                    )

                                    Spacer(Modifier.height(14.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                val numero = yapeMethod?.number ?: return@Button
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Yape", numero))
                                                Toast.makeText(context, "✅ Número $numero copiado.", Toast.LENGTH_SHORT).show()
                                            },
                                            enabled = yapeMethod != null,
                                            modifier = Modifier.weight(1f).height(44.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF32133B)),
                                            border = BorderStroke(1.dp, Color(0xFFA1238E)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp), tint = Color.White)
                                            Spacer(Modifier.width(6.dp))
                                            Text("COPIAR NÚMERO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        Button(
                                            onClick = {
                                                val numero = yapeMethod?.number ?: return@Button
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Yape", numero))
                                                PaymentAppLauncher.open(context, "com.bcp.innovabcp.yape", "Yape")
                                            },
                                            enabled = yapeMethod != null,
                                            modifier = Modifier.weight(1f).height(44.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007A)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(if (yapeMethod == null) "YAPE NO CONFIGURADO" else "ABRIR YAPE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = numOperacion,
                                        onValueChange = { if (it.length <= 80) numOperacion = it },
                                        label = { Text("N° de Operación Yape") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFFFF007A), unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                        ),
                                        singleLine = true
                                    )

                                    Spacer(Modifier.height(10.dp))
                                    Button(
                                        onClick = { receiptPicker.launch("image/*") },
                                        modifier = Modifier.fillMaxWidth().height(42.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF32133B)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.UploadFile, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(if (receiptUri == null) "ADJUNTAR COMPROBANTE YAPE" else "CAMBIAR COMPROBANTE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    receiptUri?.let { uri ->
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "Comprobante de Yape",
                                            modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).padding(top = 10.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }

                                    Spacer(Modifier.height(10.dp))
                                    NotaVerificacionManual()
                                }
                            }
                        }

                        "PLIN" -> {
                            val plinMethod = paymentMethods.firstOrNull { it.method == "PLIN" }
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                color = Color(0xFF00222B), // Azul Cyan Plin
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.dp, Color(0xFF00A3E0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.QrCode2, null, tint = Color(0xFF00C1D5), modifier = Modifier.size(22.dp))
                                        Spacer(Modifier.width(10.dp))
                                        Text("Escanea el QR de Plin", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                    }
                                    plinMethod?.let { method ->
                                        WalletQrPanel(
                                            ayuda = "Abre Plin en otro dispositivo o guarda el QR para escanearlo. Comprueba que el titular coincida antes de pagar.",
                                            numero = numeroParaMostrar(method.number),
                                            titular = method.holder,
                                            total = formatMonto(CartManager.obtenerTotal(if (usarPuntos) (user?.puntos ?: 0) else 0)),
                                            qrUrl = method.qr?.let { "${RetrofitClient.getEffectiveBaseUrl()}$it" },
                                            acento = Color(0xFF00C1D5)
                                        )
                                    }

                                    Spacer(Modifier.height(14.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                val numero = plinMethod?.number ?: return@Button
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Plin", numero))
                                                Toast.makeText(context, "✅ Número $numero copiado.", Toast.LENGTH_SHORT).show()
                                            },
                                            enabled = plinMethod != null,
                                            modifier = Modifier.weight(1f).height(44.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003846)),
                                            border = BorderStroke(1.dp, Color(0xFF00C1D5)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp), tint = Color.White)
                                            Spacer(Modifier.width(6.dp))
                                            Text("COPIAR NÚMERO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        Button(
                                            onClick = {
                                                val numero = plinMethod?.number ?: return@Button
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Plin", numero))
                                                PaymentAppLauncher.open(context, "com.interbank.mobilebanking", "Interbank / Plin")
                                            },
                                            enabled = plinMethod != null,
                                            modifier = Modifier.weight(1f).height(44.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C1D5)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(if (plinMethod == null) "PLIN NO CONFIGURADO" else "ABRIR PLIN", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                                        }
                                    }
                                    if (plinMethod == null) {
                                        Text(
                                            paymentMethodsError ?: "La tienda aún no configuró el número receptor y titular de Plin para la app.",
                                            color = Color(0xFFFFB4AB),
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = numOperacion,
                                        onValueChange = { if (it.length <= 80) numOperacion = it },
                                        label = { Text("N° de Operación Plin") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFF00C1D5), unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                        ),
                                        singleLine = true
                                    )

                                    Spacer(Modifier.height(10.dp))
                                    Button(
                                        onClick = { receiptPicker.launch("image/*") },
                                        modifier = Modifier.fillMaxWidth().height(42.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003846)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.UploadFile, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(if (receiptUri == null) "ADJUNTAR COMPROBANTE PLIN" else "CAMBIAR COMPROBANTE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    receiptUri?.let { uri ->
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "Comprobante de transferencia Plin",
                                            modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).padding(top = 10.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }

                                    Spacer(Modifier.height(10.dp))
                                    NotaVerificacionManual()
                                }
                            }
                        }

                        "BCP" -> {
                            val bcpMethod = paymentMethods.firstOrNull { it.method == "BCP" }
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                color = Color(0xFF081329), // Azul Marino BCP
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.dp, Color(0xFF0052CC))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccountBalance, null, tint = Color(0xFF4C9AFF), modifier = Modifier.size(22.dp))
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            bcpMethod?.let { "CCI BCP: ${it.number}" } ?: "BCP no está configurado",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp
                                        )
                                    }
                                    bcpMethod?.let { method ->
                                        if (!method.account.isNullOrBlank()) {
                                            Text("Cuenta: ${method.account}", color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                                        }
                                        Text("Titular: ${method.holder}", color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                                    } ?: Text(
                                        paymentMethodsError ?: "La tienda debe configurar el CCI y el titular BCP desde el administrador.",
                                        color = Color(0xFFFFB4AB),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 6.dp)
                                    )

                                    if (bcpMethod != null) {
                                        Spacer(Modifier.height(12.dp))
                                        Text("Total exacto", color = Color.Gray, fontSize = 11.sp)
                                        Text(
                                            formatMonto(CartManager.obtenerTotal(if (usarPuntos) (user?.puntos ?: 0) else 0)),
                                            color = Color(0xFF4C9AFF),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }

                                    Spacer(Modifier.height(14.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val cci = bcpMethod?.number ?: return@Button
                                                clipboard.setPrimaryClip(ClipData.newPlainText("CCI BCP", cci))
                                                Toast.makeText(context, "CCI BCP copiado. Verifica el titular antes de pagar.", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f).height(44.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC)),
                                            shape = RoundedCornerShape(10.dp),
                                            enabled = bcpMethod != null
                                        ) {
                                            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp), tint = Color.White)
                                            Spacer(Modifier.width(6.dp))
                                            Text("COPIAR CCI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        Button(
                                            onClick = {
                                                bcpMethod?.let { method ->
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(ClipData.newPlainText("CCI BCP", method.number))
                                                    PaymentAppLauncher.open(context, "com.bcp.bank.mobile", "BCP")
                                                }
                                            },
                                            modifier = Modifier.weight(1f).height(44.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC)),
                                            shape = RoundedCornerShape(10.dp),
                                            enabled = bcpMethod != null
                                        ) {
                                            Text(if (bcpMethod == null) "BCP NO CONFIGURADO" else "ABRIR BCP", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = numOperacion,
                                        onValueChange = { if (it.length <= 80) numOperacion = it },
                                        label = { Text("N° de Transferencia BCP") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFF4C9AFF), unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                        ),
                                        singleLine = true
                                    )

                                    Spacer(Modifier.height(10.dp))
                                    Button(
                                        onClick = { receiptPicker.launch("image/*") },
                                        modifier = Modifier.fillMaxWidth().height(42.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102A50)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.UploadFile, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(if (receiptUri == null) "ADJUNTAR COMPROBANTE BCP" else "CAMBIAR COMPROBANTE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    receiptUri?.let { uri ->
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "Comprobante de transferencia BCP",
                                            modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).padding(top = 10.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }

                                    Spacer(Modifier.height(10.dp))
                                    NotaVerificacionManual()
                                }
                            }
                        }

                        "CASH" -> {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                color = Color(0xFF1E1903), // Dorado Efectivo
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.dp, Color(0xFFFFB400).copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Payments, null, tint = Color(0xFFFFB400), modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Pago Contra Entrega en Efectivo", color = Color(0xFFFFB400), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text("Abona en efectivo al recibir tu pedido en domicilio o al recoger en la Sede Central GALY.", color = Color.LightGray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // CUPÓN Y PUNTOS
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
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
                        if (pendingOrderId != null && manualPayment) {
                            Text(
                                text = if (receiptUri == null) {
                                    "Pedido #$pendingOrderId creado. Completa la transferencia y adjunta el comprobante para enviarlo."
                                } else {
                                    "Pedido #$pendingOrderId creado. Envía el comprobante para terminar."
                                },
                                color = Color(0xFFFFD180),
                                fontSize = 13.sp,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                        }

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
                            colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (CartManager.validandoCupon) "…" else "Aplicar")
                        }
                    }
                }
            }

            // RESUMEN CON CONVERSIÓN DE MONEDA
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color(0xFF111111),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Resumen del pedido", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(16.dp))
                    CostRow("Subtotal", formatMonto(CartManager.obtenerSubtotal()))
                    if (CartManager.descuentoPorCupon > 0) {
                        CostRow("Descuento Cupón", "- ${formatMonto(CartManager.descuentoPorCupon)}", color = ColorGalyRed)
                    }
                    if (usarPuntos) {
                        val descuentoPuntos = (user?.puntos ?: 0).toDouble()
                        CostRow("Puntos Galy", "- ${formatMonto(descuentoPuntos)}", color = Color.Yellow)
                    }
                    CostRow("Entrega", if (metodoEntrega == "DOMICILIO") "Domicilio" else "Tienda")
                    CostRow("Envío", "GRATIS", color = Color(0xFF00FF41))
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total a pagar", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                        val totalFinalSoles = CartManager.obtenerTotal(if(usarPuntos) (user?.puntos ?: 0) else 0)
                        Text(formatMonto(totalFinalSoles), fontWeight = FontWeight.Black, fontSize = 22.sp, color = ColorGalyRed)
                    }
                }
            }

            // TÉRMINOS
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = aceptoTerminos, onCheckedChange = { aceptoTerminos = it }, colors = CheckboxDefaults.colors(checkedColor = ColorGalyRed))
                Text("Acepto los términos y política de privacidad.", color = Color.Gray, fontSize = 12.sp)
            }

            // BOTÓN DE PAGO FINAL CON VALIDACIÓN DE DATOS Y DIRECCIÓN
            Button(
                onClick = { 
                    val validationError = checkoutValidationError()
                    if (validationError != null) {
                        Toast.makeText(context, validationError, Toast.LENGTH_LONG).show()
                        if (metodoEntrega == "DOMICILIO" && CartManager.direccionSeleccionada == null) {
                            alIrADirecciones()
                        }
                        return@Button
                    }

                    if (!selectedPaymentConfigured) {
                        Toast.makeText(context, "Este método de pago no está configurado por la tienda.", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    if (OrderManager.isLoading || isOpeningYape) {
                        return@Button
                    }

                    if (metodoSeleccionado == "YAPE" && pendingOrderId != null) {
                        CartManager.eliminarItemsSeleccionados(context)
                        Toast.makeText(context, "El pago quedó registrado para verificación de la tienda.", Toast.LENGTH_LONG).show()
                        alFinalizarCompra()
                        return@Button
                    }

                    val activeUser = AuthManager.currentUser ?: User(
                        id = 101,
                        nombre = nombreCliente,
                        email = emailCliente,
                        telefono = telefonoCliente,
                        direccion = CartManager.direccionSeleccionada?.direccion
                    )
                    AuthManager.currentUser = activeUser

                    scope.launch {
                        val manualMethods = setOf("YAPE", "PLIN", "BCP")
                        if (pendingOrderId != null) {
                            val receipt = receiptUri
                            if (receipt == null) {
                                CartManager.eliminarItemsSeleccionados(context)
                                Toast.makeText(context, "El pedido quedó registrado en pagos. La tienda verificará el abono.", Toast.LENGTH_LONG).show()
                                alFinalizarCompra()
                                return@launch
                            }
                            val receiptError = OrderManager.sendPaymentReceipt(
                                context = context,
                                orderId = pendingOrderId!!,
                                method = metodoSeleccionado,
                                imageUri = receipt,
                                operationNumber = numOperacion
                            )
                            if (receiptError != null) {
                                Toast.makeText(context, receiptError, Toast.LENGTH_LONG).show()
                                return@launch
                            }
                            CartManager.eliminarItemsSeleccionados(context)
                            alFinalizarCompra()
                            return@launch
                        }

                        val itemsAComprar = CartManager.items.filter { it.isSelected }
                        val totalFinalSoles = CartManager.obtenerTotal(if (usarPuntos) (activeUser.puntos) else 0)

                        if (metodoSeleccionado == "CARD") {
                            val result = OrderManager.placeOrder(
                                userId = activeUser.id,
                                items = itemsAComprar,
                                total = totalFinalSoles,
                                metodoPago = "CARD",
                                metodoEntrega = metodoEntrega,
                                direccion = if (metodoEntrega == "DOMICILIO") CartManager.direccionSeleccionada else null,
                                dni = dniRuc
                            )

                            if (result is Int) {
                                val orderId = result
                                try {
                                    val mpData = mapOf(
                                        "items" to itemsAComprar.map { mapOf("nombre" to it.producto.nombre, "cantidad" to it.cantidad, "precio" to it.producto.precioOferta) },
                                        "total" to totalFinalSoles,
                                        "pedido_id" to orderId
                                    )
                                    val mpResponse = RetrofitClient.instance.getMpPreference(mpData)
                                    val success = mpResponse["success"] == true || mpResponse["success"].toString().lowercase() == "true"
                                    if (success) {
                                        val initPoint = mpResponse["init_point"] as? String ?: mpResponse["url"] as? String
                                        if (!initPoint.isNullOrBlank()) {
                                            CartManager.eliminarItemsSeleccionados(context)
                                            alIrAPagoWeb(initPoint)
                                            return@launch
                                        }
                                    }
                                    Toast.makeText(context, "El pago con tarjeta requiere configurar tu Access Token de Mercado Pago. Usa Yape, Plin o Transferencia temporalmente.", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error de conexión con la pasarela de pago. Usa Yape, Plin o Transferencia.", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    result?.toString() ?: "No se pudo crear el pedido. Inténtalo nuevamente.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            return@launch
                        }

                        val result = OrderManager.placeOrder(
                            userId = activeUser.id,
                            items = itemsAComprar,
                            total = totalFinalSoles,
                            metodoPago = metodoSeleccionado,
                            metodoEntrega = metodoEntrega,
                            direccion = if (metodoEntrega == "DOMICILIO") CartManager.direccionSeleccionada else null,
                            dni = dniRuc
                        )

                        if (result is Int) {
                            pendingOrderId = result
                            if (metodoSeleccionado in manualMethods) {
                                if (metodoSeleccionado == "YAPE" && receiptUri == null) {
                                    Toast.makeText(
                                        context,
                                        "Pedido #$result registrado en pagos. Completa el pago en Yape.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    PaymentAppLauncher.open(context, "com.bcp.innovabcp.yape", "Yape")
                                    return@launch
                                }

                                if (receiptUri == null) {
                                    Toast.makeText(context, "Pedido #$result registrado en pagos. La tienda verificará el abono.", Toast.LENGTH_LONG).show()
                                } else {
                                    val receiptError = OrderManager.sendPaymentReceipt(
                                        context = context,
                                        orderId = result,
                                        method = metodoSeleccionado,
                                        imageUri = receiptUri!!,
                                        operationNumber = numOperacion
                                    )
                                    if (receiptError != null) {
                                        Toast.makeText(context, receiptError, Toast.LENGTH_LONG).show()
                                        return@launch
                                    }
                                }
                            }

                            if (metodoSeleccionado !in manualMethods) {
                                Toast.makeText(context, "Pedido #$result creado.", Toast.LENGTH_LONG).show()
                            }
                            CartManager.eliminarItemsSeleccionados(context)
                            alFinalizarCompra()
                        } else {
                            Toast.makeText(
                                context,
                                result?.toString() ?: "No se pudo crear el pedido. Inténtalo nuevamente.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed),
                enabled = aceptoTerminos
                    && CartManager.items.isNotEmpty()
                    && !OrderManager.isLoading
                    && !OrderManager.isSubmittingReceipt
                    && !isOpeningYape
                    && metodoSeleccionado.isNotBlank()
                    && selectedPaymentConfigured
                    && (pendingOrderId == null || metodoSeleccionado == "YAPE" || receiptUri != null)
            ) {
                if (OrderManager.isLoading || OrderManager.isSubmittingReceipt) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    val totalFinalSoles = CartManager.obtenerTotal(if(usarPuntos) (user?.puntos ?: 0) else 0)
                    Text(
                        when {
                            pendingOrderId != null && metodoSeleccionado == "YAPE" -> "YA PAGUÉ - FINALIZAR"
                            pendingOrderId != null -> "ENVIAR COMPROBANTE"
                            else -> "PAGAR ${formatMonto(totalFinalSoles)}"
                        },
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PaymentRow(
    title: String, 
    desc: String, 
    icons: List<Int>, 
    isSelected: Boolean, 
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) ColorGalyRed.copy(alpha = 0.12f) else Color(0xFF161616),
        border = BorderStroke(1.dp, if (isSelected) ColorGalyRed else Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected, 
                onClick = onClick,
                enabled = enabled,
                colors = RadioButtonDefaults.colors(selectedColor = ColorGalyRed, unselectedColor = Color.Gray)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(desc, color = Color.Gray, fontSize = 12.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                icons.forEach { iconRes ->
                    Image(
                        painter = painterResource(id = iconRes), 
                        contentDescription = null, 
                        modifier = Modifier.height(28.dp).widthIn(max = 40.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
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

@Composable
fun WalletQrPanel(
    ayuda: String,
    numero: String,
    titular: String,
    total: String,
    qrUrl: String?,
    acento: Color
) {
    Text(ayuda, color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        qrUrl?.let { url ->
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, acento)
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = "Código QR del destinatario",
                    modifier = Modifier.size(128.dp).padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(Modifier.width(14.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("Total exacto", color = Color.Gray, fontSize = 11.sp)
            Text(total, color = acento, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(10.dp))
            Text("Titular", color = Color.Gray, fontSize = 11.sp)
            Text(titular, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("Número", color = Color.Gray, fontSize = 11.sp)
            Text(numero, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/** Un celular peruano se muestra con +51 para que el cliente copie el mismo formato que ve en la web. */
fun numeroParaMostrar(numero: String): String =
    if (numero.length == 9 && numero.all { it.isDigit() }) "+51 $numero" else numero

/** El aviso de la web: un pago manual no confirma el pedido hasta que la tienda revisa el comprobante. */
@Composable
fun NotaVerificacionManual() {
    Text(
        "El pago no se confirma automáticamente. Envía el comprobante; la tienda verificará el abono antes de marcar el pedido como pagado.",
        color = Color(0xFFFFB4AB),
        fontSize = 11.sp
    )
}

@Composable
fun DeliveryOptionCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = if (isSelected) ColorGalyRed.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isSelected) ColorGalyRed else Color.Transparent)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) ColorGalyRed else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color.Gray
            )
        }
    }
}
