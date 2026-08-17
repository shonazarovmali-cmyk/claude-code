package com.hanfood.warehouse.ai

import android.content.Context
import com.hanfood.warehouse.R
import com.hanfood.warehouse.data.repository.WarehouseRepository
import com.hanfood.warehouse.util.formatMoney
import com.hanfood.warehouse.util.formatQuantity
import java.util.Calendar
import kotlinx.coroutines.flow.first

/**
 * Rule-based analysis engine over the local warehouse data — no network
 * access required. All answers are computed directly from real numbers in
 * the local database (no hallucination), and text is resolved from string
 * resources via [context] so the assistant speaks the app's current
 * language. Free-text intent matching below uses a best-effort multilingual
 * keyword list so typed questions work across all supported languages, not
 * just the one currently selected.
 */
class LocalAiAnalysisEngine(
    private val context: Context,
    private val repository: WarehouseRepository
) : AiEngine {

    override fun suggestedQuestions(): List<String> = listOf(
        context.getString(R.string.ai_question_low_stock),
        context.getString(R.string.ai_question_stock_value),
        context.getString(R.string.ai_question_stock_out_month),
        context.getString(R.string.ai_question_top_clients),
        context.getString(R.string.ai_question_top_out_products),
        context.getString(R.string.ai_question_returns_month)
    )

    override suspend fun answer(question: String): String {
        val q = question.lowercase().trim()
        return when {
            containsAny(q, LOW_STOCK_KEYWORDS) -> lowStockAnswer()
            containsAny(q, STOCK_VALUE_KEYWORDS) -> stockValueAnswer()
            containsAny(q, TOP_CLIENTS_KEYWORDS) -> topClientsAnswer()
            containsAny(q, RETURNS_KEYWORDS) -> returnsAnswer(q)
            containsAny(q, TOP_PRODUCTS_KEYWORDS) -> topOutProductsAnswer(q)
            containsAny(q, STOCK_IN_KEYWORDS) -> stockInAnswer(q)
            containsAny(q, STOCK_OUT_KEYWORDS) -> stockOutAnswer(q)
            else -> generalSummary()
        }
    }

    private fun containsAny(text: String, needles: List<String>) = needles.any { text.contains(it) }

    private suspend fun lowStockAnswer(): String {
        val lowStock = repository.lowStockProducts.first()
        if (lowStock.isEmpty()) return context.getString(R.string.ai_answer_low_stock_none)
        val lines = lowStock.take(8).joinToString("\n") { p ->
            context.getString(R.string.ai_answer_low_stock_line, p.name, formatQuantity(p.quantity), p.unit, formatQuantity(p.minQuantity))
        }
        val extra = if (lowStock.size > 8) context.getString(R.string.ai_answer_low_stock_more, lowStock.size - 8) else ""
        val header = context.getString(R.string.ai_answer_low_stock_header, lowStock.size)
        val tip = context.getString(R.string.ai_answer_low_stock_tip)
        return "$header\n$lines$extra$tip"
    }

    private suspend fun stockValueAnswer(): String {
        val value = repository.totalStockValue.first()
        val units = repository.totalUnits.first()
        return context.getString(R.string.ai_answer_stock_value, formatQuantity(units), formatMoney(value))
    }

    private suspend fun topClientsAnswer(): String {
        val (from, to) = monthRange()
        val summary = repository.reportSummary(from, to, topLimit = 5)
        if (summary.topClients.isEmpty()) return context.getString(R.string.ai_answer_top_clients_none)
        val lines = summary.topClients.joinToString("\n") { c ->
            context.getString(R.string.ai_answer_top_clients_line, c.clientName, c.transactionCount, formatMoney(c.totalAmount))
        }
        return context.getString(R.string.ai_answer_top_clients_header) + "\n" + lines
    }

    private suspend fun topOutProductsAnswer(q: String): String {
        val (from, to) = if (containsAny(q, TODAY_KEYWORDS)) todayRange() else monthRange()
        val summary = repository.reportSummary(from, to, topLimit = 5)
        if (summary.topOutProducts.isEmpty()) return context.getString(R.string.ai_answer_top_products_none)
        val lines = summary.topOutProducts.joinToString("\n") { p ->
            context.getString(R.string.ai_answer_top_products_line, p.productName, formatQuantity(p.totalQuantity), p.unit)
        }
        return context.getString(R.string.ai_answer_top_products_header) + "\n" + lines
    }

    private suspend fun stockInAnswer(q: String): String {
        val (from, to, labelRes) = periodFor(q)
        val summary = repository.reportSummary(from, to)
        return context.getString(
            R.string.ai_answer_stock_in,
            context.getString(labelRes),
            summary.stockInCount,
            formatQuantity(summary.stockInQty),
            formatMoney(summary.stockInAmount)
        )
    }

    private suspend fun stockOutAnswer(q: String): String {
        val (from, to, labelRes) = periodFor(q)
        val summary = repository.reportSummary(from, to)
        return context.getString(
            R.string.ai_answer_stock_out,
            context.getString(labelRes),
            summary.stockOutCount,
            formatQuantity(summary.stockOutQty),
            formatMoney(summary.stockOutAmount)
        )
    }

    private suspend fun returnsAnswer(q: String): String {
        val (from, to, labelRes) = periodFor(q)
        val summary = repository.reportSummary(from, to)
        val label = context.getString(labelRes)
        if (summary.returnCount == 0) return context.getString(R.string.ai_answer_returns_none, label)
        return context.getString(
            R.string.ai_answer_returns,
            label,
            summary.returnCount,
            formatQuantity(summary.returnQty),
            formatMoney(summary.returnAmount)
        )
    }

    private suspend fun generalSummary(): String {
        val value = repository.totalStockValue.first()
        val lowStock = repository.lowStockProducts.first()
        val (from, to) = monthRange()
        val summary = repository.reportSummary(from, to)
        return buildString {
            append(context.getString(R.string.ai_answer_general_header)).append("\n")
            append(context.getString(R.string.ai_answer_general_stock_value, formatMoney(value))).append("\n")
            append(context.getString(R.string.ai_answer_general_stock_in, formatMoney(summary.stockInAmount), summary.stockInCount)).append("\n")
            append(context.getString(R.string.ai_answer_general_stock_out, formatMoney(summary.stockOutAmount), summary.stockOutCount)).append("\n")
            append(context.getString(R.string.ai_answer_general_return, formatMoney(summary.returnAmount), summary.returnCount)).append("\n")
            append(context.getString(R.string.ai_answer_general_low_stock, lowStock.size)).append("\n")
            append(context.getString(R.string.ai_answer_general_footer))
        }
    }

    private fun periodFor(q: String): Triple<Long, Long, Int> = when {
        containsAny(q, TODAY_KEYWORDS) -> todayRange().let { Triple(it.first, it.second, R.string.ai_period_today) }
        containsAny(q, WEEK_KEYWORDS) -> weekRange().let { Triple(it.first, it.second, R.string.ai_period_week) }
        else -> monthRange().let { Triple(it.first, it.second, R.string.ai_period_month) }
    }

    private fun todayRange(): Pair<Long, Long> {
        val from = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }
        return from.timeInMillis to System.currentTimeMillis()
    }

    private fun weekRange(): Pair<Long, Long> {
        val from = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }
        return from.timeInMillis to System.currentTimeMillis()
    }

    private fun monthRange(): Pair<Long, Long> {
        val from = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }
        return from.timeInMillis to System.currentTimeMillis()
    }

    private companion object {
        val LOW_STOCK_KEYWORDS = listOf(
            "tugab", "kam qol", "past qoldiq", "low stock", "running low", "заканчива", "мало на складе",
            "kończy się", "mało towaru", "azalıyor", "tükeniyor", "закінчується", "мало на складі",
            "wenig auf lager", "geht zur neige", "niedrigen bestand"
        )
        val STOCK_VALUE_KEYWORDS = listOf(
            "umumiy qiymat", "necha pul", "qancha pul", "qiymati", "total value", "stock value", "worth",
            "общая стоимость", "стоимость склада", "wartość magazynu", "łączna wartość", "depo değeri",
            "toplam değer", "загальна вартість", "вартість складу", "lagerwert", "gesamtwert"
        )
        val TOP_CLIENTS_KEYWORDS = listOf(
            "faol mijoz", "top mijoz", "active client", "top client", "most active", "активные клиент",
            "лучшие клиент", "aktywni klienci", "najlepsi klienci", "aktif müşteri", "en çok müşteri",
            "активні клієнт", "найкращі клієнт", "aktive kunden", "beste kunden"
        )
        val RETURNS_KEYWORDS = listOf(
            "qayt", "return", "возврат", "zwrot", "iade", "повернен", "rückgabe", "zurückgegeben", "retoure"
        )
        val TOP_PRODUCTS_KEYWORDS = listOf(
            "eng ko'p berilgan", "eng ko'p sotilgan", "top mahsulot", "best-selling", "best selling",
            "top product", "most given", "most sold", "лучше продава", "топ товар", "najlepiej sprzedaj",
            "najczęściej wydawan", "en çok satılan", "en çok verilen", "найкраще продава", "meistverkauf", "bestseller"
        )
        val STOCK_IN_KEYWORDS = listOf(
            "kirim", "stock in", "поступлен", "przyjęcie", "przyjęto", "stok girişi", "надходження",
            "wareneingang", "eingang"
        )
        val STOCK_OUT_KEYWORDS = listOf(
            "chiqim", "berildi", "berdik", "stock out", "given", "issued", "отгруз", "выдан", "wydanie",
            "wydano", "çıkış", "verildi", "видача", "видано", "warenausgang", "ausgabe"
        )
        val TODAY_KEYWORDS = listOf("bugun", "today", "сегодня", "dzisiaj", "bugün", "сьогодні", "heute")
        val WEEK_KEYWORDS = listOf("hafta", "week", "недел", "tydzień", "тижд", "woche")
    }
}
