package com.hanfood.warehouse.data.local.entity

/** Faktura qatorini mahsulot nomi/o'lchov birligi bilan birga ko'rsatish uchun (UI qulayligi). */
data class TransactionItemDetail(
    val id: Long,
    val transactionId: Long,
    val productId: Long,
    val productName: String,
    val unit: String,
    val quantity: Double,
    val unitPrice: Double
) {
    val lineTotal: Double get() = quantity * unitPrice
}

/** Bitta mahsulot bo'yicha davr ichidagi kirim/chiqim/qaytarish yig'indisi (hisobotlar uchun). */
data class ProductMovementSummary(
    val productId: Long,
    val productName: String,
    val unit: String,
    val totalQuantity: Double
)

/** Mijoz bo'yicha yig'indi statistika (eng faol mijozlar hisoboti uchun). */
data class ClientActivitySummary(
    val clientId: Long,
    val clientName: String,
    val transactionCount: Int,
    val totalAmount: Double
)
