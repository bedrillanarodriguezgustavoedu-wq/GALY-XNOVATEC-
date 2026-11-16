package com.galyxnovatec.tienda.data.model

import com.google.gson.annotations.SerializedName

data class Categoria(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("slug")
    val slug: String? = null,
    @SerializedName("descripcion")
    val descripcion: String? = null,
    @SerializedName("icono")
    val icono: String? = null,
    @SerializedName("productos_count")
    val productosCount: Int = 0
)
