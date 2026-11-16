package com.galyxnovatec.tienda.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.manager.LocalCacheManager
import com.galyxnovatec.tienda.data.model.*
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.coroutines.cancellation.CancellationException
import org.json.JSONObject
import retrofit2.HttpException

/**
 * REPOSITORIO MAESTRO - GALY XNOVATEC
 */
object ProductRepository {

    val productos = mutableStateListOf<Producto>()
    val listaCategoriasBase = mutableStateListOf<Categoria>()
    val marcas = mutableStateListOf<MarcaCompartida>()
    val banners = mutableStateListOf<Banner>()
    val servicios = mutableStateListOf<ServicioTecnico>()
    var informacionContacto by mutableStateOf<InformacionContacto?>(null)
    var informacionNosotros by mutableStateOf<InformacionNosotros?>(null)
    
    var isLoading by mutableStateOf(false)
    var ultimoError: String? = null
    private const val PREFS_NAME = "galy_cache"
    private const val KEY_PRODUCTS = "cached_products"

    init {
        // Inicialización limpia sin items dummy ficticios
    }

    fun cargarCache(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val cachedProds = LocalCacheManager.obtenerProductos(context)
            val cachedCats = LocalCacheManager.obtenerCategorias(context)
            val cachedBanners = LocalCacheManager.obtenerBanners(context)
            val cachedBrands = LocalCacheManager.obtenerMarcas(context)

            withContext(Dispatchers.Main) {
                if (cachedProds.isNotEmpty() && productos.isEmpty()) {
                    productos.clear()
                    productos.addAll(cachedProds)
                }
                if (cachedCats.isNotEmpty() && listaCategoriasBase.isEmpty()) {
                    listaCategoriasBase.clear()
                    listaCategoriasBase.addAll(cachedCats)
                }
                if (cachedBanners.isNotEmpty() && banners.isEmpty()) {
                    banners.clear()
                    banners.addAll(cachedBanners)
                }
                if (cachedBrands.isNotEmpty() && marcas.isEmpty()) {
                    marcas.clear()
                    marcas.addAll(cachedBrands)
                }
            }
        }
    }

    private fun guardarCache(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            com.galyxnovatec.tienda.data.manager.LocalCacheManager.guardarProductos(context, productos.toList())
            LocalCacheManager.guardarCategorias(context, listaCategoriasBase.toList())
            LocalCacheManager.guardarBanners(context, banners.toList())
            LocalCacheManager.guardarMarcas(context, marcas.toList())
        }
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
        categoria: String,
        precioNormal: Double,
        precioOferta: Double,
        precioCompra: Double,
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
                "categoria" to categoria,
                "precio_normal" to precioNormal,
                "precio_oferta" to precioOferta,
                "precio_compra" to precioCompra,
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

    private fun categoryErrorMessage(exception: Exception, fallback: String): String {
        if (exception is HttpException) {
            val body = exception.response()?.errorBody()?.string()
            if (!body.isNullOrBlank()) {
                val message = runCatching { JSONObject(body).optString("message") }.getOrNull()
                if (!message.isNullOrBlank()) return message
            }
        }

        return exception.message ?: fallback
    }

    suspend fun sincronizarCategorias(): Boolean {
        return try {
            val response = RetrofitClient.instance.getCategorias()
            withContext(Dispatchers.Main) {
                listaCategoriasBase.clear()
                listaCategoriasBase.addAll(response)
            }
            true
        } catch (e: Exception) {
            Log.e("GALY", "Error sincronizando categorías compartidas: ${e.message}", e)
            false
        }
    }

    suspend fun guardarCategoria(id: Int?, nombre: String): String? {
        return try {
            val data = mutableMapOf<String, Any>("nombre" to nombre)
            val response = if (id == null) {
                RetrofitClient.instance.addCategoria(data)
            } else {
                data["id"] = id
                RetrofitClient.instance.updateCategoria(data)
            }
            if (response["success"] != true) {
                response["message"]?.toString() ?: "No se pudo guardar la categoría."
            } else if (sincronizarCategorias()) {
                null
            } else {
                "La categoría se guardó, pero no se pudo actualizar la lista."
            }
        } catch (e: Exception) {
            Log.e("GALY", "Error guardando categoría compartida: ${e.message}", e)
            categoryErrorMessage(e, "No se pudo conectar con el catálogo de categorías.")
        }
    }

    suspend fun eliminarCategoria(id: Int): String? {
        return try {
            val res = RetrofitClient.instance.deleteCategoria(mapOf("id" to id))
            if (res["success"] != true) {
                res["message"]?.toString() ?: "No se pudo eliminar la categoría."
            } else if (sincronizarCategorias()) {
                null
            } else {
                "La categoría se eliminó, pero no se pudo actualizar la lista."
            }
        } catch (e: Exception) {
            Log.e("GALY", "Error eliminando categoría compartida: ${e.message}", e)
            categoryErrorMessage(e, "No se pudo conectar con el catálogo de categorías.")
        }
    }

    suspend fun sincronizarMarcas(incluirInactivas: Boolean = true): Boolean {
        return try {
            val res = RetrofitClient.instance.getMarcas(if (incluirInactivas) 1 else 0)
            withContext(Dispatchers.Main) {
                marcas.clear()
                marcas.addAll(res)
            }
            true
        } catch (e: Exception) {
            Log.e("GALY", "Error sincronizando marcas compartidas: ${e.message}", e)
            false
        }
    }

    suspend fun guardarMarca(id: Int?, nombre: String, activo: Boolean = true): String? {
        return try {
            val data = mutableMapOf<String, Any>("nombre" to nombre, "activo" to activo)
            val response = if (id == null) {
                RetrofitClient.instance.addMarca(data)
            } else {
                data["id"] = id
                RetrofitClient.instance.updateMarca(data)
            }
            if (response["success"] != true) {
                response["message"]?.toString() ?: "No se pudo guardar la marca."
            } else if (sincronizarMarcas()) {
                null
            } else {
                "La marca se guardó, pero no se pudo actualizar la lista."
            }
        } catch (e: Exception) {
            Log.e("GALY", "Error guardando marca compartida: ${e.message}", e)
            e.message ?: "No se pudo conectar con el catálogo de marcas."
        }
    }

    suspend fun desactivarMarca(id: Int): String? {
        return try {
            val response = RetrofitClient.instance.deactivateMarca(mapOf("id" to id))
            if (response["success"] != true) {
                response["message"]?.toString() ?: "No se pudo desactivar la marca."
            } else if (sincronizarMarcas()) {
                null
            } else {
                "La marca se desactivó, pero no se pudo actualizar la lista."
            }
        } catch (e: Exception) {
            Log.e("GALY", "Error desactivando marca compartida: ${e.message}", e)
            e.message ?: "No se pudo conectar con el catálogo de marcas."
        }
    }

    suspend fun sincronizarInformacionContacto(): Boolean {
        return try {
            val response = RetrofitClient.instance.getInformacionContacto()
            withContext(Dispatchers.Main) {
                informacionContacto = response
            }
            true
        } catch (e: Exception) {
            Log.e("GALY", "Error sincronizando información de contacto: ${e.message}", e)
            false
        }
    }

    suspend fun guardarInformacionContacto(data: Map<String, Any>): String? {
        return try {
            val response = RetrofitClient.instance.updateInformacionContacto(data)
            if (response["success"] != true) {
                response["message"]?.toString() ?: "No se pudo guardar la información de contacto."
            } else if (sincronizarInformacionContacto()) {
                null
            } else {
                "Los datos se guardaron, pero no se pudo actualizar la información mostrada."
            }
        } catch (e: Exception) {
            Log.e("GALY", "Error actualizando información de contacto: ${e.message}", e)
            categoryErrorMessage(e, "No se pudo conectar con la información de contacto compartida.")
        }
    }

    suspend fun sincronizarInformacionNosotros(): Boolean {
        return try {
            val response = RetrofitClient.instance.getInformacionNosotros()
            withContext(Dispatchers.Main) {
                informacionNosotros = response
            }
            true
        } catch (e: Exception) {
            Log.e("GALY", "Error sincronizando información de Nosotros: ${e.message}", e)
            false
        }
    }

    suspend fun guardarInformacionNosotros(data: Map<String, Any>): String? {
        return try {
            val response = RetrofitClient.instance.updateInformacionNosotros(data)
            if (response["success"] != true) {
                response["message"]?.toString() ?: "No se pudo guardar la información de Nosotros."
            } else if (sincronizarInformacionNosotros()) {
                null
            } else {
                "Los datos se guardaron, pero no se pudo actualizar la información mostrada."
            }
        } catch (e: Exception) {
            Log.e("GALY", "Error actualizando información de Nosotros: ${e.message}", e)
            categoryErrorMessage(e, "No se pudo conectar con la información de Nosotros compartida.")
        }
    }

    suspend fun obtenerReservasUsuario(userId: Int): List<ReservaProducto> {
        return RetrofitClient.instance.getUserReservations(userId)
    }

    suspend fun crearReserva(userId: Int, productId: Int, quantity: Int): String? {
        return try {
            val response = RetrofitClient.instance.createReservation(
                mapOf("usuario_id" to userId, "producto_id" to productId, "cantidad" to quantity)
            )
            if (response["success"] == true) null
            else response["message"]?.toString() ?: "No se pudo reservar el producto."
        } catch (e: Exception) {
            Log.e("GALY", "Error creando reserva de producto: ${e.message}", e)
            categoryErrorMessage(e, "No se pudo reservar el producto. Revisa tu conexión.")
        }
    }

    suspend fun cancelarReserva(userId: Int, reservationId: Int): String? {
        return try {
            val response = RetrofitClient.instance.cancelReservation(
                mapOf("usuario_id" to userId, "reserva_id" to reservationId)
            )
            if (response["success"] == true) null
            else response["message"]?.toString() ?: "No se pudo cancelar la reserva."
        } catch (e: Exception) {
            Log.e("GALY", "Error cancelando reserva de producto: ${e.message}", e)
            categoryErrorMessage(e, "No se pudo cancelar la reserva. Revisa tu conexión.")
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

    private val syncMutex = Mutex()

    /** Sincroniza todos los datos desde el servidor XAMPP de forma paralela */
    suspend fun sincronizarTodo(context: Context? = null): Boolean {
        if (!syncMutex.tryLock()) return false
        isLoading = true
        ultimoError = null
        var exitoGlobal = false
        
        return try {
            coroutineScope {
                // Cargamos todo por separado para que nada se bloquee
                launch {
                    try {
                        val res = RetrofitClient.instance.getProductos()
                        val validos = res.filter { it.id > 0 }
                        if (validos.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                productos.clear()
                                productos.addAll(validos)
                            }
                            exitoGlobal = true
                            context?.let { guardarCache(it) }
                            Log.d("GALY_SYNC", "✅ Productos: ${validos.size} cargados.")
                        }
                    } catch (e: Exception) { Log.e("GALY_SYNC", "❌ Error P: ${e.message}") }
                }
                
                launch {
                    try {
                        sincronizarCategorias()
                    } catch (e: Exception) {
                        Log.e("GALY_SYNC", "Error cargando categorías compartidas: ${e.message}", e)
                    }
                }

                launch {
                    try {
                        val res = RetrofitClient.instance.getMarcas(incluirInactivas = 1)
                        withContext(Dispatchers.Main) {
                            marcas.clear()
                            marcas.addAll(res)
                        }
                    } catch (e: Exception) {
                        Log.e("GALY_SYNC", "Error cargando marcas compartidas: ${e.message}", e)
                    }
                }

                launch {
                    try {
                        val res = RetrofitClient.instance.getBanners()
                        val listBanners = res.map { 
                            Banner(
                                id = it.id,
                                imagenUrl = it.imagenUrl,
                                titulo = it.titulo,
                                subtitulo = it.subtitulo ?: it.link,
                                colorFondo = it.colorFondo
                            )
                        }
                        withContext(Dispatchers.Main) {
                            banners.clear()
                            banners.addAll(listBanners)
                        }
                    } catch (e: Exception) { }
                }

                launch {
                    if (!sincronizarInformacionContacto()) {
                        Log.e("GALY_SYNC", "No se pudo cargar la información compartida de contacto.")
                    }
                }
            }
            // Antes se hacía refreshImageCacheVersion() aquí, lo que cambiaba el ?v= de TODAS las
            // imágenes en cada sincronización (y se sincroniza en cada onResume): la app volvía a
            // descargar los 164 productos una y otra vez y la caché de disco no daba abasto.
            exitoGlobal
        } catch (e: Exception) {
            ultimoError = e.message
            false
        } finally {
            isLoading = false
            try { syncMutex.unlock() } catch (e: Exception) {}
        }
    }

    fun obtenerNovedades(): List<Producto> = productos.filter { it.etiquetaEspecial?.contains("NUEVO", ignoreCase = true) == true }
    fun obtenerPorId(id: Int): Producto? = productos.find { it.id == id }
    fun obtenerPorCategoria(nombre: String): List<Producto> = productos.filter { it.categoria.equals(nombre, ignoreCase = true) }
    
    fun buscar(texto: String): List<Producto> {
        val busqueda = texto.trim().lowercase()
        if (busqueda.isEmpty()) return productos
        
        return productos.filter { 
            it.nombre.lowercase().contains(busqueda) || 
            it.marca.lowercase().contains(busqueda) ||
            it.categoria.lowercase().contains(busqueda) ||
            it.descripcion?.lowercase()?.contains(busqueda) == true ||
            it.especificaciones?.lowercase()?.contains(busqueda) == true ||
            it.variantes?.lowercase()?.contains(busqueda) == true
        }
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

    /** Obtiene productos filtrados por búsqueda, categoría, rango de precio, ordenamiento y disponibilidad */
    fun obtenerProductosFiltrados(
        busqueda: String? = null,
        categoria: String? = null,
        precioMin: Double? = null,
        precioMax: Double? = null,
        soloEnStock: Boolean = false,
        orden: String = "DEFECTO"
    ): List<Producto> {
        var resultado = productos.filter { it.id > 0 }

        if (!busqueda.isNullOrBlank()) {
            val q = busqueda.lowercase().trim()
            resultado = resultado.filter {
                it.nombre.lowercase().contains(q) ||
                it.categoria.lowercase().contains(q) ||
                it.marca.lowercase().contains(q) ||
                it.descripcion?.lowercase()?.contains(q) == true
            }
        }

        if (!categoria.isNullOrBlank() && !categoria.equals("TODOS", ignoreCase = true) && !categoria.equals("Todas", ignoreCase = true)) {
            resultado = resultado.filter { it.categoria.equals(categoria, ignoreCase = true) }
        }

        if (precioMin != null) {
            resultado = resultado.filter { (if (it.precioOferta > 0) it.precioOferta else it.precioNormal) >= precioMin }
        }

        if (precioMax != null) {
            resultado = resultado.filter { (if (it.precioOferta > 0) it.precioOferta else it.precioNormal) <= precioMax }
        }

        if (soloEnStock) {
            resultado = resultado.filter { it.stock > 0 }
        }

        return when (orden) {
            "PRECIO_MENOR" -> resultado.sortedBy { if (it.precioOferta > 0) it.precioOferta else it.precioNormal }
            "PRECIO_MAYOR" -> resultado.sortedByDescending { if (it.precioOferta > 0) it.precioOferta else it.precioNormal }
            "NOMBRE" -> resultado.sortedBy { it.nombre }
            "OFERTAS" -> resultado.sortedByDescending { if (it.precioNormal > 0 && it.precioOferta > 0) ((it.precioNormal - it.precioOferta) / it.precioNormal) else 0.0 }
            else -> resultado
        }
    }

    fun obtenerUltimoError(): String? = ultimoError
}
