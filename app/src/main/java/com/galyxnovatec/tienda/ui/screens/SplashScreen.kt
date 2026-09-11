package com.galyxnovatec.tienda.ui.screens

import android.net.Uri
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.ui.draw.scale

import androidx.compose.foundation.isSystemInDarkTheme

@OptIn(UnstableApi::class)
@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var videoEnded by remember { mutableStateOf(false) }
    var minTimeElapsed by remember { mutableStateOf(false) }
    
    // Forzamos estilo oscuro para el video siempre
    val colorFondo = Color.Black

    // Cuando AMBAS condiciones se cumplen, avanzamos
    LaunchedEffect(videoEnded, minTimeElapsed) {
        if (videoEnded && minTimeElapsed) {
            onAnimationFinished()
        }
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(2500, 5000, 1000, 1500) // Valores más robustos para emulador
                    .build()
            )
            .build().apply {
                val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.gus}")
                setMediaItem(MediaItem.fromUri(videoUri))
                setAudioAttributes(AudioAttributes.DEFAULT, false)
                prepare()
                playWhenReady = true
                
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            videoEnded = true
                        }
                    }
                })
            }
    }

    LaunchedEffect(Unit) {
        // Sincronización de datos en segundo plano con retraso para dar prioridad al video
        scope.launch(Dispatchers.IO) {
            delay(2000) // Permitimos que el video inicie sin carga de red/CPU adicional
            try { ProductRepository.sincronizarTodo() } catch(e: Exception) {}
        }
        
        // Tiempo mínimo garantizado (7 segundos para que se vea el logo)
        delay(7000)
        minTimeElapsed = true
        
        // Fail-safe: Si después de 12 segundos el video no dio señal de terminar, avanzamos igual
        delay(5000)
        videoEnded = true
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    player = exoPlayer
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .scale(1.1f) // Zoom reducido para evitar recortes negros
        )
        
        // FILTRO DE VIDEO (Simplificado para mejor rendimiento en emulador)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.3f),
                        0.5f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.6f)
                    )
                )
        )
        
        // PARCHE DE SEGURIDAD NEGRO (Tapa el logo de Renderforest)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(115.dp) 
                .background(Color.Black)
        )
    }
}
