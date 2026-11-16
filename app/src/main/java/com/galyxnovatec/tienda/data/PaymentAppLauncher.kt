package com.galyxnovatec.tienda.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object PaymentAppLauncher {
    fun open(context: Context, packageName: String, appLabel: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            try {
                context.startActivity(launchIntent)
                Toast.makeText(
                    context,
                    "Completa la transferencia en $appLabel y confirma el importe y el destinatario.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (exception: ActivityNotFoundException) {
                openStoreListing(context, packageName, appLabel)
            }
            return
        }

        openStoreListing(context, packageName, appLabel)
    }

    private fun openStoreListing(context: Context, packageName: String, appLabel: String) {
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        )

        try {
            context.startActivity(marketIntent)
        } catch (_: ActivityNotFoundException) {
            try {
                context.startActivity(webIntent)
            } catch (exception: ActivityNotFoundException) {
                Toast.makeText(
                    context,
                    "No se pudo abrir $appLabel ni su página de instalación.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
