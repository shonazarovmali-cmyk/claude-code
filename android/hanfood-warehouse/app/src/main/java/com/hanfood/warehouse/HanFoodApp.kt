package com.hanfood.warehouse

import android.app.Application
import com.hanfood.warehouse.ai.AiEngine
import com.hanfood.warehouse.ai.LocalAiAnalysisEngine
import com.hanfood.warehouse.data.local.AppDatabase
import com.hanfood.warehouse.data.repository.WarehouseRepository
import com.hanfood.warehouse.security.PinManager

/**
 * Ilova darajasidagi oddiy qo'lda DI (Hilt/Dagger ishlatilmagan — ilova
 * hajmi kichik, shuning uchun bitta joyda lazy singletonlar yetarli).
 */
class HanFoodApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: WarehouseRepository by lazy { WarehouseRepository(database) }
    val pinManager: PinManager by lazy { PinManager(this) }

    /**
     * AI yordamchi. Hozircha lokal tahlil motori ishlatiladi (internet shart
     * emas). Real LLM ulash uchun shu joyda [AiEngine]ning boshqa
     * implementatsiyasini qaytarish kifoya.
     */
    val aiEngine: AiEngine by lazy { LocalAiAnalysisEngine(this, repository) }
}
