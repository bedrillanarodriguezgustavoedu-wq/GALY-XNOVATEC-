package com.galyxnovatec.tienda.data.model

import com.google.gson.annotations.SerializedName

data class Cupon(
    @SerializedName("id") val id: Int,
    @SerializedName("codigo") val codigo: String,
    @SerializedName("descuento") val descuento: Double,
    @SerializedName("tipo") val tipo: String, // 'fijo' o 'porcentaje'
    @SerializedName("estado") val estado: Int // 1 activo, 0 inactivo
)
