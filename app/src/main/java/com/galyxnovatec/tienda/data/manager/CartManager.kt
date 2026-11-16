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
import kotlinx.coroutines.withContext
import kotlin.math.round

object CartManager {
    val items = mutableStateListOf<CartItem>()
    var cuponAplicado by mutableStateOf<String?>(null)
    var descuentoPorCupon by mutableDoubleStateOf(0.0)
    var validandoCupon by mutableStateOf(false)
    var direccionSeleccionada by mutableStateOf<Direccion?>(null)

    private var porcentajeCupon = 0

    private data class ValidacionCupon(
        val exito: Boolean,
        val mensaje: String,
        val codigo: String = "",
        val porcentaje: Int = 0
    )

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
            } catch (e: Exception) {
                Log.e("GALY", "No se pudo cargar el carrito guardado", e)
            }
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

    fun aplicarCupon(codigo: String, alTerminar: (Boolean, String) -> Unit) {
        val codigoLimpio = codigo.trim().uppercase()
        if (codigoLimpio.isEmpty()) {
            alTerminar(false, "Escribe el código del cupón.")
            return
        }
        if (validandoCupon) return

        validandoCupon = true
        val subtotal = obtenerSubtotal()
        scope.launch {
            val resultado = try {
                val respuesta = RetrofitClient.instance.validarCupon(
                    mapOf("codigo" to codigoLimpio, "subtotal" to subtotal)
                )
                val porcentaje = (respuesta["descuento_porcentaje"] as? Number)?.toInt() ?: 0
                val mensaje = respuesta["message"]?.toString()
                if (respuesta["success"] == true && porcentaje > 0) {
                    ValidacionCupon(
                        exito = true,
                        mensaje = mensaje ?: "Cupón aplicado: $porcentaje% de descuento.",
                        codigo = respuesta["codigo"]?.toString() ?: codigoLimpio,
                        porcentaje = porcentaje
                    )
                } else {
                    ValidacionCupon(false, mensaje ?: "Ese cupón ya no está disponible.")
                }
            } catch (error: Exception) {
                Log.e("GALY", "No se pudo validar el cupón $codigoLimpio", error)
                ValidacionCupon(false, "No se pudo validar el cupón. Revisa tu conexión e inténtalo otra vez.")
            }

            withContext(Dispatchers.Main) {
                validandoCupon = false
                if (resultado.exito) {
                    cuponAplicado = resultado.codigo
                    porcentajeCupon = resultado.porcentaje
                    recalcularDescuento()
                } else {
                    quitarCupon()
                }
                alTerminar(resultado.exito, resultado.mensaje)
            }
        }
    }

    fun quitarCupon() {
        cuponAplicado = null
        descuentoPorCupon = 0.0
        porcentajeCupon = 0
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
                requireSuccessfulCartResponse(
                    RetrofitClient.instance.addToCart(
                        mapOf("usuario_id" to userId, "producto_id" to productoId, "cantidad" to cantidad)
                    ),
                    "agregar el producto"
                )
            } catch (e: Exception) {
                Log.e("GALY", "No se pudo sincronizar el producto agregado al carrito", e)
            }
        }
    }

    private fun actualizarCantidadEnServidor(productoId: Int, cantidad: Int) {
        val userId = AuthManager.currentUser?.id ?: return
        scope.launch {
            try {
                requireSuccessfulCartResponse(
                    RetrofitClient.instance.updateCartQuantity(
                        mapOf("usuario_id" to userId, "producto_id" to productoId, "cantidad" to cantidad)
                    ),
                    "actualizar la cantidad"
                )
            } catch (e: Exception) {
                Log.e("GALY", "No se pudo sincronizar la cantidad del carrito", e)
            }
        }
    }

    private fun eliminarDelServidor(productoId: Int) {
        val userId = AuthManager.currentUser?.id ?: return
        scope.launch {
            try {
                requireSuccessfulCartResponse(
                    RetrofitClient.instance.deleteFromCart(mapOf("usuario_id" to userId, "producto_id" to productoId)),
                    "eliminar el producto"
                )
            } catch (e: Exception) {
                Log.e("GALY", "No se pudo eliminar el producto del carrito remoto", e)
            }
        }
    }

    private fun requireSuccessfulCartResponse(response: Map<String, Any>, operation: String) {
        if (response["success"] != true) {
            throw IllegalStateException(response["message"]?.toString() ?: "No se pudo $operation.")
        }
    }

    private fun recalcularDescuento() {
        val codigo = cuponAplicado
        descuentoPorCupon = if (codigo != null && porcentajeCupon > 0) {
            val bruto = obtenerSubtotal() * porcentajeCupon / 100.0
            round(bruto * 100) / 100
        } else {
            0.0
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
                    requireSuccessfulCartResponse(
                        RetrofitClient.instance.clearCart(mapOf("usuario_id" to userId)),
                        "vaciar el carrito"
                    )
                } catch (e: Exception) {
                    Log.e("GALY", "No se pudo vaciar el carrito remoto", e)
                }
            }
        }
    }

    fun eliminarItemsSeleccionados(context: Context) {
        val userId = AuthManager.currentUser?.id
        val itemsAElminar = items.filter { it.isSelected }
        items.removeAll { it.isSelected }
        guardarLocalmente(context)
        recalcularDescuento()

        if (userId == null) return

        scope.launch {
            itemsAElminar.forEach { 
                try {
                    requireSuccessfulCartResponse(
                        RetrofitClient.instance.deleteFromCart(
                            mapOf("usuario_id" to userId, "producto_id" to it.producto.id)
                        ),
                        "eliminar el producto"
                    )
                } catch (e: Exception) {
                    Log.e("GALY", "No se pudo eliminar un producto seleccionado del carrito remoto", e)
                }
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
