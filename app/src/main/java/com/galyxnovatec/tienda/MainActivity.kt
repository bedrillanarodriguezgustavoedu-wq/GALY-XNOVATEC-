package com.galyxnovatec.tienda

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import coil.Coil
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.galyxnovatec.tienda.data.manager.AuthManager
import com.galyxnovatec.tienda.data.manager.CartManager
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.navigation.GalyNavGraph
import com.galyxnovatec.tienda.navigation.Screen
import com.galyxnovatec.tienda.ui.theme.GALYTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.Main).launch {
            ProductRepository.sincronizarTodo(this@MainActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar caché global de imágenes en Coil para eliminar tirones/lag al desplazar listas
        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    // El catálogo completo (164 imágenes) pesa ~115 MB: con 100 MB la caché
                    // se llenaba y expulsaba imágenes constantemente.
                    .maxSizeBytes(512L * 1024 * 1024)
                    .build()
            }
            .crossfade(true)
            .build()
        Coil.setImageLoader(imageLoader)

        // Cargar productos desde cache e iniciar sincronización
        ProductRepository.cargarCache(this)
        CartManager.inicializar(this)
        AuthManager.initialize(this)
        
        CoroutineScope(Dispatchers.Main).launch {
            ProductRepository.sincronizarTodo()
        }

        // Sincronización inicial del Carrito si el usuario ya está logueado
        if (AuthManager.currentUser != null) {
            val context = this
            CoroutineScope(Dispatchers.Main).launch {
                CartManager.sincronizarConServidor(context)
            }
        }

        enableEdgeToEdge()
setContent {
    GALYTheme {
        SolicitarPermisoNotificaciones()
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black,
        ) {
            val navController = rememberNavController()

            LaunchedEffect(intent) {
                val targetScreen = intent?.getStringExtra("EXTRA_TARGET_SCREEN")
                val orderId = intent?.getStringExtra("EXTRA_ORDER_ID")?.toIntOrNull()
                val productId = intent?.getStringExtra("EXTRA_PRODUCT_ID")?.toIntOrNull()

                if (targetScreen == "tracking" && orderId != null) {
                    navController.navigate(Screen.Tracking.createRoute(orderId))
                } else if (targetScreen == "detail" && productId != null) {
                    navController.navigate(Screen.Detail.createRoute(productId))
                } else if (targetScreen == "orders") {
                    navController.navigate(Screen.Orders.route)
                }
            }

            GalyNavGraph(navController = navController)
        }
    }
}
}
}

@Composable
private fun SolicitarPermisoNotificaciones() {
    val context = LocalContext.current
    var yaSolicitado by rememberSaveable { mutableStateOf(false) }
    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) AuthManager.vincularTokenFCM()
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !yaSolicitado &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            yaSolicitado = true
            permisoLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
