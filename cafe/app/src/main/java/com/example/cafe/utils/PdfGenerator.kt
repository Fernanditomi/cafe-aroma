package com.example.cafe.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.cafe.ui.CartItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {
    fun generateInvoicePdf(
        context: Context,
        customerName: String,
        items: List<CartItem>,
        total: Double,
        paymentMethod: String
    ): File? {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()

        // Page info (A4 size approx)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Header - Brand
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 24f
        titlePaint.color = Color.parseColor("#3D2C24") // PrimaryCoffee
        canvas.drawText("CAFÉ AROMA", 50f, 50f, titlePaint)

        paint.textSize = 12f
        paint.color = Color.GRAY
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Fecha: $date", 50f, 75f, paint)
        canvas.drawText("Factura #ORD-${System.currentTimeMillis().toString().takeLast(6)}", 50f, 95f, paint)

        // Customer Info
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.BLACK
        canvas.drawText("CLIENTE: $customerName", 50f, 130f, paint)
        canvas.drawText("MÉTODO DE PAGO: $paymentMethod", 50f, 150f, paint)

        // Table Header
        paint.color = Color.DKGRAY
        canvas.drawRect(50f, 180f, 545f, 205f, paint.apply { alpha = 20 })
        paint.alpha = 255
        canvas.drawText("Producto", 60f, 197f, paint)
        canvas.drawText("Cant.", 350f, 197f, paint)
        canvas.drawText("Precio", 420f, 197f, paint)
        canvas.drawText("Total", 490f, 197f, paint)

        // Table Content
        var yPos = 230f
        items.forEach { item ->
            val basePrice = item.menuItem.price.replace("$", "").toDoubleOrNull() ?: 0.0
            val unitPrice = if (item.menuItem.discount > 0) basePrice * (1 - item.menuItem.discount / 100.0) else basePrice
            val itemTotal = unitPrice * item.quantity

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(item.menuItem.name, 60f, yPos, paint)
            canvas.drawText(item.quantity.toString(), 360f, yPos, paint)
            canvas.drawText("$${String.format("%.2f", unitPrice)}", 420f, yPos, paint)
            canvas.drawText("$${String.format("%.2f", itemTotal)}", 490f, yPos, paint)
            
            yPos += 25f
        }

        // Total
        canvas.drawLine(50f, yPos + 10f, 545f, yPos + 10f, paint)
        titlePaint.textSize = 18f
        canvas.drawText("TOTAL A PAGAR:", 300f, yPos + 50f, titlePaint)
        canvas.drawText("$${String.format("%.2f", total)}", 470f, yPos + 50f, titlePaint)

        // Footer
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText("¡Gracias por preferir Café Aroma! Disfrute su estancia.", 50f, 800f, paint)

        pdfDocument.finishPage(page)

        // Save File
        val fileName = "Factura_CafeAroma_${System.currentTimeMillis()}.pdf"
        val filePath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(filePath, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            pdfDocument.close()
        }
        return file
    }
}
