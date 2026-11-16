package com.galyxnovatec.tienda.ui.screens

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.data.model.InformacionNosotros
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

private val aboutFields = listOf(
    "titulo_principal" to "Título principal *",
    "subtitulo" to "Subtítulo *",
    "descripcion_principal" to "Descripción principal *",
    "historia" to "Historia de la empresa *",
    "mision" to "Misión *",
    "vision" to "Visión *",
    "valores" to "Valores (separados por comas) *",
    "video_url" to "Enlace de video (opcional)"
)

private fun InformacionNosotros.toEditableValues(): List<String> = listOf(
    tituloPrincipal,
    subtitulo,
    descripcionPrincipal,
    historia,
    mision,
    vision,
    valores,
    videoUrl.orEmpty()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNosotrosScreen(alVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fields = remember { mutableStateListOf<String>() }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var active by remember { mutableStateOf(true) }
    var experience by remember { mutableStateOf("0") }
    var clients by remember { mutableStateOf("0") }
    var products by remember { mutableStateOf("0") }

    fun refreshAbout() {
        scope.launch {
            isLoading = true
            val success = ProductRepository.sincronizarInformacionNosotros()
            isLoading = false
            if (!success) {
                Toast.makeText(context, "No se pudo cargar la información compartida.", Toast.LENGTH_LONG).show()
            }
        }
    }

    val about = ProductRepository.informacionNosotros
    LaunchedEffect(Unit) { refreshAbout() }
    LaunchedEffect(about) {
        if (about != null) {
            fields.clear()
            fields.addAll(about.toEditableValues())
            active = about.activo
            experience = about.experienciaAnos.toString()
            clients = about.clientesCount.toString()
            products = about.productosCount.toString()
        }
    }

    val requiredFieldsPresent = (0..6).all { fields.getOrNull(it)?.isNotBlank() == true }
    val experienceValue = experience.toIntOrNull()
    val clientsValue = clients.toIntOrNull()
    val productsValue = products.toIntOrNull()
    val countsValid = listOf(experienceValue, clientsValue, productsValue).all { it != null && it >= 0 }
    val videoUrl = fields.getOrNull(7).orEmpty()
    val videoValid = videoUrl.isBlank() || Patterns.WEB_URL.matcher(videoUrl).matches()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Nosotros compartido", color = Color.White, fontWeight = FontWeight.Black)
                        Text("Contenido de la web y GALY", color = Color.LightGray, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { refreshAbout() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        if (isLoading && about == null) {
            Box(
                Modifier.fillMaxSize().padding(padding).background(Color.Black),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator(color = ColorGalyRed)
            }
        } else if (about == null) {
            Column(
                Modifier.fillMaxSize().padding(padding).background(Color.Black).padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("No existe información de Nosotros. Configúrala primero desde el panel web.", color = Color.White)
                TextButton(onClick = { refreshAbout() }) { Text("Reintentar") }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.Black)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Los cambios se reflejan en la página web y en la sección Sobre GALY. La imagen principal se conserva y se administra desde la web.",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )

                aboutFields.forEachIndexed { index, (key, label) ->
                    OutlinedTextField(
                        value = fields.getOrElse(index) { "" },
                        onValueChange = { value -> if (index < fields.size) fields[index] = value },
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = if (key in listOf("descripcion_principal", "historia", "mision", "vision", "valores")) 3 else 1,
                        singleLine = key !in listOf("descripcion_principal", "historia", "mision", "vision", "valores"),
                        isError = key == "video_url" && !videoValid,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ColorGalyRed,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                            focusedLabelColor = ColorGalyRed,
                            unfocusedLabelColor = Color.LightGray
                        )
                    )
                }

                OutlinedTextField(
                    value = experience,
                    onValueChange = { experience = it },
                    label = { Text("Años de experiencia *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = experienceValue == null || experienceValue < 0
                )
                OutlinedTextField(
                    value = clients,
                    onValueChange = { clients = it },
                    label = { Text("Cantidad de clientes *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = clientsValue == null || clientsValue < 0
                )
                OutlinedTextField(
                    value = products,
                    onValueChange = { products = it },
                    label = { Text("Cantidad de productos *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = productsValue == null || productsValue < 0
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("Sección pública activa", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = active,
                        onCheckedChange = { active = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ColorGalyRed)
                    )
                }

                Button(
                    onClick = {
                        isSaving = true
                        val data = mutableMapOf<String, Any>()
                        aboutFields.forEachIndexed { index, (key, _) -> data[key] = fields[index] }
                        data["experiencia_anos"] = experienceValue!!
                        data["clientes_count"] = clientsValue!!
                        data["productos_count"] = productsValue!!
                        data["activo"] = active
                        scope.launch {
                            val error = ProductRepository.guardarInformacionNosotros(data)
                            isSaving = false
                            Toast.makeText(
                                context,
                                error ?: "Información de Nosotros de la web y GALY actualizada.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    enabled = requiredFieldsPresent && countsValid && videoValid && !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed)
                ) {
                    if (isSaving) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                    else Text("GUARDAR CAMBIOS", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
