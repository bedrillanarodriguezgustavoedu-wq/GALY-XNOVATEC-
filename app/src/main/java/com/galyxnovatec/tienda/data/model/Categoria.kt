package com.galyxnovatec.tienda.data.model

import com.google.gson.annotations.SerializedName

data class Categoria(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nombre")
    val nombre: String
)
