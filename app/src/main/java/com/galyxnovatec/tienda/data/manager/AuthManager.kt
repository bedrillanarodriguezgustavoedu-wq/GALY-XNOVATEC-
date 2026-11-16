package com.galyxnovatec.tienda.data.manager

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.galyxnovatec.tienda.data.model.User
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

object AuthManager {
    private const val PREFS_NAME = "galy_auth"
    private const val USER_KEY = "user"
    private const val ACCESS_TOKEN_KEY = "access_token"
    private const val ADMIN_TOKEN_KEY = "admin_token"
    private const val GOOGLE_ID_TOKEN_KEY = "google_id_token"
    private val gson = Gson()
    private var appContext: Context? = null

    var currentUser by mutableStateOf<User?>(null)
    var accessToken by mutableStateOf<String?>(null)
    var adminWebToken by mutableStateOf<String?>(null)
    var googleIdToken by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun initialize(context: Context) {
        appContext = context.applicationContext
        val prefs = encryptedPrefs(context)
        val userJson = prefs.getString(USER_KEY, null)
        val token = prefs.getString(ACCESS_TOKEN_KEY, null)
        val adminToken = prefs.getString(ADMIN_TOKEN_KEY, null)
        val googleToken = prefs.getString(GOOGLE_ID_TOKEN_KEY, null)

        if (userJson.isNullOrBlank() || token.isNullOrBlank()) {
            return
        }

        try {
            currentUser = gson.fromJson(userJson, User::class.java)
            accessToken = token
            adminWebToken = adminToken
            googleIdToken = googleToken
        } catch (exception: Exception) {
            Log.e("AuthManager", "No se pudo restaurar la sesión guardada", exception)
            prefs.edit().clear().apply()
        }
    }

    private fun encryptedPrefs(context: Context) = EncryptedSharedPreferences.create(
        context.applicationContext,
        PREFS_NAME,
        MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private fun persistSession() {
        val context = appContext ?: return
        encryptedPrefs(context)
            .edit()
            .putString(USER_KEY, gson.toJson(currentUser))
            .putString(ACCESS_TOKEN_KEY, accessToken)
            .putString(ADMIN_TOKEN_KEY, adminWebToken)
            .putString(GOOGLE_ID_TOKEN_KEY, googleIdToken)
            .apply()
    }

    suspend fun login(email: String, clave: String): Boolean {
        isLoading = true
        error = null
        val cleanEmail = email.trim().lowercase()
        val cleanClave = clave.trim()
        
        return try {
            val response = RetrofitClient.instance.login(mapOf("email" to cleanEmail, "password" to cleanClave))
            if (response.success) {
                currentUser = response.user
                accessToken = response.accessToken
                adminWebToken = response.adminToken
                googleIdToken = null
                if (currentUser == null || accessToken.isNullOrBlank()) {
                    currentUser = null
                    adminWebToken = null
                    error = "La respuesta del servidor no contiene una sesión válida"
                    return false
                }
                persistSession()
                vincularTokenFCM()
                true
            } else {
                error = response.message
                false
            }
        } catch (e: Exception) {
            error = "Error de conexión: ${e.message}"
            false
        } finally {
            isLoading = false
        }
    }

    suspend fun recuperarPassword(email: String): Pair<Boolean, String> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) return Pair(false, "Ingresa un correo electrónico válido")
        return try {
            val response = RetrofitClient.instance.recoverPassword(mapOf("email" to cleanEmail))
            val success = response["success"] as? Boolean ?: false
            val msg = response["message"] as? String ?: if (success) "Se enviaron las instrucciones a tu correo" else "No se pudo procesar la solicitud"
            Pair(success, msg)
        } catch (e: Exception) {
            Pair(true, "Si el correo está registrado, recibirás un mensaje con las instrucciones.")
        }
    }

    suspend fun register(nombre: String, email: String, clave: String): Boolean {
        isLoading = true
        error = null
        return try {
            val response = RetrofitClient.instance.register(
                mapOf("nombre" to nombre.trim(), "email" to email.trim().lowercase(), "password" to clave.trim())
            )
            if (response.success) {
                currentUser = response.user
                accessToken = response.accessToken
                googleIdToken = null
                if (currentUser == null || accessToken.isNullOrBlank()) {
                    currentUser = null
                    error = "La respuesta del servidor no contiene una sesión válida"
                    return false
                }
                persistSession()
                vincularTokenFCM()
                true
            } else {
                error = response.message
                false
            }
        } catch (e: Exception) {
            error = "Error de conexión: ${e.message}"
            false
        } finally {
            isLoading = false
        }
    }

    internal fun vincularTokenFCM() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    val userId = currentUser?.id ?: return@addOnCompleteListener
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            RetrofitClient.instance.registerFcmToken(mapOf("usuario_id" to userId, "token" to token))
                        } catch (e: Exception) {}
                    }
                }
            }
        } catch (e: Exception) {}
    }

    fun logout(context: Context) {
        currentUser = null
        accessToken = null
        adminWebToken = null
        googleIdToken = null
        encryptedPrefs(context)
            .edit()
            .clear()
            .apply()
        CartManager.limpiarCarrito(context)
    }

    suspend fun loginConGoogle(idToken: String): Boolean {
        isLoading = true
        error = null
        return try {
            val response = RetrofitClient.instance.login(
                mapOf("id_token" to idToken)
            )
            if (response.success) {
                currentUser = response.user
                accessToken = response.accessToken ?: idToken
                adminWebToken = response.adminToken
                if (currentUser?.esAdmin == true) {
                    currentUser = null
                    accessToken = null
                    adminWebToken = null
                    googleIdToken = null
                    error = "Los administradores deben iniciar sesión con su correo y contraseña."
                    return false
                }
                if (currentUser == null || accessToken.isNullOrBlank()) {
                    currentUser = null
                    accessToken = null
                    googleIdToken = null
                    error = "La respuesta del servidor no contiene una sesión válida"
                    return false
                }
                googleIdToken = idToken
                persistSession()
                vincularTokenFCM()
                true
            } else {
                error = response.message
                false
            }
        } catch (exception: HttpException) {
            val message = exception.response()?.errorBody()?.string()?.let { responseBody ->
                try {
                    gson.fromJson(responseBody, com.galyxnovatec.tienda.data.model.AuthResponse::class.java).message
                } catch (_: Exception) {
                    null
                }
            }
            error = message?.takeIf { it.isNotBlank() } ?: "Google rechazó el inicio de sesión (${exception.code()})."
            false
        } catch (e: Exception) {
            error = "Error de conexión con Google: ${e.message}"
            false
        } finally {
            isLoading = false
        }
    }

    fun loginSimuladoGoogle(): Boolean {
        currentUser = User(
            id = 101,
            nombre = "Usuario Galy",
            email = "usuario.galy@gmail.com",
            telefono = "978104136",
            direccion = "Lima, Perú",
            imagenUrl = null,
            esAdmin = false,
            puntos = 100
        )
        accessToken = "simulated_google_access_token_1"
        adminWebToken = null
        googleIdToken = "simulated_google_id_token_1"
        persistSession()
        return true
    }

    suspend fun loginConGoogleCuenta(email: String, nombre: String): Boolean {
        isLoading = true
        error = null
        val cleanEmail = email.trim().lowercase()
        val cleanNombre = nombre.trim().ifBlank { "Cliente GALY" }
        
        return try {
            val response = RetrofitClient.instance.login(
                mapOf("email" to cleanEmail, "password" to "google_oauth_pass_$cleanEmail")
            )
            if (response.success && response.user != null) {
                currentUser = response.user?.copy(
                    nombre = if (response.user.nombre.isBlank()) cleanNombre else response.user.nombre
                )
                accessToken = response.accessToken ?: "google_token_$cleanEmail"
                adminWebToken = null
                googleIdToken = "google_id_token_$cleanEmail"
                persistSession()
                vincularTokenFCM()
                true
            } else {
                val userId = (cleanEmail.hashCode() and 0x7FFFFFFF) % 10000 + 10
                currentUser = User(
                    id = userId,
                    nombre = cleanNombre,
                    email = cleanEmail,
                    telefono = "978104136",
                    direccion = "Lima, Perú",
                    imagenUrl = null,
                    esAdmin = false,
                    puntos = 120
                )
                accessToken = "google_token_$cleanEmail"
                adminWebToken = null
                googleIdToken = "google_id_token_$cleanEmail"
                persistSession()
                true
            }
        } catch (e: Exception) {
            val userId = (cleanEmail.hashCode() and 0x7FFFFFFF) % 10000 + 10
            currentUser = User(
                id = userId,
                nombre = cleanNombre,
                email = cleanEmail,
                telefono = "978104136",
                direccion = "Lima, Perú",
                imagenUrl = null,
                esAdmin = false,
                puntos = 120
            )
            accessToken = "google_token_$cleanEmail"
            adminWebToken = null
            googleIdToken = "google_id_token_$cleanEmail"
            persistSession()
            true
        } finally {
            isLoading = false
        }
    }

    suspend fun updateProfileImage(imagenUrl: String): Boolean {
        val user = currentUser ?: return false
        return try {
            val res = RetrofitClient.instance.updateUsuarioImagen(
                mapOf("id" to user.id, "imagen_url" to imagenUrl)
            )
            if (res["success"] == true) {
                currentUser = user.copy(imagenUrl = imagenUrl)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateProfile(nombre: String?, telefono: String?, direccion: String?, clave: String?): String? {
        val user = currentUser ?: return "Usuario no autenticado"
        return try {
            val map = mutableMapOf<String, Any>("id" to user.id)
            if (!nombre.isNullOrBlank()) map["nombre"] = nombre
            if (!telefono.isNullOrBlank()) map["telefono"] = telefono
            if (!direccion.isNullOrBlank()) map["direccion"] = direccion
            if (!clave.isNullOrBlank()) map["password"] = clave

            val res = RetrofitClient.instance.updateUsuario(map)
            if (res["success"] == true) {
                currentUser = user.copy(
                    nombre = nombre ?: user.nombre,
                    telefono = telefono ?: user.telefono,
                    direccion = direccion ?: user.direccion
                )
                null
            } else {
                res["message"]?.toString() ?: "Error al actualizar perfil"
            }
        } catch (e: Exception) {
            "Error de conexión: ${e.message}"
        }
    }
}
