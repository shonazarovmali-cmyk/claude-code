package com.hanfood.warehouse.data.repository

import androidx.room.withTransaction
import com.hanfood.warehouse.data.local.AppDatabase
import com.hanfood.warehouse.data.local.entity.Client
import com.hanfood.warehouse.data.local.entity.ClientActivitySummary
import com.hanfood.warehouse.data.local.entity.Product
import com.hanfood.warehouse.data.local.entity.ProductMovementSummary
import com.hanfood.warehouse.data.local.entity.StockTransaction
import com.hanfood.warehouse.data.local.entity.TransactionItem
import com.hanfood.warehouse.data.local.entity.TransactionItemDetail
import com.hanfood.warehouse.data.local.entity.TransactionType
import com.hanfood.warehouse.data.local.entity.toAttachmentPathsString
import com.hanfood.warehouse.util.ExcelProductRow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow

/** Bitta savatcha qatori: qaysi mahsulotdan qancha miqdor va narxda. */
data class CartLine(
    val productId: Long,
    val productName: String,
    val unit: String,
    val quantity: Double,
    val unitPrice: Double
)

/** Excel'dan import natijasi — Sozlamalar/Mahsulotlar ekranida xulosa ko'rsatish uchun. */
data class ExcelImportResult(
    val productsCreated: Int,
    val productsMatched: Int,
    val stockLines: Int,
    val transactionId: Long?
)

/** [WarehouseRepository.recordStockOut] uchun mahsulot yetarli emasligi haqidagi xatolik. */
class InsufficientStockException(val product: Product, val requested: Double) :
    Exception("\"${product.name}\" uchun omborda yetarli miqdor yo'q (bor: ${product.quantity}, so'ralgan: $requested)")

/**
 * Ombor bilan bog'liq barcha yozish/o'qish amallarining yagona darvozasi.
 * Kirim/chiqim/qaytarish amallari Room tranzaksiyasi ichida bajariladi — shu
 * bilan mahsulot qoldig'i va faktura yozuvi doimo mos keladi.
 */
class WarehouseRepository(private val db: AppDatabase) {

    private val invoiceDateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)

    // ---- Mahsulotlar ----

    val products: Flow<List<Product>> = db.productDao().observeAll()
    val lowStockProducts: Flow<List<Product>> = db.productDao().observeLowStock()
    val totalStockValue: Flow<Double> = db.productDao().observeTotalStockValue()
    val totalUnits: Flow<Double> = db.productDao().observeTotalUnits()
    val activeProductCount: Flow<Int> = db.productDao().observeActiveProductCount()

    fun searchProducts(query: String): Flow<List<Product>> = db.productDao().search(query)

    fun observeProduct(id: Long): Flow<Product?> = db.productDao().observeById(id)

    suspend fun getProduct(id: Long): Product? = db.productDao().getById(id)

    suspend fun getProductByBarcode(barcode: String): Product? = db.productDao().getByBarcode(barcode)

    suspend fun upsertProduct(product: Product): Long =
        if (product.id == 0L) db.productDao().insert(product) else {
            db.productDao().update(product); product.id
        }

    suspend fun archiveProduct(product: Product) {
        db.productDao().update(product.copy(isArchived = true))
    }

    /**
     * Excel jadvaldan mahsulotlarni ombor bilan bir vaqtda import qiladi:
     * har bir qator uchun shtrix-kod (yoki topilmasa nomi) bo'yicha mavjud
     * mahsulot izlanadi — topilsa unga, topilmasa yangi yaratilgan
     * mahsulotga miqdor qo'shiladi. Barcha qatorlar bitta "kirim" (STOCK_IN)
     * fakturasi sifatida yoziladi — shu bilan umumiy summa avtomatik
     * hisoblanadi va qoldiq/hisobotlarda ko'rinadi.
     */
    suspend fun importProductsFromExcel(rows: List<ExcelProductRow>, sourceLabel: String?): ExcelImportResult {
        if (rows.isEmpty()) return ExcelImportResult(0, 0, 0, null)
        var created = 0
        var matched = 0
        val lines = mutableListOf<CartLine>()
        for (row in rows) {
            val existing = row.barcode?.let { db.productDao().getByBarcode(it) }
                ?: db.productDao().getByNameIgnoreCase(row.name)
            val productId: Long
            if (existing != null) {
                productId = existing.id
                matched++
            } else {
                productId = db.productDao().insert(
                    Product(
                        name = row.name,
                        barcode = row.barcode,
                        unit = row.unit,
                        quantity = 0.0,
                        minQuantity = row.minQuantity,
                        purchasePrice = row.purchasePrice,
                        sellPrice = row.sellPrice
                    )
                )
                created++
            }
            if (row.quantity > 0) {
                lines.add(
                    CartLine(
                        productId = productId,
                        productName = row.name,
                        unit = row.unit,
                        quantity = row.quantity,
                        unitPrice = row.purchasePrice
                    )
                )
            }
        }
        val transactionId = if (lines.isNotEmpty()) {
            recordStockIn(supplierName = null, note = null, lines = lines, title = sourceLabel)
        } else {
            null
        }
        return ExcelImportResult(created, matched, lines.size, transactionId)
    }

    // ---- Mijozlar ----

    val clients: Flow<List<Client>> = db.clientDao().observeAll()
    val activeClientCount: Flow<Int> = db.clientDao().observeActiveClientCount()

    fun searchClients(query: String): Flow<List<Client>> = db.clientDao().search(query)

    fun observeClient(id: Long): Flow<Client?> = db.clientDao().observeById(id)

    suspend fun getClient(id: Long): Client? = db.clientDao().getById(id)

    suspend fun upsertClient(client: Client): Long =
        if (client.id == 0L) db.clientDao().insert(client) else {
            db.clientDao().update(client); client.id
        }

    suspend fun archiveClient(client: Client) {
        db.clientDao().update(client.copy(isArchived = true))
    }

    // ---- Fakturalar / harakatlar ----

    fun recentTransactions(limit: Int = 20): Flow<List<StockTransaction>> = db.transactionDao().observeRecent(limit)

    val allTransactions: Flow<List<StockTransaction>> = db.transactionDao().observeAll()

    fun transactionsByType(type: TransactionType): Flow<List<StockTransaction>> = db.transactionDao().observeByType(type)

    fun transactionsForClient(clientId: Long): Flow<List<StockTransaction>> = db.transactionDao().observeByClient(clientId)

    fun observeTransaction(id: Long): Flow<StockTransaction?> = db.transactionDao().observeById(id)

    fun itemsForTransaction(transactionId: Long): Flow<List<TransactionItemDetail>> =
        db.transactionDao().observeItemsForTransaction(transactionId)

    /** Kirim — ta'minotchidan yuk qabul qilish. Barcha qatorlar qoldiqqa qo'shiladi. */
    suspend fun recordStockIn(
        supplierName: String?,
        note: String?,
        lines: List<CartLine>,
        title: String? = null,
        attachmentPaths: List<String> = emptyList()
    ): Long = recordMovement(
        TransactionType.STOCK_IN,
        clientId = null,
        supplierName = supplierName,
        note = note,
        lines = lines,
        title = title,
        attachmentPaths = attachmentPaths
    )

    /** Chiqim — mijozga yuk berish. Qoldiq yetarli bo'lmasa [InsufficientStockException] otiladi. */
    suspend fun recordStockOut(
        clientId: Long,
        note: String?,
        lines: List<CartLine>,
        title: String? = null,
        attachmentPaths: List<String> = emptyList()
    ): Long = recordMovement(
        TransactionType.STOCK_OUT,
        clientId = clientId,
        supplierName = null,
        note = note,
        lines = lines,
        title = title,
        attachmentPaths = attachmentPaths
    )

    /** Qaytarish — mijozdan yuk qaytishi. Barcha qatorlar qoldiqqa qayta qo'shiladi. */
    suspend fun recordReturn(
        clientId: Long,
        note: String?,
        lines: List<CartLine>,
        title: String? = null,
        attachmentPaths: List<String> = emptyList()
    ): Long = recordMovement(
        TransactionType.RETURN,
        clientId = clientId,
        supplierName = null,
        note = note,
        lines = lines,
        title = title,
        attachmentPaths = attachmentPaths
    )

    private suspend fun recordMovement(
        type: TransactionType,
        clientId: Long?,
        supplierName: String?,
        note: String?,
        lines: List<CartLine>,
        title: String? = null,
        attachmentPaths: List<String> = emptyList()
    ): Long = db.withTransaction {
        require(lines.isNotEmpty()) { "Faktura kamida bitta mahsulot qatorini o'z ichiga olishi kerak" }

        // STOCK_OUT uchun avval barcha mahsulotlarning qoldig'i yetarli ekanini tekshiramiz,
        // shundan keyingina yozish amalga oshiriladi (hech qanday qisman yangilanish bo'lmasligi uchun).
        if (type == TransactionType.STOCK_OUT) {
            for (line in lines) {
                val product = db.productDao().getById(line.productId)
                    ?: error("Mahsulot topilmadi: ${line.productId}")
                if (product.quantity < line.quantity) {
                    throw InsufficientStockException(product, line.quantity)
                }
            }
        }

        val invoiceNumber = generateInvoiceNumber(type)
        val totalAmount = lines.sumOf { it.quantity * it.unitPrice }
        val transactionId = db.transactionDao().insertTransaction(
            StockTransaction(
                type = type,
                clientId = clientId,
                supplierName = supplierName,
                invoiceNumber = invoiceNumber,
                title = title?.trim()?.ifBlank { null },
                note = note,
                attachmentPaths = attachmentPaths.toAttachmentPathsString(),
                totalAmount = totalAmount
            )
        )

        db.transactionDao().insertItems(
            lines.map { line ->
                TransactionItem(
                    transactionId = transactionId,
                    productId = line.productId,
                    quantity = line.quantity,
                    unitPrice = line.unitPrice
                )
            }
        )

        val sign = if (type == TransactionType.STOCK_OUT) -1.0 else 1.0
        for (line in lines) {
            db.productDao().adjustQuantity(line.productId, sign * line.quantity)
        }

        transactionId
    }

    /**
     * Fakturani (kirim/chiqim/qaytarish) butunlay o'chiradi va mahsulot
     * qoldig'iga qilgan ta'sirini bekor qiladi: STOCK_IN/RETURN uchun
     * qo'shilgan miqdor ayiriladi, STOCK_OUT uchun ayirilgan miqdor qayta
     * qo'shiladi. Faktura qatorlari (transaction_items) FK CASCADE orqali
     * avtomatik o'chadi.
     */
    suspend fun deleteTransaction(transactionId: Long) = db.withTransaction {
        val dao = db.transactionDao()
        val transaction = dao.getById(transactionId) ?: return@withTransaction
        val items = dao.getItemsForTransactionOnce(transactionId)
        val sign = if (transaction.type == TransactionType.STOCK_OUT) 1.0 else -1.0
        for (item in items) {
            db.productDao().adjustQuantity(item.productId, sign * item.quantity)
        }
        dao.deleteTransaction(transaction)
    }

    private suspend fun generateInvoiceNumber(type: TransactionType): String {
        val prefix = when (type) {
            TransactionType.STOCK_IN -> "KIR"
            TransactionType.STOCK_OUT -> "CHQ"
            TransactionType.RETURN -> "QAY"
        }
        val countSoFar = db.transactionDao().countByType(type)
        val datePart = invoiceDateFormat.format(Date())
        return "$prefix-$datePart-${(countSoFar + 1).toString().padStart(4, '0')}"
    }

    /**
     * Oxirgi 30 kunda eng ko'p sotilgan (chiqim qilingan) mahsulotlar —
     * bosh sahifadagi "eng aktiv tovarlar" bannerida ko'rsatish uchun.
     */
    suspend fun topSellingProducts(limit: Int = 8): List<ProductMovementSummary> {
        val to = System.currentTimeMillis()
        val from = to - 30L * 24 * 60 * 60 * 1000
        return db.transactionDao().topProductsByType(TransactionType.STOCK_OUT, from, to, limit)
    }

    // ---- Hisobotlar ----

    suspend fun reportSummary(from: Long, to: Long, topLimit: Int = 5): ReportSummary {
        val dao = db.transactionDao()
        return ReportSummary(
            stockInAmount = dao.totalAmountByType(TransactionType.STOCK_IN, from, to),
            stockInQty = dao.totalQuantityByType(TransactionType.STOCK_IN, from, to),
            stockInCount = dao.countByTypeInRange(TransactionType.STOCK_IN, from, to),
            stockOutAmount = dao.totalAmountByType(TransactionType.STOCK_OUT, from, to),
            stockOutQty = dao.totalQuantityByType(TransactionType.STOCK_OUT, from, to),
            stockOutCount = dao.countByTypeInRange(TransactionType.STOCK_OUT, from, to),
            returnAmount = dao.totalAmountByType(TransactionType.RETURN, from, to),
            returnQty = dao.totalQuantityByType(TransactionType.RETURN, from, to),
            returnCount = dao.countByTypeInRange(TransactionType.RETURN, from, to),
            topOutProducts = dao.topProductsByType(TransactionType.STOCK_OUT, from, to, topLimit),
            topInProducts = dao.topProductsByType(TransactionType.STOCK_IN, from, to, topLimit),
            topClients = dao.topClients(from, to, topLimit),
            lowStock = db.productDao().getLowStockOnce()
        )
    }
}

data class ReportSummary(
    val stockInAmount: Double,
    val stockInQty: Double,
    val stockInCount: Int,
    val stockOutAmount: Double,
    val stockOutQty: Double,
    val stockOutCount: Int,
    val returnAmount: Double,
    val returnQty: Double,
    val returnCount: Int,
    val topOutProducts: List<ProductMovementSummary>,
    val topInProducts: List<ProductMovementSummary>,
    val topClients: List<ClientActivitySummary>,
    val lowStock: List<Product>
)
