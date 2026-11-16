package com.galyxnovatec.tienda.ui.screens

import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.compose.ui.tooling.preview.Preview
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var hasNavigated by remember { mutableStateOf(false) }
    val alphaAnim = remember { Animatable(1f) }

    fun finishSplash() {
        if (!hasNavigated) {
            hasNavigated = true
            onAnimationFinished()
        }
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(2000, 5000, 1000, 1500)
                    .build()
            )
            .build().apply {
                val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.gus}")
                setMediaItem(MediaItem.fromUri(videoUri))
                setAudioAttributes(AudioAttributes.DEFAULT, false)
                repeatMode = Player.REPEAT_MODE_ONE // Bucle continuo para asegurar reproducción completa
                volume = 1.0f
                prepare()
                playWhenReady = true
                
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("GALY_SPLASH", "Error en video intro: ${error.message}")
                    }
                })
            }
    }

    LaunchedEffect(Unit) {
        // Carga de productos en segundo plano
        scope.launch(Dispatchers.IO) {
            try { ProductRepository.sincronizarTodo() } catch (e: Exception) {}
        }
        
        // Tiempo pausado y cinemático garantizado (11 segundos completos)
        delay(11000)
        alphaAnim.animateTo(0f, animationSpec = tween(800))
        finishSplash()
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                exoPlayer.stop()
                exoPlayer.release()
            } catch (e: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .alpha(alphaAnim.value)
            .clickable { finishSplash() },
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
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun SplashScreenPreview() {
    SplashScreen {}
}
