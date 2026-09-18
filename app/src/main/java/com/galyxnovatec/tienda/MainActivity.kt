package com.galyxnovatec.tienda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.galyxnovatec.tienda.data.manager.AuthManager
import com.galyxnovatec.tienda.data.manager.CartManager
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.navigation.GalyNavGraph
import com.galyxnovatec.tienda.ui.theme.GALYTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Cargar productos desde cache e iniciar sincronización
        ProductRepository.cargarCache(this)
        CartManager.inicializar(this)
        
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
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black,
        ) {
            val navController = rememberNavController()
            GalyNavGraph(navController = navController)
        }
    }
}
}
}
