package com.galyxnovatec.tienda.data.model

import android.net.Uri
import com.galyxnovatec.tienda.BuildConfig
import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("email") val email: String,
    @SerializedName("telefono") val telefono: String? = null,
    @SerializedName("direccion") val direccion: String? = null,
    @SerializedName("imagen_url") val imagenUrl: String? = null,
    @SerializedName("es_admin") val esAdmin: Boolean = false,
    @SerializedName("puntos") val puntos: Int = 0
) {
    fun getFullImageUrl(): String? {
        if (imagenUrl.isNullOrEmpty()) return null
        if (imagenUrl.startsWith("http")) return imagenUrl
        return "${BuildConfig.BASE_URL}img/${Uri.encode(imagenUrl)}"
    }

    fun getNivelNombre(): String = when {
        puntos >= 1500 -> "ORO"
        puntos >= 500 -> "PLATA"
        else -> "BRONCE"
    }

    fun getDescuentoNivel(): Double = when {
        puntos >= 1500 -> 0.05 // 5%
        puntos >= 500 -> 0.02 // 2%
        else -> 0.0
    }
}

data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: User? = null
)