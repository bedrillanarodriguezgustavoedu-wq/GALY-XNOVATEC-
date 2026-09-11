package com.galyxnovatec.tienda.data.manager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.galyxnovatec.tienda.data.model.CartItem
import com.galyxnovatec.tienda.data.model.Direccion
import com.galyxnovatec.tienda.data.model.Producto

object CartManager {
    val items = mutableStateListOf<CartItem>()
    var cuponAplicado by mutableStateOf<String?>(null)
    var descuentoPorCupon by mutableDoubleStateOf(0.0)
    var direccionSeleccionada by mutableStateOf<Direccion?>(null)

    fun aplicarCupon(codigo: String): Boolean {
        return if (codigo.uppercase() == "GALY2024" || codigo.uppercase() == "XNOVATEC") {
            cuponAplicado = codigo.uppercase()
            descuentoPorCupon = obtenerSubtotal() * 0.10 // 10% de descuento
            true
        } else {
            false
        }
    }

    fun quitarCupon() {
        cuponAplicado = null
        descuentoPorCupon = 0.0
    }

    fun agregarProducto(producto: Producto, cantidad: Int = 1) {
        val existente = items.find { it.producto.id == producto.id }
        if (existente != null) {
            existente.cantidad += cantidad
        } else {
            items.add(CartItem(producto, cantidad))
        }
        recalcularDescuento()
    }

    fun eliminarProducto(producto: Producto) {
        items.removeAll { it.producto.id == producto.id }
        recalcularDescuento()
    }

    fun cambiarCantidad(producto: Producto, nuevaCantidad: Int) {
        val item = items.find { it.producto.id == producto.id }
        if (item != null && nuevaCantidad > 0) {
            val index = items.indexOf(item)
            items[index] = item.copy(cantidad = nuevaCantidad)
        }
        recalcularDescuento()
    }

    private fun recalcularDescuento() {
        if (cuponAplicado != null) {
            descuentoPorCupon = obtenerSubtotal() * 0.10
        }
    }

    fun limpiarCarrito() {
        items.clear()
        quitarCupon()
    }

    fun obtenerSubtotal(): Double {
        return items.sumOf { it.producto.precioOferta * it.cantidad }
    }

    fun obtenerTotal(): Double {
        return obtenerSubtotal() - descuentoPorCupon
    }
    
    fun totalItems(): Int = items.sumOf { it.cantidad }

    /** Genera un mensaje detallado para WhatsApp */
    fun generarMensajeWhatsApp(direccion: Direccion? = null): String {
        val sb = StringBuilder()
        sb.append("🛍️ *NUEVO PEDIDO GALY XNOVATEC*\n\n")
        items.forEach { item ->
            sb.append("• ${item.producto.nombre} (x${item.cantidad})\n")
        }
        sb.append("\n💰 *Subtotal:* S/ ${obtenerSubtotal().toInt()}")
        if (descuentoPorCupon > 0) {
            sb.append("\n🎫 *Cupón:* $cuponAplicado (-S/ ${descuentoPorCupon.toInt()})")
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
