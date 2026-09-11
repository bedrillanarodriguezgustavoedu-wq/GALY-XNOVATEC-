package com.galyxnovatec.tienda.data.manager

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

object OrderManager {
    val orders = mutableStateListOf<Pedido>()
    var isLoading by mutableStateOf(false)

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
        direccion: Direccion? = null
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
                metodo_pago = metodoPago,
                cliente_telefono = AuthManager.currentUser?.telefono,
                direccion = direccion?.direccion,
                ciudad = direccion?.ciudad,
                distrito = direccion?.distrito,
                referencia = direccion?.referencia,
                maps_url = direccion?.mapsUrl
            )
            val response = RetrofitClient.instance.createOrder(orderRequest)
            if (response["success"] == true) {
                fetchOrders(userId)
                true // Éxito
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
}
