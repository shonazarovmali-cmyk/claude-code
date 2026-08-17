package com.hanfood.warehouse.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Omborda saqlanadigan bitta mahsulot turi (masalan: "Palov guruchi 5kg").
 * [quantity] joriy qoldiq — har bir kirim/chiqim/qaytarish amalidan so'ng
 * [com.hanfood.warehouse.data.repository.WarehouseRepository] tomonidan yangilanadi.
 */
@Entity(
    tableName = "products",
    indices = [Index(value = ["barcode"], unique = true)]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    /** Shtrix-kod yoki QR-kod matni. Kod bo'lmasa null. */
    val barcode: String? = null,
    /** O'lchov birligi: dona, kg, quti, karobka, litr ... */
    val unit: String = "dona",
    @ColumnInfo(name = "quantity", defaultValue = "0")
    val quantity: Double = 0.0,
    /** Shu qiymatdan pastga tushsa "kam qolgan" deb hisoblanadi. */
    @ColumnInfo(name = "min_quantity", defaultValue = "0")
    val minQuantity: Double = 0.0,
    @ColumnInfo(name = "purchase_price", defaultValue = "0")
    val purchasePrice: Double = 0.0,
    @ColumnInfo(name = "sell_price", defaultValue = "0")
    val sellPrice: Double = 0.0,
    val category: String? = null,
    /**
     * Mahsulot rasmlari — ilova ichki xotirasidagi fayl yo'llari
     * (content:// emas), vergul bilan ajratilgan, birinchisi asosiy/muqova
     * rasm sifatida ishlatiladi. Bittadan o'ntagacha. [toAttachmentList] /
     * [toAttachmentPathsString] orqali List<String>'ga aylantiriladi.
     */
    @ColumnInfo(name = "image_paths")
    val imagePaths: String? = null,
    @ColumnInfo(name = "is_archived", defaultValue = "0")
    val isArchived: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
