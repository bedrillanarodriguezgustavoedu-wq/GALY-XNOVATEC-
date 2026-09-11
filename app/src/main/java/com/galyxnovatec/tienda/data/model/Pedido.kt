package com.galyxnovatec.tienda.data.model

import com.google.gson.annotations.SerializedName

data class Pedido(
    @SerializedName("id") val id: Int,
    @SerializedName("usuario_id") val usuarioId: Int,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("total") val total: Double,
    @SerializedName("estado") val estado: String,
    @SerializedName("metodo_pago") val metodoPago: String? = null,
    @SerializedName("cliente_nombre") val clienteNombre: String? = null,
    @SerializedName("cliente_telefono") val clienteTelefono: String? = null,
    @SerializedName("direccion") val direccion: String? = null,
    @SerializedName("ciudad") val ciudad: String? = null,
    @SerializedName("distrito") val distrito: String? = null,
    @SerializedName("referencia") val referencia: String? = null,
    @SerializedName("maps_url") val mapsUrl: String? = null,
    @SerializedName("productos") val productos: List<PedidoProducto>? = null
)

data class PedidoProducto(
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("precio") val precio: Double,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("imagen_url") val imagenUrl: String? = null
)
