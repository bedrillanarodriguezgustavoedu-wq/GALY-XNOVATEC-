package com.galyxnovatec.tienda.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.model.Banner
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun BannerPromocional(banners: List<Banner>) {
    val listaBanners = remember(banners) {
        val base = if (banners.isEmpty()) {
            listOf(
                Banner(1, "laptop_1.jpg", "Tectron Galy", "Tu tienda de tecnología y accesorios. Envío a todo el Perú, pago 100% seguro."),
                Banner(2, "audio_1.jpg", "SONIDO PRO", "20% OFF"),
                Banner(3, "monitor_1.jpg", "VISIÓN 4K", "Calidad Ultra")
            )
        } else banners

        base.sortedByDescending { it.titulo?.contains("Tectron", true) == true || it.titulo?.contains("Galy", true) == true }
    }

    val infinitePageCount = 1000
    val pagerState = rememberPagerState(initialPage = infinitePageCount / 2, pageCount = { infinitePageCount })

    // Auto-scroll optimizado cada 5 segundos
    LaunchedEffect(listaBanners.size) {
        if (listaBanners.size > 1) {
            while (true) {
                delay(5000)
                try {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                } catch (e: Exception) { }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(20.dp))
        ) { page ->
            val index = Math.floorMod(page, listaBanners.size)
            val banner = listaBanners[index]
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // 1. Imagen de fondo completa (Full Background Crop)
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(banner.getFullImageUrl())
                        .crossfade(true)
                        .size(1000, 1000)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.logo_2),
                    placeholder = painterResource(R.drawable.logo_2)
                )

                // 2. Gradiente oscuro superpuesto para garantizar que el texto sea perfectamente legible
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.95f),
                                    Color.Black.copy(alpha = 0.75f),
                                    Color.Black.copy(alpha = 0.35f)
                                )
                            )
                        )
                )

                // 3. Contenido de texto superpuesto a la izquierda
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        color = ColorGalyRed,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if ((banner.subtitulo?.length ?: 0) > 36) "TIENDA ONLINE" else "PROMOCIÓN GALY",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = banner.titulo ?: "OFERTA IMPERDIBLE",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 24.sp
                    )

                    if (!banner.subtitulo.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        BannerTimerBadge(subtitulo = banner.subtitulo)
                    }
                }
            }
        }

        // Indicadores
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(listaBanners.size) { i ->
                val isSelected = Math.floorMod(pagerState.currentPage, listaBanners.size) == i
                Box(
                    modifier = Modifier
                        .padding(3.dp)
                        .size(if (isSelected) 8.dp else 5.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) ColorGalyRed else Color.DarkGray)
                )
            }
        }
    }
}

@Composable
fun BannerTimerBadge(subtitulo: String) {
    val texto = subtitulo.trim()
    val esOferta = texto.contains("TERMINA", true)

    if (texto.length > 36 && !esOferta) {
        Text(
            text = texto,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 15.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        return
    }

    if (esOferta) {
        BannerCountdownBadge()
        return
    }

    Surface(
        color = ColorGalyRed,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = texto,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun BannerCountdownBadge() {
    var tiempoRestante by remember { mutableLongStateOf(2 * 60 * 60 * 1000L) }
    LaunchedEffect(Unit) {
        while (tiempoRestante > 0) {
            delay(1000)
            tiempoRestante -= 1000
        }
    }

    val hours = (tiempoRestante / (1000 * 60 * 60)) % 24
    val minutes = (tiempoRestante / (1000 * 60)) % 60
    val seconds = (tiempoRestante / 1000) % 60
    val timerString = remember(hours, minutes, seconds) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    Surface(
        color = Color.Yellow,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = "TERMINA: $timerString",
            color = Color.Black,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
