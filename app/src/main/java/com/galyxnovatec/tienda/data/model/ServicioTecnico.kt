package com.galyxnovatec.tienda.data.model

import com.google.gson.annotations.SerializedName

data class ServicioTecnico(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre_servicio") val nombre: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("precio_base") val precioBase: Double
)
