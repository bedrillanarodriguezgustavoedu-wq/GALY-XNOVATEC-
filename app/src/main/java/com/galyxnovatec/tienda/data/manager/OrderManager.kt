package com.galyxnovatec.tienda.data.manager

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.galyxnovatec.tienda.data.model.CartItem
import com.galyxnovatec.tienda.data.model.Direccion
import com.galyxnovatec.tienda.data.model.Pedido
import com.galyxnovatec.tienda.data.network.OrderItemRequest
import com.galyxnovatec.tienda.data.network.OrderRequest
import com.galyxnovatec.tienda.data.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

object OrderManager {
    val orders = mutableStateListOf<Pedido>()
    var isLoading by mutableStateOf(false)
    var isSubmittingReceipt by mutableStateOf(false)

    suspend fun fetchOrders(userId: Int) {
        isLoading = true
        try {
            val remoteOrders = RetrofitClient.instance.getPedidos(userId)
            orders.clear()
            orders.addAll(remoteOrders)
        } catch (e: Exception) {
            android.util.Log.e("GALY", "Error cargando pedidos: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    suspend fun fetchAllOrders() {
        isLoading = true
        try {
            val remoteOrders = RetrofitClient.instance.getAllOrders()
            orders.clear()
            orders.addAll(remoteOrders)
        } catch (e: Exception) {
            Log.e("GALY", "Error cargando todos los pedidos: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    suspend fun updateOrderStatus(orderId: Int, newStatus: String): Boolean {
        return try {
            val response = RetrofitClient.instance.updateOrderStatus(
                mapOf("id" to orderId, "estado" to newStatus)
            )
            response["success"] == true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun placeOrder(
        userId: Int?, 
        items: List<CartItem>, 
        total: Double,
        metodoPago: String? = null,
        metodoEntrega: String? = null,
        direccion: Direccion? = null,
        dni: String? = null
    ): Any? {
        if (userId == null) return "Usuario no identificado"

        isLoading = true
        return try {
            val orderRequest = OrderRequest(
                usuario_id = userId,
                total = total,
                items = items.map {
                    OrderItemRequest(
                        producto_id = it.producto.id,
                        cantidad = it.cantidad,
                        precio = it.producto.precioOferta
                    )
                },
                metodo_pago = metodoPago?.lowercase(),
                cliente_nombre = AuthManager.currentUser?.nombre,
                cliente_telefono = AuthManager.currentUser?.telefono,
                cliente_email = AuthManager.currentUser?.email,
                cliente_dni = dni,
                metodo_entrega = metodoEntrega,
                direccion = direccion?.direccion,
                ciudad = direccion?.ciudad,
                distrito = direccion?.distrito,
                referencia = direccion?.referencia,
                maps_url = direccion?.mapsUrl,
                cupon = CartManager.cuponAplicado,
                cupon_descuento = CartManager.descuentoPorCupon.takeIf { it > 0 }
            )
            val response = RetrofitClient.instance.createOrder(orderRequest)
            if (response["success"] == true) {
                val orderId = (response["pedido_id"] as? Number)?.toInt()
                if (orderId == null || orderId <= 0) {
                    return "El servidor no devolvió el número del pedido."
                }
                // Actualizar puntos del usuario localmente si la compra fue exitosa
                val nuevosPuntos = (total / 100).toInt()
                AuthManager.currentUser = AuthManager.currentUser?.let { 
                    it.copy(puntos = it.puntos + nuevosPuntos)
                }
                
                fetchOrders(userId)
                orderId
            } else {
                response["message"] ?: "Error desconocido en el servidor"
            }
        } catch (e: Exception) {
            Log.e("GALY", "Error de red: ${e.message}")
            "Error de conexión: Verifica que XAMPP esté encendido"
        } finally {
            isLoading = false
        }
    }

    suspend fun sendPaymentReceipt(
        context: Context,
        orderId: Int,
        method: String,
        imageUri: Uri,
        operationNumber: String? = null
    ): String? {
        val idToken = AuthManager.googleIdToken
            ?: return "La sesión de Google venció. Inicia sesión nuevamente."

        isSubmittingReceipt = true
        return try {
            val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                ?: return "No se pudo leer la imagen seleccionada."
            if (bytes.size > 5 * 1024 * 1024) {
                return "La imagen supera el límite de 5 MB."
            }

            val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
            val extension = when (mimeType.lowercase()) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            val imageBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData(
                "comprobante",
                "comprobante_$orderId.$extension",
                imageBody
            )
            val textType = "text/plain".toMediaTypeOrNull()
            val response = RetrofitClient.webInstance.sendPaymentReceipt(
                idToken.toRequestBody(textType),
                orderId.toString().toRequestBody(textType),
                method.toRequestBody(textType),
                operationNumber?.takeIf { it.isNotBlank() }?.toRequestBody(textType),
                imagePart
            )

            if (response.success) null else response.message
        } catch (exception: Exception) {
            Log.e("GALY", "No se pudo enviar el comprobante del pedido $orderId", exception)
            "No se pudo enviar el comprobante. Verifica tu conexión e inténtalo nuevamente."
        } finally {
            isSubmittingReceipt = false
        }
    }

}
