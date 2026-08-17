package com.hanfood.warehouse.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hanfood.warehouse.data.local.dao.ClientDao
import com.hanfood.warehouse.data.local.dao.ProductDao
import com.hanfood.warehouse.data.local.dao.TransactionDao
import com.hanfood.warehouse.data.local.entity.Client
import com.hanfood.warehouse.data.local.entity.Product
import com.hanfood.warehouse.data.local.entity.StockTransaction
import com.hanfood.warehouse.data.local.entity.TransactionItem

/**
 * Ilovaning yagona lokal ma'lumotlar bazasi. Internetga bog'liq emas — barcha
 * ombor ma'lumotlari shu qurilmada saqlanadi.
 */
@Database(
    entities = [Product::class, Client::class, StockTransaction::class, TransactionItem::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun clientDao(): ClientDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        private const val DB_NAME = "hanfood_warehouse.db"

        /**
         * v1 -> v2: mahsulot rasmi (products.image_path) va faktura nomi/biriktirilgan
         * fayllar (stock_transactions.title, stock_transactions.attachment_paths).
         * Mavjud foydalanuvchi ma'lumotlarini saqlab qolish uchun destructive emas,
         * qo'shimcha ustunlar bilan haqiqiy migratsiya.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN image_path TEXT")
                db.execSQL("ALTER TABLE stock_transactions ADD COLUMN title TEXT")
                db.execSQL("ALTER TABLE stock_transactions ADD COLUMN attachment_paths TEXT")
            }
        }

        /**
         * v2 -> v3: mijozning GPS joylashuvi (clients.latitude/longitude) — mijoz
         * qo'shishda "GPS joylashuvni olish" tugmasi bilan yoziladi, Google Maps
         * ilovasida ochish uchun ishlatiladi.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE clients ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE clients ADD COLUMN longitude REAL")
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build().also { instance = it }
            }
    }
}
