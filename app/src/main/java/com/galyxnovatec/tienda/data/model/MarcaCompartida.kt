package com.galyxnovatec.tienda.data.model

import android.net.Uri
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.google.gson.annotations.SerializedName

data class MarcaCompartida(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("imagen") val imagen: String? = null,
    @SerializedName("activo") val activo: Boolean,
    @SerializedName("productos_count") val productosCount: Int = 0
) {
    fun getFullImageUrl(): String? {
        val path = imagen?.trim()?.trimStart('/')?.takeIf { it.isNotEmpty() } ?: return null
        if (path.startsWith("http://") || path.startsWith("https://")) return path

        return "${RetrofitClient.getEffectiveBaseUrl()}img/${Uri.encode(path, "/")}"
    }
}
