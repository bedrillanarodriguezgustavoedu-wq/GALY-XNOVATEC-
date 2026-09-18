package com.galyxnovatec.tienda.data.network

import com.galyxnovatec.tienda.BuildConfig
import com.galyxnovatec.tienda.data.model.*
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
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
    suspend fun addCategoria(@Body data: Map<String, String>): Map<String, Any>

    @POST("delete_category.php")
    suspend fun deleteCategoria(@Body data: Map<String, Int>): Map<String, Any>

    @GET("get_categories.php")
    suspend fun getCategorias(): List<Categoria>

    @GET("get_banners.php")
    suspend fun getBanners(): List<BannerResponse>

    @POST("add_banner.php")
    suspend fun addBanner(@Body data: Map<String, String>): Map<String, Any>

    @POST("delete_banner.php")
    suspend fun deleteBanner(@Body data: Map<String, Int>): Map<String, Any>

    @GET("get_cupones.php")
    suspend fun getCupones(): List<Cupon>

    @POST("add_coupon.php")
    suspend fun addCupon(@Body data: Map<String, @JvmSuppressWildcards Any>): Map<String, Any>

    @POST("delete_coupon.php")
    suspend fun deleteCupon(@Body data: Map<String, Int>): Map<String, Any>

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

    @POST("create_order.php")
    suspend fun createOrder(@Body orderData: OrderRequest): Map<String, Any>

    @POST("mercado_pago_checkout.php")
    suspend fun getMpPreference(@Body data: Map<String, Any>): Map<String, Any>

    @GET("get_user_orders.php")
    suspend fun getPedidos(@Query("usuario_id") usuarioId: Int): List<Pedido>

    @GET("get_all_orders.php")
    suspend fun getAllOrders(): List<Pedido>

    @GET("get_admin_stats.php")
    suspend fun getAdminStats(): Map<String, Any>

    @GET("get_sales_chart_data.php")
    suspend fun getSalesChartData(): List<SalesData>

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
}

/** Modelo para recibir los items del carrito desde la API */
data class CartItemResponse(
    @SerializedName("producto_id") val productoId: Int,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("producto") val producto: Producto
)

/** Modelos de Petición para evitar errores de wildcards en Retrofit */
data class OrderRequest(
    val usuario_id: Int,
    val total: Double,
    val items: List<OrderItemRequest>,
    val metodo_pago: String? = null,
    val cliente_telefono: String? = null,
    val cliente_email: String? = null,
    val cliente_dni: String? = null,
    val metodo_entrega: String? = null,
    val direccion: String? = null,
    val ciudad: String? = null,
    val distrito: String? = null,
    val referencia: String? = null,
    val maps_url: String? = null
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
 */
object RetrofitClient {
    private val BASE_URL = BuildConfig.BASE_URL
    private const val AI_URL = "http://192.168.1.67:8000/" 

    private val gson = GsonBuilder().setLenient().create()

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC // Reducimos de BODY a BASIC
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logger)
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("X-GALY-KEY", "galy_master_key_2026")
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
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(GalyApiService::class.java)
    }

    val aiInstance: GalyAiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AI_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(GalyAiApiService::class.java)
    }
}
