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
            // Por defecto solo compartimos
            compartirPdf(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pdfDocument.close()
    }

    fun verBoleta(context: Context, pedido: Pedido) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        val titlePaint = Paint()

        // --- DISEÑO ---
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 18f
        titlePaint.color = Color.BLACK
        canvas.drawText("GALY XNOVATEC", 80f, 40f, titlePaint)
        paint.textSize = 10f
        canvas.drawText("Boleta de Venta Electrónica", 85f, 75f, paint)
        canvas.drawLine(10f, 90f, 290f, 90f, paint)
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("PEDIDO #${pedido.id}", 20f, 110f, paint)
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("DNI/RUC: ${pedido.clienteDni ?: "N/A"}", 20f, 125f, paint)
        canvas.drawText("Cliente: ${pedido.clienteNombre}", 20f, 140f, paint)
        canvas.drawText("Total: S/ ${pedido.total.toInt()}", 20f, 155f, paint)
        canvas.drawLine(10f, 170f, 290f, 170f, paint)
        
        var yPos = 190f
        pedido.productos?.forEach { item ->
            canvas.drawText("${item.nombre?.take(15)} x${item.cantidad}", 20f, yPos, paint)
            canvas.drawText("S/ ${(item.precio * item.cantidad).toInt()}", 240f, yPos, paint)
            yPos += 20f
        }
        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "Boleta_GALY_${pedido.id}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Abrir Boleta GALY"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pdfDocument.close()
    }

    fun enviarBoletaPorWhatsApp(context: Context, pedido: Pedido) {
        // Primero generamos el archivo (reutilizando la lógica)
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        val titlePaint = Paint()

        // (Mismo diseño para consistencia)
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 18f
        titlePaint.color = Color.BLACK
        canvas.drawText("GALY XNOVATEC", 80f, 40f, titlePaint)
        paint.textSize = 10f
        canvas.drawText("Boleta Electrónica", 90f, 75f, paint)
        canvas.drawLine(10f, 90f, 290f, 90f, paint)
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("PEDIDO #${pedido.id}", 20f, 110f, paint)
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("DNI: ${pedido.clienteDni ?: "N/A"}", 20f, 125f, paint)
        canvas.drawText("Total: S/ ${pedido.total.toInt()}", 20f, 140f, paint)
        canvas.drawLine(10f, 155f, 290f, 155f, paint)
        
        var yPos = 180f
        pedido.productos?.forEach { item ->
            canvas.drawText("${item.nombre?.take(15)} x${item.cantidad}", 20f, yPos, paint)
            canvas.drawText("S/ ${(item.precio * item.cantidad).toInt()}", 240f, yPos, paint)
            yPos += 20f
        }
        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "Boleta_GALY_${pedido.id}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            
            val mensajeWhatsApp = """
                🚀 *GALY XNOVATEC - Tu Boleta Digital*
                
                Hola *${pedido.clienteNombre}*, adjuntamos la boleta de tu pedido *#${pedido.id}*. 
                
                💰 Total: S/ ${pedido.total.toInt()}
                ✅ Estado: ${pedido.estado}
                
                ¡Gracias por confiar en nosotros! 📱🔥
            """.trimIndent()

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, mensajeWhatsApp)
                
                // Intento de enviar a número específico en WhatsApp (Best Effort)
                if (!pedido.clienteTelefono.isNullOrEmpty()) {
                    val phone = pedido.clienteTelefono.replace(" ", "").replace("+", "")
                    val formattedPhone = if (phone.startsWith("51")) phone else "51$phone"
                    putExtra("jid", "$formattedPhone@s.whatsapp.net")
                }
                
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            }
            
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Si no tiene WhatsApp instalado, usamos el selector normal
                val shareIntent = Intent.createChooser(intent, "Enviar Boleta por...")
                context.startActivity(shareIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pdfDocument.close()
    }

    fun enviarBoletaPorEmail(context: Context, pedido: Pedido) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        val titlePaint = Paint()

        // (Mismo diseño que generarBoleta para consistencia)
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 18f
        titlePaint.color = Color.BLACK
        canvas.drawText("GALY XNOVATEC", 80f, 40f, titlePaint)
        paint.textSize = 10f
        canvas.drawText("RUC: 20601234567", 100f, 60f, paint)
        canvas.drawText("Boleta de Venta Electrónica", 85f, 75f, paint)
        canvas.drawLine(10f, 90f, 290f, 90f, paint)
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("PEDIDO #${pedido.id}", 20f, 110f, paint)
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Fecha: ${pedido.fecha}", 20f, 125f, paint)
        canvas.drawText("Total: S/ ${pedido.total.toInt()}", 20f, 140f, paint)
        canvas.drawLine(10f, 155f, 290f, 155f, paint)
        var yPos = 180f
        pedido.productos?.forEach { item ->
            canvas.drawText("${item.nombre?.take(15)} x${item.cantidad}", 20f, yPos, paint)
            canvas.drawText("S/ ${(item.precio * item.cantidad).toInt()}", 240f, yPos, paint)
            yPos += 20f
        }
        canvas.drawText("¡Gracias por su preferencia!", 80f, yPos + 40f, paint)
        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "Boleta_GALY_${pedido.id}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            
            // Intent para Gmail
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(pedido.clienteEmail ?: ""))
                putExtra(Intent.EXTRA_SUBJECT, "Boleta de Venta - Pedido #${pedido.id} - GALY XNOVATEC")
                putExtra(Intent.EXTRA_TEXT, "Estimado(a) ${pedido.clienteNombre},\n\nAdjuntamos la boleta de su compra realizada en GALY XNOVATEC.\n\n¡Gracias por su preferencia!")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.google.android.gm") // Forzamos Gmail
            }
            
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Si no hay Gmail, usamos el selector normal
                val shareIntent = Intent.createChooser(intent, "Enviar Boleta por Email")
                context.startActivity(shareIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pdfDocument.close()
    }

    fun generarReportePedidos(context: Context, pedidos: List<Pedido>) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 20f
        canvas.drawText("REPORTE DE VENTAS GALY", 50f, 50f, paint)
        
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 10f
        canvas.drawText("Fecha de generación: ${System.currentTimeMillis()}", 50f, 70f, paint)
        
        canvas.drawLine(50f, 85f, 545f, 85f, paint)
        
        // Cabecera Tabla
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("ID", 50f, 105f, paint)
        canvas.drawText("CLIENTE", 80f, 105f, paint)
        canvas.drawText("ESTADO", 250f, 105f, paint)
        canvas.drawText("TOTAL", 480f, 105f, paint)
        
        paint.typeface = Typeface.DEFAULT
        var y = 125f
        pedidos.forEach { o ->
            canvas.drawText("#${o.id}", 50f, y, paint)
            canvas.drawText(o.clienteNombre?.take(25) ?: "N/A", 80f, y, paint)
            canvas.drawText(o.estado, 250f, y, paint)
            canvas.drawText("S/ ${o.total.toInt()}", 480f, y, paint)
            y += 20f
            if (y > 800) return@forEach // Simple limit for one page
        }
        
        canvas.drawLine(50f, y + 10f, 545f, y + 10f, paint)
        val totalSum = pedidos.sumOf { it.total }
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("TOTAL GENERAL:", 350f, y + 30f, paint)
        canvas.drawText("S/ ${totalSum.toInt()}", 480f, y + 30f, paint)
        
        pdfDocument.finishPage(page)
        
        val file = File(context.cacheDir, "Reporte_Ventas_General.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            compartirPdf(context, file)
        } catch (e: Exception) { e.printStackTrace() }
        pdfDocument.close()
    }

    fun generarReporteGanancias(context: Context, pedidos: List<Pedido>) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        
        // Título Principal
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 22f
        paint.color = Color.BLACK
        canvas.drawText("REPORTE DE GANANCIAS GALY", 50f, 50f, paint)
        
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText("GALY XNOVATEC S.A.C. - Sistema Contable Maestro", 50f, 70f, paint)
        
        canvas.drawLine(50f, 90f, 545f, 90f, paint)
        
        // Encabezado Tabla
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = Color.BLACK
        paint.textSize = 11f
        canvas.drawText("ID", 50f, 115f, paint)
        canvas.drawText("FECHA", 80f, 115f, paint)
        canvas.drawText("VENTA (S/)", 200f, 115f, paint)
        canvas.drawText("COSTO (S/)", 300f, 115f, paint)
        canvas.drawText("UTILIDAD (S/)", 420f, 115f, paint)
        
        paint.typeface = Typeface.DEFAULT
        var y = 140f
        var totalVentas = 0.0
        var totalCostos = 0.0
        var totalUtilidad = 0.0

        pedidos.filter { it.estado == "ENTREGADO" || it.estado == "PAGADO" }.forEach { o ->
            var costoPedido = 0.0
            o.productos?.forEach { p ->
                costoPedido += (p.precioCompra ?: 0.0) * p.cantidad
            }
            val utilidadPedido = o.total - costoPedido
            
            canvas.drawText("#${o.id}", 50f, y, paint)
            canvas.drawText(o.fecha.take(10), 80f, y, paint)
            canvas.drawText(o.total.toInt().toString(), 210f, y, paint)
            canvas.drawText(costoPedido.toInt().toString(), 310f, y, paint)
            
            paint.color = if (utilidadPedido > 0) Color.rgb(0, 150, 0) else Color.RED
            canvas.drawText(utilidadPedido.toInt().toString(), 430f, y, paint)
            paint.color = Color.BLACK

            totalVentas += o.total
            totalCostos += costoPedido
            totalUtilidad += utilidadPedido
            
            y += 20f
            if (y > 780) { /* Aquí se podría añadir nueva página, por ahora límite simple */ }
        }
        
        canvas.drawLine(50f, y + 10f, 545f, y + 10f, paint)
        
        // Resumen Final
        y += 40f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("RESUMEN DE OPERACIÓN:", 50f, y, paint)
        y += 25f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Ventas Netas Totales:", 50f, y, paint)
        canvas.drawText("S/ ${totalVentas.toInt()}", 250f, y, paint)
        y += 20f
        canvas.drawText("Inversión en Productos (Costo):", 50f, y, paint)
        canvas.drawText("S/ ${totalCostos.toInt()}", 250f, y, paint)
        y += 30f
        paint.textSize = 16f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = Color.rgb(0, 100, 0)
        canvas.drawText("UTILIDAD NETA TOTAL:", 50f, y, paint)
        canvas.drawText("S/ ${totalUtilidad.toInt()}", 350f, y, paint)
        
        pdfDocument.finishPage(page)
        
        val file = File(context.cacheDir, "Reporte_Ganancias_GALY.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            compartirPdf(context, file)
        } catch (e: Exception) { e.printStackTrace() }
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
