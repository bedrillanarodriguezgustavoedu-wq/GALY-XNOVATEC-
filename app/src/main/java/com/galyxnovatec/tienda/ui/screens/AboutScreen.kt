package com.galyxnovatec.tienda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.data.model.InformacionNosotros
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(alVolver: () -> Unit) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var loadFailed by remember { mutableStateOf(false) }
    val about = ProductRepository.informacionNosotros

    fun refreshAbout() {
        scope.launch {
            isLoading = true
            loadFailed = !ProductRepository.sincronizarInformacionNosotros()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refreshAbout() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sobre GALY", color = Color.White, fontWeight = FontWeight.Black) },
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
        when {
            isLoading && about == null -> Box(
                Modifier.fillMaxSize().padding(padding).background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ColorGalyRed)
            }

            about == null -> Column(
                Modifier.fillMaxSize().padding(padding).background(Color.Black).padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    if (loadFailed) "No se pudo cargar la información. Comprueba tu conexión e inténtalo de nuevo."
                    else "La información de Nosotros aún no está configurada.",
                    color = Color.White
                )
                TextButton(onClick = { refreshAbout() }) { Text("Reintentar") }
            }

            !about.activo -> Box(
                Modifier.fillMaxSize().padding(padding).background(Color.Black).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Esta sección no está disponible en este momento.", color = Color.White)
            }

            else -> AboutContent(about = about, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun AboutContent(about: InformacionNosotros, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            about.subtitulo,
            color = ColorGalyRed,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            about.tituloPrincipal,
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black
        )
        Text(about.descripcionPrincipal, color = Color.LightGray, fontSize = 16.sp, lineHeight = 24.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AboutStat("Experiencia", about.experienciaAnos, Modifier.weight(1f))
            AboutStat("Clientes", about.clientesCount, Modifier.weight(1f))
            AboutStat("Productos", about.productosCount, Modifier.weight(1f))
        }

        AboutTextCard("Nuestra historia", about.historia)
        AboutTextCard("Misión", about.mision)
        AboutTextCard("Visión", about.vision)

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF151515)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Nuestros valores", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                about.valores.split(',', '\n')
                    .map(String::trim)
                    .filter(String::isNotBlank)
                    .forEach { value ->
                        Text("• $value", color = Color.LightGray, lineHeight = 22.sp)
                    }
            }
        }
    }
}

@Composable
private fun AboutStat(label: String, value: Int, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color(0xFF151515))) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$value+", color = ColorGalyRed, fontSize = 21.sp, fontWeight = FontWeight.Black)
            Text(label, color = Color.LightGray, fontSize = 10.sp)
        }
    }
}

@Composable
private fun AboutTextCard(title: String, text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151515)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text, color = Color.LightGray, lineHeight = 22.sp)
        }
    }
}
