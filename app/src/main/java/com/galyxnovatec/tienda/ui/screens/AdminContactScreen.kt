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
import com.galyxnovatec.tienda.data.model.InformacionContacto
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

private val contactFields = listOf(
    "nombre_empresa" to "Nombre de la empresa *",
    "direccion" to "Dirección *",
    "pais" to "País *",
    "departamento" to "Departamento *",
    "provincia" to "Provincia o ciudad *",
    "telefono_principal" to "Teléfono principal *",
    "telefono_secundario" to "Teléfono secundario",
    "whatsapp" to "WhatsApp (número o enlace)",
    "email" to "Correo electrónico *",
    "horario_atencion" to "Horario de atención *",
    "google_maps_link" to "Enlace o inserción de Google Maps",
    "facebook_link" to "Facebook",
    "instagram_link" to "Instagram",
    "youtube_link" to "YouTube",
    "twitter_link" to "X / Twitter",
    "mensaje_bienvenida" to "Mensaje de bienvenida"
)

private fun InformacionContacto.toEditableValues(): List<String> = listOf(
    nombreEmpresa,
    direccion,
    pais,
    departamento,
    provincia,
    telefonoPrincipal,
    telefonoSecundario.orEmpty(),
    whatsapp.orEmpty(),
    email,
    horarioAtencion,
    googleMapsLink.orEmpty(),
    facebookLink.orEmpty(),
    instagramLink.orEmpty(),
    youtubeLink.orEmpty(),
    twitterLink.orEmpty(),
    mensajeBienvenida.orEmpty()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminContactScreen(alVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fields = remember { mutableStateListOf<String>() }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var active by remember { mutableStateOf(true) }
    var loadedContactId by remember { mutableIntStateOf(-1) }

    fun refreshContact() {
        scope.launch {
            isLoading = true
            val success = ProductRepository.sincronizarInformacionContacto()
            isLoading = false
            if (!success) {
                Toast.makeText(context, "No se pudo cargar el contacto compartido.", Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(Unit) { refreshContact() }
    val contact = ProductRepository.informacionContacto
    LaunchedEffect(contact?.id) {
        if (contact != null && contact.id != loadedContactId) {
            fields.clear()
            fields.addAll(contact.toEditableValues())
            active = contact.activo
            loadedContactId = contact.id
        }
    }

    val requiredFieldsPresent = listOf(0, 1, 2, 3, 4, 5, 8, 9).all { index ->
        fields.getOrNull(index)?.isNotBlank() == true
    }
    val emailIsValid = fields.getOrNull(8)?.let { Patterns.EMAIL_ADDRESS.matcher(it).matches() } == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Contacto compartido", color = Color.White, fontWeight = FontWeight.Black)
                        Text("Información de la web y GALY", color = Color.LightGray, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { refreshContact() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        if (isLoading && contact == null) {
            Box(
                Modifier.fillMaxSize().padding(padding).background(Color.Black),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator(color = ColorGalyRed)
            }
        } else if (contact == null) {
            Box(Modifier.fillMaxSize().padding(padding).background(Color.Black), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No existe información de contacto. Configúrala primero desde el panel web.", color = Color.White)
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
                    "Los datos se guardan en el mismo contacto que muestra la web. El banner de imagen se conserva y se administra desde la web.",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )

                contactFields.forEachIndexed { index, (key, label) ->
                    OutlinedTextField(
                        value = fields.getOrElse(index) { "" },
                        onValueChange = { value -> if (index < fields.size) fields[index] = value },
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = if (key == "mensaje_bienvenida" || key == "google_maps_link") 3 else 1,
                        singleLine = key != "mensaje_bienvenida" && key != "google_maps_link",
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("Página de contacto activa en la web", color = Color.White, fontWeight = FontWeight.SemiBold)
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
                        contactFields.forEachIndexed { index, (key, _) ->
                            data[key] = fields[index]
                        }
                        data["activo"] = active
                        scope.launch {
                            val error = ProductRepository.guardarInformacionContacto(data)
                            isSaving = false
                            if (error == null) {
                                Toast.makeText(context, "Contacto de la web y GALY actualizado.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = requiredFieldsPresent && emailIsValid && !isSaving,
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
