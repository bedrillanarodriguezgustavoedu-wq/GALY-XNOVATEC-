package com.galyxnovatec.tienda.data.network

import android.os.Build
import com.galyxnovatec.tienda.BuildConfig
import com.galyxnovatec.tienda.data.model.*
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

/**
 * Interface que define los endpoints de la API de GALY XNOVATEC.
 */
interface GalyApiService {
    @Multipart
    @POST("upload_image.php")
    suspend fun uploadImage(@Part image: MultipartBody.Part): Map<String, Any>

    @GET("get_products.php")
    suspend fun getProductos(): List<Producto>

    @POST("add_product.php")
    suspend fun addProducto(@Body productoData: Map<String, @JvmSuppressWildcards Any>): Map<String, Any>

    @POST("add_category.php")
    suspend fun addCategoria(@Body data: Map<String, @JvmSuppressWildcards Any>): Map<String, Any>

    @POST("update_category.php")
    suspend fun updateCategoria(@Body data: Map<String, @JvmSuppressWildcards Any>): Map<String, Any>

    @POST("delete_category.php")
    suspend fun deleteCategoria(@Body data: Map<String, Int>): Map<String, Any>

    @GET("get_categories.php")
    suspend fun getCategorias(): List<Categoria>

    @GET("get_brands.php")
    suspend fun getMarcas(@Query("incluir_inactivas") incluirInactivas: Int = 0): List<MarcaCompartida>

    @POST("add_brand.php")
    suspend fun addMarca(@Body data: Map<String, @JvmSuppressWildcards Any>): Map<String, Any>

    @POST("update_brand.php")
    suspend fun updateMarca(@Body data: Map<String, @JvmSuppressWildcards Any>): Map<String, Any>

    @POST("deactivate_brand.php")
    suspend fun deactivateMarca(@Body data: Map<String, Int>): Map<String, Any>

    @GET("get_banners.php")
    suspend fun getBanners(): List<BannerResponse>

    @POST("add_banner.php")
    suspend fun addBanner(@Body data: Map<String, String>): Map<String, Any>

    @POST("delete_banner.php")
    suspend fun deleteBanner(@Body data: Map<String, Int>): Map<String, Any>

    @GET("get_cupones.php")
    suspend fun getCupones(): List<Cupon>

    @GET("get_contact_info.php")
    suspend fun getInformacionContacto(): InformacionContacto

    @POST("update_contact_info.php")
    suspend fun updateInformacionContacto(@Body data: Map<String, @JvmSuppressWildcards Any>): Map<String, Any>

    @GET("get_nosotros_info.php")
    suspend fun getInformacionNosotros(): InformacionNosotros

    @POST("update_nosotros_info.php")
    suspend fun updateInformacionNosotros(@Body data: Map<String, @JvmSuppressWildcards Any>): Map<String, Any>

    @GET("get_offers.php")
    suspend fun getOfertas(): List<Oferta>

    @POST("add_offer.php")
    suspend fun addOferta(@Body data: OfertaRequest): Map<String, Any>

    @POST("update_offer.php")
    suspend fun updateOferta(@Body data: OfertaRequest): Map<String, Any>

    @POST("delete_offer.php")
    suspend fun deleteOferta(@Body data: Map<String, Int>): Map<String, Any>

    @POST("add_coupon.php")
    suspend fun addCupon(@Body data: Map<String, @JvmSuppressWildcards Any>): Map<String, Any>

    @POST("delete_coupon.php")
    suspend fun deleteCupon(@Body data: Map<String, Int>): Map<String, Any>

    @POST("validate_cupon.php")
    suspend fun validarCupon(@Body data: Map<String, @JvmSuppressWildcards Any>): Map<String, Any>

    @POST("send_push.php")
    suspend fun sendPushNotification(@Body data: Map<String, String>): Map<String, Any>

    @POST("register_fcm_token.php")
    suspend fun registerFcmToken(@Body data: Map<String, @JvmSuppressWildcards Any>): Map<String, Any>

    @POST("create_ticket.php")
    suspend fun createTicket(@Body data: Map<String, @JvmSuppressWildcards Any>): Map<String, Any>

    @GET("get_user_tickets.php")
    suspend fun getUserTickets(@Query("usuario_id") usuarioId: Int): List<ServicioTecnico>

    @GET("get_all_tickets.php")
    suspend fun getAllTickets(): List<ServicioTecnico>

    @POST("update_ticket_status.php")
    suspend fun updateTicketStatus(@Body data: Map<String, @JvmSuppressWildcards Any>): Map<String, Any>

    @GET("get_servicios_tecnicos.php")
    suspend fun getServiciosTecnicos(): List<ServicioTecnico>

    @POST("login.php")
    suspend fun login(@Body credentials: Map<String, String>): AuthResponse

    @POST("register.php")
    suspend fun register(@Body userData: Map<String, String>): AuthResponse

    @POST("recover_password.php")
    suspend fun recoverPassword(@Body data: Map<String, String>): Map<String, Any>

    @POST("create_order.php")
    suspend fun createOrder(@Body orderData: OrderRequest): Map<String, Any>

    @POST("mercado_pago_checkout.php")
    suspend fun getMpPreference(@Body data: Map<String, @JvmSuppressWildcards Any>): Map<String, Any>

    @GET("get_user_orders.php")
    suspend fun getPedidos(@Query("usuario_id") usuarioId: Int): List<Pedido>

    @GET("get_all_orders.php")
    suspend fun getAllOrders(): List<Pedido>

    @GET("get_admin_stats.php")
    suspend fun getAdminStats(): Map<String, Any>

    @POST("create_admin_web_link.php")
    suspend fun createAdminWebLink(): Map<String, Any>

    @GET("get_sales_chart_data.php")
    suspend fun getSalesChartData(): List<SalesData>

    @GET("galy_ai_chat.php")
    suspend fun getAiResponsePhp(@Query("message") message: String): Map<String, Any>

    @POST("update_order_status.php")
    suspend fun updateOrderStatus(@Body data: Map<String, Any>): Map<String, Any>

    @GET("get_all_users.php")
    suspend fun getAllUsers(): List<User>

    @GET("get_comments.php")
    suspend fun getComments(@Query("producto_id") productoId: Int): List<Comment>

    @POST("add_comment.php")
    suspend fun addComment(@Body commentData: Map<String, @JvmSuppressWildcards Any>): Map<String, Any>

    @GET("get_user_addresses.php")
    suspend fun getDirecciones(@Query("usuario_id") usuarioId: Int): List<Direccion>

    @POST("add_address.php")
    suspend fun addDireccion(@Body addressData: Map<String, @JvmSuppressWildcards Any>): Map<String, Any>

    @POST("update_product_image.php")
    suspend fun updateProductoImagen(@Body data: Map<String, @JvmSuppressWildcards Any>): Map<String, Any>

    @POST("update_product.php")
    suspend fun updateProducto(@Body productoData: Map<String, @JvmSuppressWildcards Any>): Map<String, Any>

    @POST("update_user.php")
    suspend fun updateUsuario(@Body userData: Map<String, Any>): Map<String, Any>

    @POST("delete_comment.php")
    suspend fun deleteComment(@Body data: Map<String, Int>): Map<String, Any>

    @POST("update_comment.php")
    suspend fun updateComment(@Body data: Map<String, Any>): Map<String, Any>

    @POST("update_user_image.php")
    suspend fun updateUsuarioImagen(@Body data: Map<String, Any>): Map<String, Any>

    // ENDPOINTS DEL CARRITO
    @GET("get_cart.php")
    suspend fun getCart(@Query("usuario_id") usuarioId: Int): List<CartItemResponse>

    @POST("add_to_cart.php")
    suspend fun addToCart(@Body data: Map<String, Int>): Map<String, Any>

    @POST("update_cart_quantity.php")
    suspend fun updateCartQuantity(@Body data: Map<String, Int>): Map<String, Any>

    @POST("delete_from_cart.php")
    suspend fun deleteFromCart(@Body data: Map<String, Int>): Map<String, Any>

    @POST("clear_cart.php")
    suspend fun clearCart(@Body data: Map<String, Int>): Map<String, Any>

    @POST("create_reservation.php")
    suspend fun createReservation(@Body data: Map<String, Int>): Map<String, Any>

    @GET("get_user_reservations.php")
    suspend fun getUserReservations(@Query("usuario_id") usuarioId: Int): List<ReservaProducto>

    @POST("cancel_reservation.php")
    suspend fun cancelReservation(@Body data: Map<String, Int>): Map<String, Any>
}

interface GalyWebApiService {
    @Headers("Accept: application/json")
    @GET("api/mobile/payment-methods")
    suspend fun getPaymentMethods(): MobilePaymentMethodsResponse

    @Multipart
    @Headers("Accept: application/json")
    @POST("api/mobile/payment-receipts")
    suspend fun sendPaymentReceipt(
        @Part("id_token") idToken: RequestBody,
        @Part("pedido_id") orderId: RequestBody,
        @Part("metodo_pago") method: RequestBody,
        @Part("numero_operacion") operationNumber: RequestBody?,
        @Part receipt: MultipartBody.Part
    ): MobilePaymentReceiptResponse
}

data class MobilePaymentMethodsResponse(
    val success: Boolean,
    val methods: List<ManualPaymentMethod> = emptyList(),
    val message: String? = null
)

data class ManualPaymentMethod(
    val method: String,
    val number: String,
    val holder: String,
    val account: String? = null,
    val qr: String? = null
)

data class MobilePaymentReceiptResponse(
    val success: Boolean,
    val message: String
)

/** Modelo para recibir los items del carrito desde la API */
data class CartItemResponse(
    @SerializedName("producto_id") val productoId: Int,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("producto") val producto: Producto
)

data class Oferta(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("tipo_descuento") val tipoDescuento: String,
    @SerializedName("valor_descuento") val valorDescuento: Double,
    @SerializedName("fecha_inicio") val fechaInicio: String,
    @SerializedName("fecha_fin") val fechaFin: String,
    @SerializedName("activo") val activo: Boolean,
    @SerializedName("productos") val productos: List<Int> = emptyList(),
    @SerializedName("producto_nombres") val nombresProductos: List<String> = emptyList()
)

data class OfertaRequest(
    val nombre: String,
    @SerializedName("tipo_descuento") val tipoDescuento: String,
    @SerializedName("valor_descuento") val valorDescuento: Double,
    @SerializedName("fecha_inicio") val fechaInicio: String,
    @SerializedName("fecha_fin") val fechaFin: String,
    val activo: Boolean,
    val productos: List<Int>,
    val id: Int? = null
)

/** Modelos de Petición para evitar errores de wildcards en Retrofit */
data class OrderRequest(
    val usuario_id: Int,
    val total: Double,
    val items: List<OrderItemRequest>,
    val metodo_pago: String? = null,
    val cliente_nombre: String? = null,
    val cliente_telefono: String? = null,
    val cliente_email: String? = null,
    val cliente_dni: String? = null,
    val metodo_entrega: String? = null,
    val direccion: String? = null,
    val ciudad: String? = null,
    val distrito: String? = null,
    val referencia: String? = null,
    val maps_url: String? = null,
    val cupon: String? = null,
    val cupon_descuento: Double? = null
)

data class OrderItemRequest(
    val producto_id: Int,
    val cantidad: Int,
    val precio: Double
)

/** INTERFAZ PARA EL CEREBRO EN PYTHON (GALY AI) */
interface GalyAiApiService {
    @GET("recommend")
    suspend fun getAiRecommendation(@Query("query") query: String): List<Producto>
    
    @GET("chat")
    suspend fun getAiResponse(@Query("message") message: String): Map<String, Any>
}

/** Representa la respuesta de un banner desde la API. */
data class BannerResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("imagen_url") val imagenUrl: String,
    @SerializedName("titulo") val titulo: String? = null,
    @SerializedName("subtitulo") val subtitulo: String? = null,
    @SerializedName("link") val link: String? = null,
    @SerializedName("color_fondo") val colorFondo: String? = null
)

/** Representa un comentario de usuario sobre un producto. */
data class Comment(
    @SerializedName("id") val id: Int,
    @SerializedName("usuario_nombre") val userName: String,
    @SerializedName("comentario") val text: String,
    @SerializedName("estrellas") val stars: Int,
    @SerializedName("fecha") val date: String,
)

/** Representa los datos de ventas para gráficas */
data class SalesData(
    @SerializedName("dia") val dia: String,
    @SerializedName("total") val total: Double,
    @SerializedName("label") val label: String
)

/**
 * Cliente Retrofit configurado para conectar con el servidor local XAMPP y Python.
 * Detecta automáticamente si está corriendo en el Emulador de Android o en un Celular Físico por Wi-Fi.
 */
object RetrofitClient {
    private val WIFI_IP = BuildConfig.WIFI_IP

    val isEmulator: Boolean
        get() = (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || Build.PRODUCT.startsWith("sdk_gphone")
                || Build.DEVICE.contains("emu")
                || "google_sdk" == Build.PRODUCT)

    fun getEffectiveBaseUrl(): String {
        val configured = BuildConfig.BASE_URL
        return if (isEmulator) {
            configured.replace(
                Regex("^http://(?:192\\.168\\.\\d+\\.\\d+|10\\.\\d+\\.\\d+\\.\\d+|localhost|127\\.0\\.0\\.1)(?=/)"),
                "http://10.0.2.2"
            )
        } else {
            if (configured.contains("10.0.2.2")) configured.replace("10.0.2.2", WIFI_IP) else configured
        }
    }

    fun getEffectiveWebBaseUrl(): String {
        val configured = BuildConfig.WEB_BASE_URL
        return if (isEmulator) {
            configured.replace(
                Regex("^http://(?:192\\.168\\.\\d+\\.\\d+|10\\.\\d+\\.\\d+\\.\\d+|localhost|127\\.0\\.0\\.1)(?=[:/])"),
                "http://10.0.2.2"
            )
        } else {
            configured
                .replace("10.0.2.2", WIFI_IP)
                .replace("localhost", WIFI_IP)
                .replace("127.0.0.1", WIFI_IP)
        }
    }

    fun webAssetUrl(path: String): String {
        if (path.startsWith("https://") || path.startsWith("http://")) return path
        return getEffectiveWebBaseUrl().trimEnd('/') + "/" + path.trimStart('/')
    }

    fun getAdminWebBaseUrl(): String {
        return getEffectiveBaseUrl().trimEnd('/') + "/"
    }

    private val gson = GsonBuilder().setLenient().create()

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logger)
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .apply {
                    com.galyxnovatec.tienda.data.manager.AuthManager.accessToken
                        ?.takeIf { it.isNotBlank() }
                        ?.let { header("Authorization", "Bearer $it") }
                    com.galyxnovatec.tienda.data.manager.AuthManager.adminWebToken
                        ?.takeIf { it.isNotBlank() }
                        ?.let { header("X-Admin-Token", it) }
                }
                .header("Cache-Control", "no-cache, no-store, max-age=0")
                .header("Pragma", "no-cache")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(retryOnConnectionFailure = true) 
        .build()

    val instance: GalyApiService by lazy {
        Retrofit.Builder()
            .baseUrl(getEffectiveBaseUrl())
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(GalyApiService::class.java)
    }

    val webInstance: GalyWebApiService by lazy {
        Retrofit.Builder()
            .baseUrl(getEffectiveWebBaseUrl())
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(GalyWebApiService::class.java)
    }

    val aiInstance: GalyAiApiService by lazy {
        val aiBase = if (isEmulator) "http://10.0.2.2:8002/" else "http://$WIFI_IP:8002/"
        Retrofit.Builder()
            .baseUrl(aiBase)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(GalyAiApiService::class.java)
    }
}
