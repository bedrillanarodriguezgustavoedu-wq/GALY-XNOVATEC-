package com.galyxnovatec.tienda.data.model

import android.net.Uri
import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("email") val email: String,
    @SerializedName("telefono") val telefono: String? = null,
    @SerializedName("direccion") val direccion: String? = null,
    @SerializedName("imagen_url") val imagenUrl: String? = null,
    @SerializedName("es_admin") val esAdmin: Boolean = false
) {
    fun getFullImageUrl(): String? {
        if (imagenUrl.isNullOrEmpty()) return null
        if (imagenUrl.startsWith("http")) return imagenUrl
        return "http://10.0.2.2/galy_api/img/${Uri.encode(imagenUrl)}"
    }
}

data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: User? = null
)