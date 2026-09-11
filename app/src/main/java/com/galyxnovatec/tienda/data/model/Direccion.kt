package com.galyxnovatec.tienda.data.model

import com.google.gson.annotations.SerializedName

data class Direccion(
    @SerializedName("id") val id: Int,
    @SerializedName("usuario_id") val usuarioId: Int,
    @SerializedName("direccion") val direccion: String,
    @SerializedName("distrito") val distrito: String? = null,
    @SerializedName("ciudad") val ciudad: String,
    @SerializedName("referencia") val referencia: String?,
    @SerializedName("es_principal") val esPrincipal: Boolean = false,
    @SerializedName("maps_url") val mapsUrl: String? = null
)
