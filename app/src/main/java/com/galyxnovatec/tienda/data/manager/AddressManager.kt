package com.galyxnovatec.tienda.data.manager

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.galyxnovatec.tienda.data.model.Direccion
import com.galyxnovatec.tienda.data.network.RetrofitClient

object AddressManager {
    val addresses = mutableStateListOf<Direccion>()
    var isLoading by mutableStateOf(false)

    suspend fun fetchAddresses(userId: Int) {
        isLoading = true
        try {
            val remoteAddresses = RetrofitClient.instance.getDirecciones(userId)
            addresses.clear()
            addresses.addAll(remoteAddresses)
        } catch (e: Exception) {
            android.util.Log.e("GALY", "Error cargando direcciones: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    suspend fun addAddress(userId: Int, direccion: String, ciudad: String, distrito: String?, referencia: String?, mapsUrl: String? = null): Boolean {
        return try {
            val data = mutableMapOf<String, Any>(
                "usuario_id" to userId,
                "direccion" to direccion,
                "ciudad" to ciudad
            )
            if (distrito != null) data["distrito"] = distrito
            if (referencia != null) data["referencia"] = referencia
            if (mapsUrl != null) data["maps_url"] = mapsUrl
            
            val response = RetrofitClient.instance.addDireccion(data)
            if (response["success"] == true) {
                fetchAddresses(userId)
                true
            } else false
        } catch (e: Exception) {
            android.util.Log.e("GALY", "Error agregando dirección: ${e.message}")
            false
        }
    }
}
