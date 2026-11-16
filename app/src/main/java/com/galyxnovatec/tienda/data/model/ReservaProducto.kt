package com.galyxnovatec.tienda.data.model

import com.google.gson.annotations.SerializedName

data class ReservaProducto(
    @SerializedName("id") val id: Int,
    @SerializedName("usuario_id") val usuarioId: Int,
    @SerializedName("producto_id") val productoId: Int,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("estado") val estado: String,
    @SerializedName("expira_at") val expiraAt: String,
    @SerializedName("pedido_id") val pedidoId: Int? = null,
    @SerializedName("producto_nombre") val productoNombre: String,
    @SerializedName("imagen_url") val imagenUrl: String? = null,
    @SerializedName("precio_normal") val precioNormal: Double = 0.0,
    @SerializedName("precio_oferta") val precioOferta: Double = 0.0
) {
    fun getImageUrl(): String? {
        if (imagenUrl.isNullOrBlank()) return null
        if (imagenUrl.startsWith("http")) return imagenUrl
        return "${com.galyxnovatec.tienda.data.network.RetrofitClient.getEffectiveBaseUrl()}img/${android.net.Uri.encode(imagenUrl)}"
    }
}
