package com.galyxnovatec.tienda.data.model

import com.google.gson.annotations.SerializedName

data class Cupon(
    @SerializedName("id") val id: Int,
    @SerializedName("codigo") val codigo: String,
    @SerializedName("descuento_porcentaje") val descuentoPorcentaje: Int,
    @SerializedName("estado") val estado: Int = 1
)
