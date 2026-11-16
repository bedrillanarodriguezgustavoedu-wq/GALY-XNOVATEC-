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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.PaymentAppLauncher
import com.galyxnovatec.tienda.data.manager.AuthManager
import com.galyxnovatec.tienda.data.manager.OrderManager
import com.galyxnovatec.tienda.data.network.ManualPaymentMethod
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YapePaymentScreen(
    monto: Double,
    pedidoId: Int,
    alVolver: () -> Unit,
    alConfirmarPago: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var yapeMethod by remember { mutableStateOf<ManualPaymentMethod?>(null) }
    var paymentMethodsLoading by remember { mutableStateOf(true) }
    var paymentMethodsError by remember { mutableStateOf<String?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.webInstance.getPaymentMethods()
            if (response.success) {
                yapeMethod = response.methods.firstOrNull { it.method == "YAPE" }
            } else {
                paymentMethodsError = response.message ?: "No se pudo cargar la configuración de Yape."
            }
        } catch (_: Exception) {
            paymentMethodsError = "No se pudo cargar la configuración oficial de Yape."
        } finally {
            paymentMethodsLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.yape),
                            contentDescription = null,
                            modifier = Modifier.height(24.dp).padding(end = 8.dp)
                        )
                        Text("Pago con Yape", fontWeight = FontWeight.Black, color = Color.White)
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Paga desde la aplicación de Yape",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (yapeMethod != null) {
                Text("Número Yape: ${yapeMethod!!.number}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Titular: ${yapeMethod!!.holder}", color = Color.LightGray, fontSize = 13.sp)
                Text(
                    "Al abrir Yape, realiza la transferencia al número indicado y confirma que el titular sea correcto.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    if (paymentMethodsLoading) "Cargando los datos de Yape…" else paymentMethodsError
                        ?: "Yape no está configurado en la tienda.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Monto a yapear:",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "S/ ${monto.toInt()}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = ColorGalyRed
            )

            Spacer(modifier = Modifier.height(24.dp))

            // BOTÓN PARA ABRIR YAPE Y COPIAR NÚMERO
            Button(
                onClick = {
                    val number = yapeMethod?.number ?: return@Button
                    // 1. Copiar número al portapapeles
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Número Yape", number)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Número copiado", Toast.LENGTH_SHORT).show()

                    PaymentAppLauncher.open(context, "com.bcp.innovabcp.yape", "Yape")
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF742D8A)),
                enabled = yapeMethod != null
            ) {
                Image(
                    painter = painterResource(id = R.drawable.yape),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).padding(end = 8.dp)
                )
                Text("ABRIR APP DE YAPE", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // SECCIÓN SUBIR BOLETA
            Text(
                text = "Adjunta la boleta de tu compra",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = Color(0xFF1A1A1A),
                border = BorderStroke(2.dp, if (imageUri != null) Color(0xFF00FF41) else Color.White.copy(alpha = 0.1f))
            ) {
                if (imageUri == null) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        IconButton(onClick = { launcher.launch("image/*") }) {
                            Icon(Icons.Default.CloudUpload, null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                        }
                        Text("Subir boleta aquí", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Boleta",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            if (imageUri != null) {
                TextButton(onClick = { imageUri = null }) {
                    Text("Cambiar imagen", color = ColorGalyRed)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    val receipt = imageUri ?: return@Button
                    scope.launch {
                        val error = OrderManager.sendPaymentReceipt(
                            context = context,
                            orderId = pedidoId,
                            method = "YAPE",
                            imageUri = receipt
                        )
                        if (error == null) {
                            alConfirmarPago()
                        } else {
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                enabled = imageUri != null
                    && yapeMethod != null
                    && !AuthManager.googleIdToken.isNullOrBlank()
                    && !OrderManager.isSubmittingReceipt
            ) {
                if (OrderManager.isSubmittingReceipt) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = Color.Black
                    )
                } else {
                    Text("ENVIAR COMPROBANTE", fontWeight = FontWeight.ExtraBold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
