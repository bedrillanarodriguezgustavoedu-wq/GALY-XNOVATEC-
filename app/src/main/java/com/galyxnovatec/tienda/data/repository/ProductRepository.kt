package com.galyxnovatec.tienda.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.model.*
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

/**
 * REPOSITORIO MAESTRO - GALY XNOVATEC
 */
object ProductRepository {

    val productos = mutableStateListOf<Producto>()
    val listaCategoriasBase = mutableStateListOf<Categoria>()
    val banners = mutableStateListOf<Banner>()
    val servicios = mutableStateListOf<ServicioTecnico>()
    
    var ultimoError: String? = null
    private const val PREFS_NAME = "galy_cache"
    private const val KEY_PRODUCTS = "cached_products"

    init {
        // Producto temporal de carga
        productos.add(Producto(id = -1, nombre = "Cargando...", marca = "GALY", categoria = "", precioOferta = 0.0, precioNormal = 0.0, imagenRes = R.drawable.ic_launcher_background, descripcion = ""))
    }

    fun cargarCache(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_PRODUCTS, null)
        if (json != null) {
            try {
                val cached = Gson().fromJson(json, Array<Producto>::class.java).toList()
                if (productos.any { it.id == -1 }) productos.clear()
                productos.addAll(cached)
            } catch (e: Exception) { }
        }
    }

    private fun guardarCache(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(productos.filter { it.id != -1 })
        prefs.edit().putString(KEY_PRODUCTS, json).apply()
    }

    /** Convierte una Uri de Android a un archivo temporal para subirlo */
    fun uriToFile(context: Context, uri: Uri): File? {
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            Log.e("GALY", "Error al convertir Uri a File: ${e.message}")
            null
        }
    }

    /** Sube una imagen al servidor XAMPP */
    suspend fun subirImagen(archivo: File): String? {
        return try {
            val requestFile = archivo.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", archivo.name, requestFile)
            val res = RetrofitClient.instance.uploadImage(body)
            if (res["success"] == true) res["filename"] as String else null
        } catch (e: Exception) {
            Log.e("GALY", "Error subiendo imagen: ${e.message}")
            null
        }
    }

    /** Actualiza la imagen de un producto en la base de datos */
    suspend fun actualizarImagenProducto(id: Int, nuevoNombreImagen: String): Boolean {
        return try {
            val res = RetrofitClient.instance.updateProductoImagen(
                mapOf("id" to id, "imagen_url" to nuevoNombreImagen)
            )
            res["success"] == true
        } catch (e: Exception) {
            Log.e("GALY", "Error actualizando BD: ${e.message}")
            false
        }
    }

    /** Actualiza toda la información de un producto */
    suspend fun actualizarProducto(
        id: Int,
        nombre: String,
        marca: String,
        precioNormal: Double,
        precioOferta: Double,
        descripcion: String,
        especificaciones: String,
        stock: Int,
        imagenUrl: String? = null,
        imagenesSecundarias: List<String>? = null
    ): Boolean {
        return try {
            val data = mutableMapOf<String, Any>(
                "id" to id,
                "nombre" to nombre,
                "marca" to marca,
                "precio_normal" to precioNormal,
                "precio_oferta" to precioOferta,
                "descripcion" to descripcion,
                "especificaciones" to especificaciones,
                "stock" to stock
            )
            if (imagenUrl != null) data["imagen_url"] = imagenUrl
            if (imagenesSecundarias != null) data["imagenes_secundarias"] = imagenesSecundarias
            
            val res = RetrofitClient.instance.updateProducto(data)
            res["success"] == true
        } catch (e: Exception) {
            Log.e("GALY", "Error actualizando producto completo: ${e.message}")
            false
        }
    }

    suspend fun agregarCategoria(nombre: String): Boolean {
        return try {
            val res = RetrofitClient.instance.addCategoria(mapOf("nombre" to nombre))
            if (res["success"] == true) {
                sincronizarTodo()
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun eliminarCategoria(id: Int): Boolean {
        return try {
            val res = RetrofitClient.instance.deleteCategoria(mapOf("id" to id))
            if (res["success"] == true) {
                sincronizarTodo()
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun agregarBanner(imagenUrl: String, titulo: String, subtitulo: String): Boolean {
        return try {
            val res = RetrofitClient.instance.addBanner(
                mapOf(
                    "imagen_url" to imagenUrl,
                    "titulo" to titulo,
                    "subtitulo" to subtitulo
                )
            )
            if (res["success"] == true) {
                sincronizarTodo()
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun eliminarBanner(id: Int): Boolean {
        return try {
            val res = RetrofitClient.instance.deleteBanner(mapOf("id" to id))
            if (res["success"] == true) {
                sincronizarTodo()
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    /** Sincroniza todos los datos desde el servidor XAMPP de forma paralela */
    suspend fun sincronizarTodo(context: Context? = null): Boolean {
        ultimoError = null
        var exitoGlobal = false
        
        return try {
            coroutineScope {
                // Cargamos todo por separado para que nada se bloquee
                launch {
                    try {
                        val res = RetrofitClient.instance.getProductos()
                        if (res.isNotEmpty()) {
                            productos.clear()
                            productos.addAll(res)
                            exitoGlobal = true
                            context?.let { guardarCache(it) }
                            Log.d("GALY_SYNC", "✅ Productos: ${res.size} cargados.")
                        }
                    } catch (e: Exception) { Log.e("GALY_SYNC", "❌ Error P: ${e.message}") }
                }
                
                launch {
                    try {
                        val res = RetrofitClient.instance.getCategorias()
                        listaCategoriasBase.clear()
                        listaCategoriasBase.addAll(res)
                    } catch (e: Exception) { }
                }

                launch {
                    try {
                        val res = RetrofitClient.instance.getBanners()
                        banners.clear()
                        banners.addAll(res.map { 
                            Banner(
                                id = it.id,
                                imagenUrl = it.imagenUrl,
                                titulo = it.titulo,
                                subtitulo = it.subtitulo ?: it.link,
                                colorFondo = it.colorFondo
                            )
                        })
                    } catch (e: Exception) { }
                }
            }
            exitoGlobal
        } catch (e: Exception) {
            ultimoError = e.message
            false
        }
    }

    fun obtenerNovedades(): List<Producto> = productos.filter { it.etiquetaEspecial?.contains("NUEVO", ignoreCase = true) == true }
    fun obtenerPorId(id: Int): Producto? = productos.find { it.id == id }
    fun obtenerPorCategoria(nombre: String): List<Producto> = productos.filter { it.categoria.equals(nombre, ignoreCase = true) }
    
    fun buscar(texto: String): List<Producto> {
        val busqueda = texto.trim().lowercase()
        return if (busqueda.isEmpty()) productos
        else productos.filter { it.nombre.lowercase().contains(busqueda) || it.marca.lowercase().contains(busqueda) }
    }

    /** Obtiene los nombres de las categorías con fallback si no hay conexión */
    fun obtenerCategorias(): List<String> {
        val list = mutableListOf<String>()
        
        // 1. De la tabla de categorías
        if (listaCategoriasBase.isNotEmpty()) {
            list.addAll(listaCategoriasBase.map { it.nombre })
        }
        
        // 2. De los productos
        val deProductos = productos.filter { it.id != -1 }.map { it.categoria }.distinct().filter { it.isNotEmpty() }
        list.addAll(deProductos)

        // 3. Fallback final
        if (list.isEmpty()) {
            list.addAll(listOf("Laptops", "Mouses", "Audífonos", "Monitores", "Gaming"))
        }

        return list.distinct().sorted()
    }

    fun obtenerServicios(): List<ServicioTecnico> = servicios
    fun obtenerTodosLosProductos(): List<Producto> = productos
    fun obtenerUltimoError(): String? = ultimoError
}
