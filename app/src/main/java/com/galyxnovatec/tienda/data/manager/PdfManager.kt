package com.galyxnovatec.tienda.data.manager

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.galyxnovatec.tienda.data.model.Pedido
import java.io.File
import java.io.FileOutputStream

object PdfManager {

    fun generarBoleta(context: Context, pedido: Pedido) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        val titlePaint = Paint()

        // --- DISEÑO DE LA BOLETA GALY ---
        
        // Título
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 18f
        titlePaint.color = Color.BLACK
        canvas.drawText("GALY XNOVATEC", 80f, 40f, titlePaint)

        paint.textSize = 10f
        canvas.drawText("RUC: 20601234567", 100f, 60f, paint)
        canvas.drawText("Boleta de Venta Electrónica", 85f, 75f, paint)
        
        canvas.drawLine(10f, 90f, 290f, 90f, paint)

        // Datos del Pedido
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("PEDIDO #${pedido.id}", 20f, 110f, paint)
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Fecha: ${pedido.fecha}", 20f, 125f, paint)
        canvas.drawText("Estado: ${pedido.estado}", 20f, 140f, paint)

        canvas.drawLine(10f, 155f, 290f, 155f, paint)

        // Encabezado Tabla
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Producto", 20f, 170f, paint)
        canvas.drawText("Cant.", 180f, 170f, paint)
        canvas.drawText("Total", 240f, 170f, paint)
        paint.typeface = Typeface.DEFAULT

        var yPos = 190f
        pedido.productos?.forEach { item ->
            canvas.drawText(item.nombre?.take(20) ?: "Producto", 20f, yPos, paint)
            canvas.drawText("${item.cantidad}", 190f, yPos, paint)
            canvas.drawText("S/ ${(item.precio * item.cantidad).toInt()}", 240f, yPos, paint)
            yPos += 20f
        }

        canvas.drawLine(10f, yPos + 10f, 290f, yPos + 10f, paint)

        // Totales
        val finalY = yPos + 40f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("TOTAL A PAGAR:", 120f, finalY, paint)
        canvas.drawText("S/ ${pedido.total.toInt()}", 240f, finalY, paint)

        canvas.drawText("¡Gracias por su compra!", 90f, finalY + 40f, paint)

        pdfDocument.finishPage(page)

        // Guardar Archivo
        val file = File(context.cacheDir, "Boleta_GALY_${pedido.id}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            compartirPdf(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pdfDocument.close()
    }

    private fun compartirPdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir Boleta GALY"))
    }
}
