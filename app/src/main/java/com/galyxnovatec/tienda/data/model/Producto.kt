package com.galyxnovatec.tienda.data.model

import android.net.Uri
import com.galyxnovatec.tienda.R
import com.google.gson.annotations.SerializedName

data class Producto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("marca")
    val marca: String,
    @SerializedName("categoria_nombre")
    val categoria: String,
    @SerializedName("precio_oferta")
    val precioOferta: Double,
    @SerializedName("precio_normal")
    val precioNormal: Double,
    @SerializedName("imagen_url")
    val imagenUrl: String? = null,
    @SerializedName("descripcion")
    val descripcion: String? = null,
    @SerializedName("disponible")
    val disponible: Boolean = true,
    @SerializedName("tiene_envio_gratis")
    val tieneEnvioGratis: Boolean = false,
    @SerializedName("etiqueta_especial")
    val etiquetaEspecial: String? = null,
    @SerializedName("descuento")
    val descuento: Int? = null,
    @SerializedName("calificacion")
    val calificacion: Double = 4.5,
    @SerializedName("num_revisiones")
    val numRevisiones: Int = 120,
    @SerializedName("especificaciones")
    val especificaciones: String? = null,
    @SerializedName("variantes")
    val variantes: String? = null,
    @SerializedName("imagenes_secundarias")
    val imagenesSecundarias: String? = null,
    @SerializedName("stock")
    val stock: Int = 10,
    // Fallback resource ID
    val imagenRes: Int = 0
) {
    /**
     * Devuelve la URL completa de la imagen principal, manejando errores y enlaces duplicados.
     */
    fun getFullImageUrl(): Any {
        if (imagenUrl.isNullOrEmpty()) {
            return if (imagenRes != 0) imagenRes else R.drawable.logo_2
        }
        
        val cleanUrl = imagenUrl.trim()
        
        // Si el enlace contiene nuestra propia IP o ruta duplicada, extraemos solo el nombre del archivo
        if (cleanUrl.contains("galy_api/img/")) {
            val fileName = cleanUrl.substringAfterLast("/")
            return "http://10.0.2.2/galy_api/img/${Uri.encode(fileName)}"
        }

        // Si ya es un enlace externo de internet, lo devolvemos tal cual
        if (cleanUrl.startsWith("http")) return cleanUrl
        
        // Si es solo el nombre del archivo, lo codificamos y añadimos la ruta local
        val safeName = Uri.encode(cleanUrl)
        return "http://10.0.2.2/galy_api/img/$safeName"
    }

    /**
     * Devuelve la lista completa de URLs de imágenes (Principal + Secundarias).
     */
    fun getAllImageUrls(): List<Any> {
        val list = mutableListOf<Any>()
        list.add(getFullImageUrl())
        
        // Imágenes secundarias (vienen separadas por coma en la BD)
        imagenesSecundarias?.split(",")?.forEach { img ->
            val cleanImg = img.trim()
            if (cleanImg.isNotEmpty()) {
                if (cleanImg.startsWith("http")) {
                    list.add(cleanImg)
                } else {
                    val safeName = Uri.encode(cleanImg)
                    list.add("http://10.0.2.2/galy_api/img/$safeName")
                }
            }
        }
        return list.distinct()
    }

    /**
     * Convierte el texto de variantes de la BD en una lista para la UI.
     */
    fun getVariantesList(): List<String> {
        return variantes?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }

    /**
     * Convierte el texto de especificaciones en una lista.
     */
    fun getEspecificacionesList(): List<String> {
        return especificaciones?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }

    fun getPrecioOfertaFormateado(): String = "S/ ${precioOferta.toInt()}"
    fun getPrecioNormalFormateado(): String = "P. Normal: S/ ${precioNormal.toInt()}"
    fun getPrecioAnteriorFormateado(): String = "P. Oferta: S/ ${(precioOferta + 10).toInt()}"
}
