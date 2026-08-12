package com.hanfood.warehouse.ai

import com.hanfood.warehouse.data.repository.WarehouseRepository
import com.hanfood.warehouse.util.formatMoney
import com.hanfood.warehouse.util.formatQuantity
import java.util.Calendar
import kotlinx.coroutines.flow.first

/**
 * Ombor ma'lumotlari asosida ishlaydigan, internetga bog'liq bo'lmagan
 * qoida-asosidagi ("rule-based") tahlil motori. Haqiqiy neyron tarmoq emas,
 * lekin foydalanuvchi uchun tez va ishonchli — barcha javoblar to'g'ridan-to'g'ri
 * mahalliy bazadagi haqiqiy raqamlardan hisoblanadi (gallyutsinatsiya yo'q).
 */
class LocalAiAnalysisEngine(private val repository: WarehouseRepository) : AiEngine {

    override fun suggestedQuestions(): List<String> = listOf(
        "Qaysi mahsulotlar tugab qolyapti?",
        "Ombor umumiy qiymati qancha?",
        "Bu oy qancha yuk berildi?",
        "Eng faol mijozlar kim?",
        "Eng ko'p berilgan mahsulotlar qaysilar?",
        "Bu oy qancha yuk qaytdi?"
    )

    override suspend fun answer(question: String): String {
        val q = question.lowercase().trim()
        return when {
            containsAny(q, "tugab", "kam qol", "yetish", "qoldiq kam") -> lowStockAnswer()
            containsAny(q, "umumiy qiymat", "necha pul", "qancha pul", "qiymat") -> stockValueAnswer()
            containsAny(q, "faol mijoz", "eng ko'p oluvchi", "top mijoz") -> topClientsAnswer()
            containsAny(q, "qayt") -> returnsAnswer(q)
            containsAny(q, "eng ko'p berilgan", "eng ko'p sotilgan", "top mahsulot", "qaysi mahsulot ko'p") -> topOutProductsAnswer(q)
            containsAny(q, "kirim") -> stockInAnswer(q)
            containsAny(q, "chiqim", "berildi", "berdik") -> stockOutAnswer(q)
            else -> generalSummary()
        }
    }

    private fun containsAny(text: String, vararg needles: String) = needles.any { text.contains(it) }

    private suspend fun lowStockAnswer(): String {
        val lowStock = repository.lowStockProducts.first()
        if (lowStock.isEmpty()) return "Hozircha kam qolgan mahsulot yo'q — barcha mahsulotlar minimal chegaradan yuqorida."
        val lines = lowStock.take(8).joinToString("\n") { p ->
            "• ${p.name}: ${formatQuantity(p.quantity)} ${p.unit} (minimal: ${formatQuantity(p.minQuantity)} ${p.unit})"
        }
        val extra = if (lowStock.size > 8) "\n... va yana ${lowStock.size - 8} ta mahsulot" else ""
        return "Kam qolgan mahsulotlar (${lowStock.size} ta):\n$lines$extra\n\nTavsiya: ta'minotchidan yangi kirim buyurtma qiling."
    }

    private suspend fun stockValueAnswer(): String {
        val value = repository.totalStockValue.first()
        val units = repository.totalUnits.first()
        return "Omborda hozir jami ${formatQuantity(units)} birlik mahsulot bor, umumiy tannarx bo'yicha qiymati taxminan ${formatMoney(value)}."
    }

    private suspend fun topClientsAnswer(): String {
        val (from, to) = monthRange()
        val summary = repository.reportSummary(from, to, topLimit = 5)
        if (summary.topClients.isEmpty()) return "Shu oyda hali mijozlarga yuk berilmagan."
        val lines = summary.topClients.joinToString("\n") { c ->
            "• ${c.clientName}: ${c.transactionCount} ta faktura, ${formatMoney(c.totalAmount)}"
        }
        return "Shu oydagi eng faol mijozlar:\n$lines"
    }

    private suspend fun topOutProductsAnswer(q: String): String {
        val (from, to) = if (containsAny(q, "bugun")) todayRange() else monthRange()
        val summary = repository.reportSummary(from, to, topLimit = 5)
        if (summary.topOutProducts.isEmpty()) return "Bu davrda mijozlarga hali mahsulot berilmagan."
        val lines = summary.topOutProducts.joinToString("\n") { p ->
            "• ${p.productName}: ${formatQuantity(p.totalQuantity)} ${p.unit}"
        }
        return "Eng ko'p berilgan mahsulotlar:\n$lines"
    }

    private suspend fun stockInAnswer(q: String): String {
        val (from, to, label) = periodFor(q)
        val summary = repository.reportSummary(from, to)
        return "$label kirim: ${summary.stockInCount} ta faktura, ${formatQuantity(summary.stockInQty)} birlik, umumiy summa ${formatMoney(summary.stockInAmount)}."
    }

    private suspend fun stockOutAnswer(q: String): String {
        val (from, to, label) = periodFor(q)
        val summary = repository.reportSummary(from, to)
        return "$label chiqim (mijozlarga berilgan): ${summary.stockOutCount} ta faktura, ${formatQuantity(summary.stockOutQty)} birlik, umumiy summa ${formatMoney(summary.stockOutAmount)}."
    }

    private suspend fun returnsAnswer(q: String): String {
        val (from, to, label) = periodFor(q)
        val summary = repository.reportSummary(from, to)
        if (summary.returnCount == 0) return "$label hech qanday qaytarish bo'lmagan."
        return "$label qaytarilgan yuk: ${summary.returnCount} ta faktura, ${formatQuantity(summary.returnQty)} birlik, umumiy summa ${formatMoney(summary.returnAmount)}."
    }

    private suspend fun generalSummary(): String {
        val value = repository.totalStockValue.first()
        val lowStock = repository.lowStockProducts.first()
        val (from, to) = monthRange()
        val summary = repository.reportSummary(from, to)
        return buildString {
            append("Qisqacha holat:\n")
            append("• Ombor qiymati: ${formatMoney(value)}\n")
            append("• Bu oy kirim: ${formatMoney(summary.stockInAmount)} (${summary.stockInCount} faktura)\n")
            append("• Bu oy chiqim: ${formatMoney(summary.stockOutAmount)} (${summary.stockOutCount} faktura)\n")
            append("• Bu oy qaytarish: ${formatMoney(summary.returnAmount)} (${summary.returnCount} faktura)\n")
            append("• Kam qolgan mahsulotlar: ${lowStock.size} ta\n\n")
            append("Aniqroq javob uchun savolni tugmalardan tanlang yoki masalan \"Qaysi mahsulotlar tugab qolyapti?\" deb so'rang.")
        }
    }

    private fun periodFor(q: String): Triple<Long, Long, String> = when {
        containsAny(q, "bugun") -> todayRange().let { Triple(it.first, it.second, "Bugungi") }
        containsAny(q, "hafta") -> weekRange().let { Triple(it.first, it.second, "Shu haftadagi") }
        else -> monthRange().let { Triple(it.first, it.second, "Shu oydagi") }
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
}
