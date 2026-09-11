package com.galyxnovatec.tienda.data.model

import android.net.Uri
import com.google.gson.annotations.SerializedName

data class Banner(
    @SerializedName("id") val id: Int,
    @SerializedName("imagen_url") val imagenUrl: String,
    @SerializedName("titulo") val titulo: String? = null,
    @SerializedName("subtitulo") val subtitulo: String? = null, 
    @SerializedName("color_fondo") val colorFondo: String? = null
) {
    /**
     * Devuelve la URL completa de la imagen del banner, manejando espacios de forma segura.
     */
    fun getFullImageUrl(): String {
        if (imagenUrl.startsWith("http")) return imagenUrl
        
        // Codificamos el nombre del archivo para manejar espacios (ej: %20)
        val safeName = Uri.encode(imagenUrl)
        return "http://10.0.2.2/galy_api/img/$safeName"
    }
}
