package com.galyxnovatec.tienda.data.model

import android.net.Uri
import com.galyxnovatec.tienda.BuildConfig
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.google.gson.annotations.SerializedName

data class Banner(
    @SerializedName("id") val id: Int,
    @SerializedName("imagen_url") val imagenUrl: String,
    @SerializedName("titulo") val titulo: String? = null,
    @SerializedName("subtitulo") val subtitulo: String? = null, 
    @SerializedName("color_fondo") val colorFondo: String? = null
) {
    /**
     * Devuelve la URL completa de la imagen del banner usando la BASE_URL activa.
     */
    fun getFullImageUrl(): String {
        if (imagenUrl.isEmpty()) return ""
        
        val cleanUrl = imagenUrl.trim()
        
        if (cleanUrl.startsWith("http") && !cleanUrl.contains("10.0.2.2") && !cleanUrl.contains("localhost") && !cleanUrl.contains("192.168.")) {
            return cleanUrl
        }
        
        val fileName = if (cleanUrl.contains("/")) cleanUrl.substringAfterLast("/") else cleanUrl
        val safeName = Uri.encode(fileName)
        return "${RetrofitClient.getEffectiveBaseUrl()}img/$safeName"
    }
}
