package com.galyxnovatec.tienda.data.manager

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.galyxnovatec.tienda.data.model.CartItem
import com.galyxnovatec.tienda.data.model.Direccion
import com.galyxnovatec.tienda.data.model.Producto
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object CartManager {
    val items = mutableStateListOf<CartItem>()
    var cuponAplicado by mutableStateOf<String?>(null)
    var descuentoPorCupon by mutableDoubleStateOf(0.0)
    var direccionSeleccionada by mutableStateOf<Direccion?>(null)
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()

    /** Inicializa el carrito cargando datos locales si existen */
    fun inicializar(context: Context) {
        val prefs = context.getSharedPreferences("galy_cart", Context.MODE_PRIVATE)
        val cartJson = prefs.getString("items", null)
        if (cartJson != null) {
            try {
                val type = object : TypeToken<List<CartItem>>() {}.type
                val savedItems: List<CartItem> = gson.fromJson(cartJson, type)
                items.clear()
                items.addAll(savedItems)
            } catch (e: Exception) {}
        }
    }

    private fun guardarLocalmente(context: Context) {
        val prefs = context.getSharedPreferences("galy_cart", Context.MODE_PRIVATE)
        val cartJson = gson.toJson(items.toList())
        prefs.edit().putString("items", cartJson).apply()
    }

    /** Carga el carrito desde la base de datos para el usuario logueado 
     *  y fusiona los productos que ya estaban en el carrito local.
     */
    suspend fun sincronizarConServidor(context: Context) {
        val userId = AuthManager.currentUser?.id ?: return
        try {
            // 1. Guardar items locales actuales (posiblemente de cuando no estaba logueado)
            val itemsLocales = items.toList()
            
            // 2. Obtener items del servidor
            val remoteItems = RetrofitClient.instance.getCart(userId)
            
            // 3. Limpiar y reconstruir la lista local
            items.clear()
            
            // Añadir lo que viene del servidor
            remoteItems.forEach { 
                items.add(CartItem(it.producto, it.cantidad))
            }
            
            // 4. FUSIONAR: Si había algo local que NO está en el servidor, subirlo
            itemsLocales.forEach { local ->
                val existeEnRemoto = remoteItems.any { it.productoId == local.producto.id }
                if (!existeEnRemoto) {
                    items.add(local)
                    agregarAlServidor(local.producto.id, local.cantidad)
                }
            }
            
            guardarLocalmente(context)
            recalcularDescuento()
        } catch (e: Exception) {
            Log.e("GALY", "Error sincronizando carrito: ${e.message}")
        }
    }

    fun aplicarCupon(codigo: String): Boolean {
        val validCodes = listOf("GALY2024", "GALY2025", "GALY2026", "XNOVATEC")
        return if (validCodes.contains(codigo.uppercase())) {
            cuponAplicado = codigo.uppercase()
            recalcularDescuento()
            true
        } else {
            false
        }
    }

    fun quitarCupon() {
        cuponAplicado = null
        descuentoPorCupon = 0.0
    }

    fun agregarProducto(context: Context, producto: Producto, cantidad: Int = 1) {
        val existente = items.find { it.producto.id == producto.id }
        val nuevaCantidadTotal = (existente?.cantidad ?: 0) + cantidad
        
        if (nuevaCantidadTotal > producto.stock) return

        if (existente != null) {
            existente.cantidad += cantidad
            actualizarCantidadEnServidor(producto.id, existente.cantidad)
        } else {
            items.add(CartItem(producto, cantidad))
            agregarAlServidor(producto.id, cantidad)
        }
        guardarLocalmente(context)
        recalcularDescuento()
    }

    fun eliminarProducto(context: Context, producto: Producto) {
        items.removeAll { it.producto.id == producto.id }
        guardarLocalmente(context)
        recalcularDescuento()
        eliminarDelServidor(producto.id)
    }

    fun cambiarCantidad(context: Context, producto: Producto, nuevaCantidad: Int) {
        val item = items.find { it.producto.id == producto.id }
        if (item != null && nuevaCantidad > 0) {
            if (nuevaCantidad > producto.stock) return
            val index = items.indexOf(item)
            items[index] = item.copy(cantidad = nuevaCantidad)
            actualizarCantidadEnServidor(producto.id, nuevaCantidad)
            guardarLocalmente(context)
        }
        recalcularDescuento()
    }

    fun alternarSeleccion(productoId: Int) {
        val index = items.indexOfFirst { it.producto.id == productoId }
        if (index != -1) {
            val item = items[index]
            items[index] = item.copy(isSelected = !item.isSelected)
            recalcularDescuento()
        }
    }

    fun seleccionarTodo(seleccionar: Boolean) {
        for (i in items.indices) {
            items[i] = items[i].copy(isSelected = seleccionar)
        }
        recalcularDescuento()
    }

    private fun agregarAlServidor(productoId: Int, cantidad: Int) {
        val userId = AuthManager.currentUser?.id ?: return
        scope.launch {
            try {
                RetrofitClient.instance.addToCart(mapOf("usuario_id" to userId, "producto_id" to productoId, "cantidad" to cantidad))
            } catch (e: Exception) {}
        }
    }

    private fun actualizarCantidadEnServidor(productoId: Int, cantidad: Int) {
        val userId = AuthManager.currentUser?.id ?: return
        scope.launch {
            try {
                RetrofitClient.instance.updateCartQuantity(mapOf("usuario_id" to userId, "producto_id" to productoId, "cantidad" to cantidad))
            } catch (e: Exception) {}
        }
    }

    private fun eliminarDelServidor(productoId: Int) {
        val userId = AuthManager.currentUser?.id ?: return
        scope.launch {
            try {
                RetrofitClient.instance.deleteFromCart(mapOf("usuario_id" to userId, "producto_id" to productoId))
            } catch (e: Exception) {}
        }
    }

    private fun recalcularDescuento() {
        if (cuponAplicado != null) {
            descuentoPorCupon = obtenerSubtotal() * 0.10
        } else {
            descuentoPorCupon = 0.0
        }
    }

    fun limpiarCarrito(context: Context) {
        val userId = AuthManager.currentUser?.id
        items.clear()
        guardarLocalmente(context)
        quitarCupon()
        if (userId != null) {
            scope.launch {
                try {
                    RetrofitClient.instance.clearCart(mapOf("usuario_id" to userId))
                } catch (e: Exception) {}
            }
        }
    }

    fun eliminarItemsSeleccionados(context: Context) {
        val userId = AuthManager.currentUser?.id ?: return
        val itemsAElminar = items.filter { it.isSelected }
        items.removeAll { it.isSelected }
        guardarLocalmente(context)
        recalcularDescuento()
        
        scope.launch {
            itemsAElminar.forEach { 
                try {
                    RetrofitClient.instance.deleteFromCart(mapOf("usuario_id" to userId, "producto_id" to it.producto.id))
                } catch (e: Exception) {}
            }
        }
    }

    fun obtenerSubtotal(): Double {
        return items.filter { it.isSelected }.sumOf { it.producto.precioOferta * it.cantidad }
    }

    fun obtenerTotal(puntosAUsar: Int = 0): Double {
        val subtotal = obtenerSubtotal()
        val descuentoNivel = AuthManager.currentUser?.getDescuentoNivel() ?: 0.0
        val montoDescuentoNivel = subtotal * descuentoNivel
        
        val total = subtotal - descuentoPorCupon - puntosAUsar - montoDescuentoNivel
        return if (total < 0) 0.0 else total
    }
    
    fun totalItems(): Int = items.sumOf { it.cantidad }
    fun totalItemsSeleccionados(): Int = items.filter { it.isSelected }.sumOf { it.cantidad }

    /** Genera un mensaje detallado para WhatsApp */
    fun generarMensajeWhatsApp(direccion: Direccion? = null): String {
        val sb = StringBuilder()
        sb.append("🛍️ *NUEVO PEDIDO GALY XNOVATEC*\n\n")
        items.filter { it.isSelected }.forEach { item ->
            sb.append("• ${item.producto.nombre} (x${item.cantidad})\n")
        }
        sb.append("\n💰 *Subtotal:* S/ ${obtenerSubtotal().toInt()}")
        if (descuentoPorCupon > 0) {
            sb.append("\n🎫 *Cupón:* $cuponAplicado (-S/ ${descuentoPorCupon.toInt()})")
        }
        val descuentoNivel = AuthManager.currentUser?.getDescuentoNivel() ?: 0.0
        if (descuentoNivel > 0) {
            val monto = obtenerSubtotal() * descuentoNivel
            sb.append("\n🏆 *Beneficio ${AuthManager.currentUser?.getNivelNombre()}:* -S/ ${monto.toInt()}")
        }
        sb.append("\n✅ *TOTAL A PAGAR:* S/ ${obtenerTotal().toInt()}")
        
        if (direccion != null) {
            sb.append("\n\n📍 *ENTREGAR EN:*")
            sb.append("\n${direccion.direccion}, ${direccion.ciudad}")
            if (!direccion.referencia.isNullOrEmpty()) sb.append("\nRef: ${direccion.referencia}")
            if (!direccion.mapsUrl.isNullOrEmpty()) sb.append("\n🗺️ Ver en Mapas: ${direccion.mapsUrl}")
        }
        
        sb.append("\n\n📍 _Por favor, confírmenme los pasos para el pago._")
        return sb.toString()
    }
}
