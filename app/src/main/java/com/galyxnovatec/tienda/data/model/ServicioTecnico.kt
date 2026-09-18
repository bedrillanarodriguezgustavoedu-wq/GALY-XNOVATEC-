package com.galyxnovatec.tienda.data.model

import com.google.gson.annotations.SerializedName

data class ServicioTecnico(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre_servicio") val nombre: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("precio_base") val precio: Double,
    
    // Campos para Tickets (Soporte)
    @SerializedName("equipo_modelo") val equipoModelo: String? = null,
    @SerializedName("descripcion_falla") val falla: String? = null,
    @SerializedName("estado") val estado: String? = "Recibido",
    @SerializedName("costo_estimado") val costoEstimado: Double? = 0.0,
    @SerializedName("fecha_ingreso") val fecha: String? = null,
    @SerializedName("cliente_nombre") val clienteNombre: String? = null,
    
    @SerializedName("imagen_ingreso") val imagenIngreso: String? = null,
    @SerializedName("imagen_listo") val imagenListo: String? = null
)
