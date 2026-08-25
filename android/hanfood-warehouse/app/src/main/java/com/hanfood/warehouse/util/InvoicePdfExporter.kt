package com.hanfood.warehouse.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.hanfood.warehouse.R
import com.hanfood.warehouse.data.local.entity.StockTransaction
import com.hanfood.warehouse.data.local.entity.TransactionItemDetail
import com.hanfood.warehouse.data.local.entity.TransactionType
import java.io.File
import java.io.FileOutputStream

/**
 * Renders the invoice as a simple one-page A4 PDF (no external library — uses
 * Android's built-in `android.graphics.pdf` API), then returns a content://
 * Uri for sharing. Labels are resolved from string resources via [context],
 * so the generated PDF matches the app's current language.
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
        val res = context.resources
        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create())
        val canvas = page.canvas

        val titlePaint = Paint().apply { textSize = 20f; isFakeBoldText = true }
        val labelPaint = Paint().apply { textSize = 12f; color = android.graphics.Color.DKGRAY }
        val bodyPaint = Paint().apply { textSize = 13f }
        val headerPaint = Paint().apply { textSize = 12f; isFakeBoldText = true }

        var y = 50f
        canvas.drawText(res.getString(R.string.pdf_title), 40f, y, titlePaint)
        y += 28f
        canvas.drawText(transaction.invoiceNumber, 40f, y, bodyPaint)
        y += 24f

        canvas.drawText("${res.getString(R.string.pdf_type_label)} ${typeLabel(context, transaction.type)}", 40f, y, bodyPaint); y += 18f
        canvas.drawText("${res.getString(R.string.pdf_date_label)} ${formatDateTime(transaction.date)}", 40f, y, bodyPaint); y += 18f
        canvas.drawText("$counterpartyLabel: $counterpartyName", 40f, y, bodyPaint); y += 18f
        if (!transaction.note.isNullOrBlank()) {
            canvas.drawText("${res.getString(R.string.pdf_note_label)} ${transaction.note}", 40f, y, bodyPaint); y += 18f
        }
        y += 12f

        canvas.drawText(res.getString(R.string.pdf_column_product), 40f, y, headerPaint)
        canvas.drawText(res.getString(R.string.pdf_column_quantity), 300f, y, headerPaint)
        canvas.drawText(res.getString(R.string.pdf_column_price), 390f, y, headerPaint)
        canvas.drawText(res.getString(R.string.pdf_column_total), 480f, y, headerPaint)
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
        canvas.drawText(
            "${res.getString(R.string.pdf_total_label)} ${formatMoney(transaction.totalAmount)}",
            400f,
            y,
            titlePaint.apply { textSize = 15f }
        )

        document.finishPage(page)

        val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val file = File(dir, "${transaction.invoiceNumber}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun typeLabel(context: Context, type: TransactionType): String = when (type) {
        TransactionType.STOCK_IN -> context.getString(R.string.transaction_type_stock_in)
        TransactionType.STOCK_OUT -> context.getString(R.string.transaction_type_stock_out)
        TransactionType.RETURN -> context.getString(R.string.transaction_type_return)
    }
}
