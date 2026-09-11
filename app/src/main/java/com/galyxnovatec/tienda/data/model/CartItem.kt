package com.galyxnovatec.tienda.data.model

data class CartItem(
    val producto: Producto,
    var cantidad: Int = 1
)
