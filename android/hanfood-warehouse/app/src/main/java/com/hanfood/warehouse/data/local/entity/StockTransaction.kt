package com.hanfood.warehouse.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Ombor harakati turi — faktura qaysi jarayonga tegishli ekanini bildiradi. */
enum class TransactionType {
    /** Kirim — ta'minotchidan omborga yuk kelishi. */
    STOCK_IN,

    /** Chiqim — mijozga yuk berish. */
    STOCK_OUT,

    /** Qaytarish — mijozdan yuk qaytib kelishi (omborga qayta qo'shiladi). */
    RETURN
}

/**
 * Bitta ombor harakatining "sarlavhasi" (faktura). Har bir harakat bir nechta
 * [TransactionItem] qatoriga ega bo'lishi mumkin (bir nechta mahsulot bitta
 * faktura ichida).
 */
@Entity(
    tableName = "stock_transactions",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["client_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["client_id"]), Index(value = ["type"]), Index(value = ["date"])]
)
data class StockTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: TransactionType,
    /** STOCK_OUT / RETURN uchun mijoz. STOCK_IN uchun odatda null. */
    @ColumnInfo(name = "client_id")
    val clientId: Long? = null,
    /** STOCK_IN uchun ta'minotchi nomi (erkin matn, alohida jadval shart emas). */
    @ColumnInfo(name = "supplier_name")
    val supplierName: String? = null,
    /** Faktura raqami — avtomatik yaratiladigan, inson o'qiy oladigan format (masalan CHQ-20260812-0001). */
    @ColumnInfo(name = "invoice_number")
    val invoiceNumber: String,
    /** Foydalanuvchi kiritadigan ixtiyoriy nom (masalan "Toshkent filiali uchun yuk"). */
    val title: String? = null,
    val date: Long = System.currentTimeMillis(),
    val note: String? = null,
    /** Ilova ichki xotirasidagi biriktirilgan fayl/rasm yo'llari, vergul bilan ajratilgan. */
    @ColumnInfo(name = "attachment_paths")
    val attachmentPaths: String? = null,
    @ColumnInfo(name = "total_amount", defaultValue = "0")
    val totalAmount: Double = 0.0
)

/** [StockTransaction.attachmentPaths] ni ro'yxatga aylantiradi va aksincha. */
fun String?.toAttachmentList(): List<String> = this?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()

fun List<String>.toAttachmentPathsString(): String? = this.filter { it.isNotBlank() }.joinToString(",").ifBlank { null }
