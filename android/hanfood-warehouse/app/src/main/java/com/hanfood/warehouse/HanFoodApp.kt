package com.hanfood.warehouse

import android.app.Application
import com.hanfood.warehouse.data.local.AppDatabase
import com.hanfood.warehouse.data.repository.WarehouseRepository
import com.hanfood.warehouse.security.PinManager
import java.io.File
import org.osmdroid.config.Configuration

/**
 * Ilova darajasidagi oddiy qo'lda DI (Hilt/Dagger ishlatilmagan — ilova
 * hajmi kichik, shuning uchun bitta joyda lazy singletonlar yetarli).
 */
class HanFoodApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: WarehouseRepository by lazy { WarehouseRepository(database) }
    val pinManager: PinManager by lazy { PinManager(this) }

    override fun onCreate() {
        super.onCreate()
        // osmdroid (xaritadan joylashuv tanlash ekrani uchun) — keshni ilovaning
        // shaxsiy papkasiga yo'naltiramiz (WRITE_EXTERNAL_STORAGE shart emas) va
        // OSM plitka serveri siyosatiga ko'ra alohida User-Agent qo'yamiz.
        val osmConfig = Configuration.getInstance()
        val osmBaseDir = File(cacheDir, "osmdroid")
        osmConfig.osmdroidBasePath = osmBaseDir
        osmConfig.osmdroidTileCache = File(osmBaseDir, "tiles")
        osmConfig.userAgentValue = packageName
    }
}
