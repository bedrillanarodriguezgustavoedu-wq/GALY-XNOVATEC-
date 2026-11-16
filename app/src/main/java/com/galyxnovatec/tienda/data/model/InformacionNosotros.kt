package com.galyxnovatec.tienda.data.model

import com.google.gson.annotations.SerializedName

data class InformacionNosotros(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("titulo_principal") val tituloPrincipal: String = "",
    @SerializedName("subtitulo") val subtitulo: String = "",
    @SerializedName("descripcion_principal") val descripcionPrincipal: String = "",
    @SerializedName("historia") val historia: String = "",
    @SerializedName("mision") val mision: String = "",
    @SerializedName("vision") val vision: String = "",
    @SerializedName("valores") val valores: String = "",
    @SerializedName("imagen_principal") val imagenPrincipal: String? = null,
    @SerializedName("video_url") val videoUrl: String? = null,
    @SerializedName("experiencia_anos") val experienciaAnos: Int = 0,
    @SerializedName("clientes_count") val clientesCount: Int = 0,
    @SerializedName("productos_count") val productosCount: Int = 0,
    @SerializedName("activo") val activo: Boolean = true
)
