package com.hanfood.warehouse.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Faktura ichidagi bitta mahsulot qatori: qaysi mahsulotdan qancha miqdor. */
@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = StockTransaction::class,
            parentColumns = ["id"],
            childColumns = ["transaction_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["transaction_id"]), Index(value = ["product_id"])]
)
data class TransactionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "transaction_id")
    val transactionId: Long,
    @ColumnInfo(name = "product_id")
    val productId: Long,
    val quantity: Double,
    @ColumnInfo(name = "unit_price")
    val unitPrice: Double
)
