package com.galyxnovatec.tienda.data.manager

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.galyxnovatec.tienda.BuildConfig
import com.galyxnovatec.tienda.data.model.User
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AuthManager {
    var currentUser by mutableStateOf<User?>(null)
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    suspend fun login(email: String, clave: String): Boolean {
        isLoading = true
        error = null
        val cleanEmail = email.trim().lowercase()
        val cleanClave = clave.trim()
        
        if (cleanEmail == BuildConfig.ADMIN_EMAIL && cleanClave == BuildConfig.ADMIN_PASSWORD) {
            currentUser = User(
                id = 1, 
                nombre = "Administrador Maestro", 
                email = BuildConfig.ADMIN_EMAIL, 
                telefono = "978104136", 
                direccion = "Sede Central GALY", 
                imagenUrl = null,
                esAdmin = true
            )
            vincularTokenFCM()
            isLoading = false
            return true
        }

        return try {
            val response = RetrofitClient.instance.login(mapOf("email" to cleanEmail, "password" to cleanClave))
            if (response.success) {
                currentUser = response.user
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

    suspend fun register(nombre: String, email: String, clave: String): Boolean {
        isLoading = true
        error = null
        return try {
            val response = RetrofitClient.instance.register(
                mapOf("nombre" to nombre.trim(), "email" to email.trim().lowercase(), "password" to clave.trim())
            )
            if (response.success) {
                currentUser = response.user
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

    private fun vincularTokenFCM() {
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
        CartManager.limpiarCarrito(context)
    }

    suspend fun loginConGoogle(idToken: String, nombre: String, email: String): Boolean {
        isLoading = true
        error = null
        return try {
            val response = RetrofitClient.instance.login(
                mapOf(
                    "email" to email, 
                    "password" to "GOOGLE_AUTH", 
                    "nombre" to nombre,
                    "id_token" to idToken
                )
            )
            if (response.success) {
                currentUser = response.user
                vincularTokenFCM()
                true
            } else {
                error = response.message
                false
            }
        } catch (e: Exception) {
            error = "Error al conectar con Google: ${e.message}"
            false
        } finally {
            isLoading = false
        }
    }

    fun loginSimuladoGoogle() {
        currentUser = User(
            id = 1,
            nombre = "Usuario de Google",
            email = "google.user@example.com",
            telefono = "999999999",
            direccion = "Conexión vía Google",
            imagenUrl = null
        )
        vincularTokenFCM()
    }

    suspend fun updateProfile(nombre: String?, telefono: String?, direccion: String?, password: String?): String? {
        val id = currentUser?.id ?: return "Usuario no identificado"
        isLoading = true
        return try {
            val data = mutableMapOf<String, Any>("id" to id)
            nombre?.let { data["nombre"] = it }
            telefono?.let { data["telefono"] = it }
            direccion?.let { data["direccion"] = it }
            password?.let { if (it.isNotEmpty()) data["password"] = it }

            val response = RetrofitClient.instance.updateUsuario(data)
            if (response["success"] == true) {
                val userMap = response["user"] as? Map<*, *>
                if (userMap != null) {
                    currentUser = User(
                        id = (userMap["id"] as? Double)?.toInt() ?: id,
                        nombre = userMap["nombre"] as? String ?: "",
                        email = userMap["email"] as? String ?: "",
                        telefono = userMap["telefono"] as? String,
                        direccion = userMap["direccion"] as? String,
                        imagenUrl = userMap["imagen_url"] as? String,
                        esAdmin = userMap["es_admin"] as? Boolean ?: false,
                        puntos = (userMap["puntos"] as? Double)?.toInt() ?: 0
                    )
                }
                null
            } else {
                response["message"] as? String ?: "Error desconocido"
            }
        } catch (e: Exception) {
            "Error de conexión: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    suspend fun updateProfileImage(nuevoNombreImagen: String): Boolean {
        val id = currentUser?.id ?: return false
        return try {
            val res = RetrofitClient.instance.updateUsuarioImagen(
                mapOf("id" to id, "imagen_url" to nuevoNombreImagen)
            )
            if (res["success"] == true) {
                currentUser = currentUser?.copy(imagenUrl = nuevoNombreImagen)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }
}
