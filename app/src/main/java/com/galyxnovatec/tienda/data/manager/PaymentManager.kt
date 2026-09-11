package com.galyxnovatec.tienda.data.manager

import android.app.Activity
import android.content.Intent
import com.galyxnovatec.tienda.BuildConfig
import com.galyxnovatec.tienda.data.model.CartItem
import com.mercadopago.android.px.core.MercadoPagoCheckout
import com.mercadopago.android.px.model.Payment

object PaymentManager {
    // LLAVE PÚBLICA DE GALY XNOVATEC (Cargada desde local.properties)
    private val PUBLIC_KEY = BuildConfig.MP_PUBLIC_KEY

    fun iniciarPago(activity: Activity, total: Double, items: List<CartItem>, requestCode: Int) {
        // En un proyecto real, primero crearías una preferencia en tu PHP y obtendrías un PREFERENCE_ID
        // Por ahora usamos un ID simulado para que la app no se detenga
        val checkout = MercadoPagoCheckout.Builder(PUBLIC_KEY, "simulated_id")
            .build()
        
        checkout.startPayment(activity, requestCode)
    }

    fun procesarResultado(resultCode: Int, data: Intent?): Boolean {
        if (resultCode == MercadoPagoCheckout.PAYMENT_RESULT_CODE) {
            val payment = data?.getSerializableExtra(MercadoPagoCheckout.EXTRA_PAYMENT_RESULT) as? Payment
            // Verificamos si el pago fue aprobado por Mercado Pago
            return payment?.paymentStatus == "approved"
        }
        return false
    }
}
