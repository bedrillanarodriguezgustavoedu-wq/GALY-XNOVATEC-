package com.galyxnovatec.tienda.data.model

import com.google.gson.annotations.SerializedName

data class InformacionContacto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("nombre_empresa") val nombreEmpresa: String = "",
    @SerializedName("direccion") val direccion: String = "",
    @SerializedName("pais") val pais: String = "",
    @SerializedName("departamento") val departamento: String = "",
    @SerializedName("provincia") val provincia: String = "",
    @SerializedName("telefono_principal") val telefonoPrincipal: String = "",
    @SerializedName("telefono_secundario") val telefonoSecundario: String? = null,
    @SerializedName("whatsapp") val whatsapp: String? = null,
    @SerializedName("email") val email: String = "",
    @SerializedName("horario_atencion") val horarioAtencion: String = "",
    @SerializedName("google_maps_link") val googleMapsLink: String? = null,
    @SerializedName("facebook_link") val facebookLink: String? = null,
    @SerializedName("instagram_link") val instagramLink: String? = null,
    @SerializedName("youtube_link") val youtubeLink: String? = null,
    @SerializedName("twitter_link") val twitterLink: String? = null,
    @SerializedName("mensaje_bienvenida") val mensajeBienvenida: String? = null,
    @SerializedName("activo") val activo: Boolean = true
)
