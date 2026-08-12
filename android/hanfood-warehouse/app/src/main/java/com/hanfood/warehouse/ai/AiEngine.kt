package com.hanfood.warehouse.ai

/**
 * AI yordamchi mexanizmi uchun umumiy interfeys. Hozircha [LocalAiAnalysisEngine]
 * lokal (internetsiz) qoida-asosidagi tahlil qiladi. Kelajakda haqiqiy LLM
 * (masalan Claude API) ulanmoqchi bo'lsangiz, shu interfeysni amalga
 * oshiruvchi yangi klass yozib, [com.hanfood.warehouse.HanFoodApp]da
 * almashtirish kifoya — ekran va ViewModel kodini o'zgartirish shart emas.
 */
interface AiEngine {
    /** Foydalanuvchiga taklif qilinadigan tayyor savollar (chip'lar uchun). */
    fun suggestedQuestions(): List<String>

    /** Erkin matnli savolga javob qaytaradi. */
    suspend fun answer(question: String): String
}
