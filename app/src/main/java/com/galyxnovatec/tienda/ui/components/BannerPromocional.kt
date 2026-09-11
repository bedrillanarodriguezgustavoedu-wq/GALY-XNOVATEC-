package com.galyxnovatec.tienda.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.model.Banner
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import com.galyxnovatec.tienda.ui.theme.ColorGalyDarkRed
import kotlinx.coroutines.delay


import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun BannerPromocional(banners: List<Banner>) {
    // Timer para Oferta Relámpago (2 horas)
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
    val timerString = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

    // Lista de banners real o con imágenes de muestra si está vacía
    val listaBanners = if (banners.isEmpty()) {
        listOf(
            Banner(1, "laptop_1.jpg", "LAPTOPS GAMER", "TERMINA EN: $timerString"),
            Banner(2, "audio_1.jpg", "SONIDO PRO", "20% OFF"),
            Banner(3, "monitor_1.jpg", "VISIÓN 4K", "Calidad Ultra")
        )
    } else {
        banners
    }

    // Pager para el carrusel infinito
    val infinitePageCount = 1000
    val pagerState = rememberPagerState(initialPage = infinitePageCount / 2, pageCount = { infinitePageCount })

    // Auto-scroll: Se mueve solo cada 4 segundos
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            try {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            } catch (e: Exception) { }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Un poco más alto para que luzcan las fotos
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
        ) { page ->
            val index = Math.floorMod(page, listaBanners.size)
            val banner = listaBanners[index]
            
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF111111))) {
                // IMAGEN DEL ANUNCIO
                AsyncImage(
                    model = banner.getFullImageUrl(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop, // Llena todo el espacio
                    error = painterResource(R.drawable.logo_2),
                    placeholder = painterResource(R.drawable.logo_2)
                )
                
                // Capa oscura para que el texto resalte
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )

                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = banner.titulo ?: "",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    
                    if (!banner.subtitulo.isNullOrEmpty()) {
                        val esOferta = banner.subtitulo!!.contains("TERMINA", true)
                        Surface(
                            color = if (esOferta) Color.Yellow else ColorGalyRed,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = if(esOferta) "OFERTA TERMINA: $timerString" else banner.subtitulo!!,
                                color = if (esOferta) Color.Black else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Puntos de navegación (indicadores)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(listaBanners.size) { i ->
                val isSelected = Math.floorMod(pagerState.currentPage, listaBanners.size) == i
                Box(
                    modifier = Modifier
                        .padding(3.dp)
                        .size(if (isSelected) 10.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) ColorGalyRed else Color.DarkGray)
                )
            }
        }
    }
}

