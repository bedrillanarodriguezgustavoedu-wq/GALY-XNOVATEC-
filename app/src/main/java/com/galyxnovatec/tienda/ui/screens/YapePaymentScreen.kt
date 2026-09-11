package com.galyxnovatec.tienda.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.QrCode2
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
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YapePaymentScreen(
    monto: Double,
    alVolver: () -> Unit,
    alConfirmarPago: () -> Unit
) {
    val context = LocalContext.current
    val numeroYape = "978104136"
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
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
                text = "Escanea el código QR para pagar",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // RECUADRO DEL QR PREMIUM
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White)
                    .border(BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f)), RoundedCornerShape(32.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                // Si tienes la imagen en drawable, se verá aquí
                val qrRes = painterResource(id = R.drawable.icono) // Usamos el nuevo icono como placeholder elegante
                Image(
                    painter = qrRes,
                    contentDescription = "QR GALY",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit
                )
                
                // Texto de ayuda sobre el QR
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("QR OFICIAL GALY", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
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
                    // 1. Copiar número al portapapeles
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Número Yape", numeroYape)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Número $numeroYape copiado", Toast.LENGTH_SHORT).show()

                    // 2. Intentar abrir la app de Yape
                    val intent = context.packageManager.getLaunchIntentForPackage("com.bcp.innovabcp.yape")
                    if (intent != null) {
                        context.startActivity(intent)
                    } else {
                        // Si no está instalada, abrir Play Store
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.bcp.innovabcp.yape")))
                        } catch (e: Exception) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.bcp.innovabcp.yape")))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF742D8A)) // Color morado Yape
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
                onClick = alConfirmarPago,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                enabled = imageUri != null
            ) {
                Text("CONFIRMAR MI PEDIDO", fontWeight = FontWeight.ExtraBold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
