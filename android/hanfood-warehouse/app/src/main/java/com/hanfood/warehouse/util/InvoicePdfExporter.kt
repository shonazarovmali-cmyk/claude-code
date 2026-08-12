package com.hanfood.warehouse.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.hanfood.warehouse.data.local.entity.StockTransaction
import com.hanfood.warehouse.data.local.entity.TransactionItemDetail
import com.hanfood.warehouse.data.local.entity.TransactionType
import java.io.File
import java.io.FileOutputStream

/**
 * Fakturani oddiy A4 formatidagi bir sahifali PDF ko'rinishida yaratadi
 * (tashqi kutubxonasiz — Android'ning o'rnatilgan `android.graphics.pdf` API'si
 * orqali), so'ngra ulashish uchun content:// Uri qaytaradi.
 */
object InvoicePdfExporter {

    private const val PAGE_WIDTH = 595 // A4 @ 72dpi
    private const val PAGE_HEIGHT = 842

    fun export(
        context: Context,
        transaction: StockTransaction,
        counterpartyLabel: String,
        counterpartyName: String,
        items: List<TransactionItemDetail>
    ): Uri {
        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create())
        val canvas = page.canvas

        val titlePaint = Paint().apply { textSize = 20f; isFakeBoldText = true }
        val labelPaint = Paint().apply { textSize = 12f; color = android.graphics.Color.DKGRAY }
        val bodyPaint = Paint().apply { textSize = 13f }
        val headerPaint = Paint().apply { textSize = 12f; isFakeBoldText = true }

        var y = 50f
        canvas.drawText("HAN FOOD Ombor — Faktura", 40f, y, titlePaint)
        y += 28f
        canvas.drawText(transaction.invoiceNumber, 40f, y, bodyPaint)
        y += 24f

        canvas.drawText("Turi: ${typeLabel(transaction.type)}", 40f, y, bodyPaint); y += 18f
        canvas.drawText("Sana: ${formatDateTime(transaction.date)}", 40f, y, bodyPaint); y += 18f
        canvas.drawText("$counterpartyLabel: $counterpartyName", 40f, y, bodyPaint); y += 18f
        if (!transaction.note.isNullOrBlank()) {
            canvas.drawText("Izoh: ${transaction.note}", 40f, y, bodyPaint); y += 18f
        }
        y += 12f

        canvas.drawText("Mahsulot", 40f, y, headerPaint)
        canvas.drawText("Miqdor", 300f, y, headerPaint)
        canvas.drawText("Narxi", 390f, y, headerPaint)
        canvas.drawText("Summa", 480f, y, headerPaint)
        y += 8f
        canvas.drawLine(40f, y, 555f, y, labelPaint)
        y += 18f

        items.forEach { item ->
            canvas.drawText(item.productName.take(30), 40f, y, bodyPaint)
            canvas.drawText("${formatQuantity(item.quantity)} ${item.unit}", 300f, y, bodyPaint)
            canvas.drawText(formatQuantity(item.unitPrice), 390f, y, bodyPaint)
            canvas.drawText(formatQuantity(item.lineTotal), 480f, y, bodyPaint)
            y += 20f
        }

        y += 10f
        canvas.drawLine(40f, y, 555f, y, labelPaint)
        y += 24f
        canvas.drawText("Jami: ${formatMoney(transaction.totalAmount)}", 400f, y, titlePaint.apply { textSize = 15f })

        document.finishPage(page)

        val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val file = File(dir, "${transaction.invoiceNumber}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun typeLabel(type: TransactionType): String = when (type) {
        TransactionType.STOCK_IN -> "Kirim"
        TransactionType.STOCK_OUT -> "Chiqim (yuk berish)"
        TransactionType.RETURN -> "Qaytarish"
    }
}
