package com.galyxnovatec.tienda.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.galyxnovatec.tienda.MainActivity
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.manager.AuthManager
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "GALY STORE"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""
        val targetScreen = remoteMessage.data["target_screen"] ?: remoteMessage.data["screen"]
        val orderId = remoteMessage.data["order_id"]
        val productId = remoteMessage.data["product_id"]

        sendNotification(title, body, targetScreen, orderId, productId)
    }

    private fun sendNotification(
        title: String,
        messageBody: String,
        targetScreen: String? = null,
        orderId: String? = null,
        productId: String? = null
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            targetScreen?.let { putExtra("EXTRA_TARGET_SCREEN", it) }
            orderId?.let { putExtra("EXTRA_ORDER_ID", it) }
            productId?.let { putExtra("EXTRA_PRODUCT_ID", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "galy_notifications"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo_2)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Notificaciones de GALY",
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        Log.d("GALY_FCM", "Nuevo Token: $token")
        enviarTokenAlServidor(token)
    }

    private fun enviarTokenAlServidor(token: String) {
        val userId = AuthManager.currentUser?.id ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.instance.registerFcmToken(mapOf("usuario_id" to userId, "token" to token))
                Log.d("GALY_FCM", "Token registrado en el servidor para usuario #$userId")
            } catch (e: Exception) {
                Log.e("GALY_FCM", "Error registrando token: ${e.message}")
            }
        }
    }
}
