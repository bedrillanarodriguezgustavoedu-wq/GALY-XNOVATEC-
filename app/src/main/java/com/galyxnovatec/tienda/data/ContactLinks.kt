package com.galyxnovatec.tienda.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

private fun contactNumber(value: String?): String? {
    val raw = value?.trim().orEmpty()
    if (raw.isEmpty()) return null

    val number = if (raw.startsWith("http://", ignoreCase = true) || raw.startsWith("https://", ignoreCase = true)) {
        val uri = Uri.parse(raw)
        if (uri.host.equals("wa.me", ignoreCase = true)) {
            uri.pathSegments.firstOrNull().orEmpty()
        } else {
            uri.getQueryParameter("phone").orEmpty()
        }
    } else {
        raw
    }.filter(Char::isDigit)

    return number.takeIf { it.isNotEmpty() }
}

fun Context.openContactWhatsApp(value: String?, message: String): Boolean {
    val number = contactNumber(value)
    if (number == null) {
        Toast.makeText(this, "El WhatsApp de contacto no está configurado.", Toast.LENGTH_LONG).show()
        return false
    }

    return try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://wa.me/$number?text=${Uri.encode(message)}")
            )
        )
        true
    } catch (exception: Exception) {
        Toast.makeText(this, "No se pudo abrir WhatsApp: ${exception.message}", Toast.LENGTH_LONG).show()
        false
    }
}

fun Context.dialContactNumber(value: String?): Boolean {
    val number = contactNumber(value)
    if (number == null) {
        Toast.makeText(this, "El teléfono de contacto no está configurado.", Toast.LENGTH_LONG).show()
        return false
    }

    return try {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
        true
    } catch (exception: Exception) {
        Toast.makeText(this, "No se pudo abrir el marcador: ${exception.message}", Toast.LENGTH_LONG).show()
        false
    }
}
