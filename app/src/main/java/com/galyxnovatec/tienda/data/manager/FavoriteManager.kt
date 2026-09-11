package com.galyxnovatec.tienda.data.manager

import androidx.compose.runtime.mutableStateListOf
import com.galyxnovatec.tienda.data.model.Producto

object FavoriteManager {
    val items = mutableStateListOf<Producto>()
    
    fun toggleFavorito(producto: Producto) {
        if (items.any { it.id == producto.id }) {
            items.removeAll { it.id == producto.id }
        } else {
            items.add(producto)
        }
    }
    
    fun esFavorito(producto: Producto): Boolean = items.any { it.id == producto.id }
}
