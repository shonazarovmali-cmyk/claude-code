package com.hanfood.warehouse.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hanfood.warehouse.data.local.entity.ClientActivitySummary
import com.hanfood.warehouse.data.local.entity.ProductMovementSummary
import com.hanfood.warehouse.data.local.entity.StockTransaction
import com.hanfood.warehouse.data.local.entity.TransactionItem
import com.hanfood.warehouse.data.local.entity.TransactionItemDetail
import com.hanfood.warehouse.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransaction(transaction: StockTransaction): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertItems(items: List<TransactionItem>): List<Long>

    /**
     * Faktura (StockTransaction) o'chirilishi kerak bo'lganda mahsulot
     * qoldig'iga qanday ta'sir qilganini bekor qilish uchun qatorlarini
     * bir martalik ro'yxat sifatida o'qiydi (Flow emas).
     */
    @Query("SELECT * FROM transaction_items WHERE transaction_id = :transactionId")
    suspend fun getItemsForTransactionOnce(transactionId: Long): List<TransactionItem>

    /** Faktura o'chirilganda tegishli qatorlar (transaction_items) FK CASCADE orqali avtomatik o'chadi. */
    @Delete
    suspend fun deleteTransaction(transaction: StockTransaction)

    @Query("SELECT * FROM stock_transactions ORDER BY date DESC")
    fun observeAll(): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE type = :type ORDER BY date DESC")
    fun observeByType(type: TransactionType): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE client_id = :clientId ORDER BY date DESC")
    fun observeByClient(clientId: Long): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions ORDER BY date DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE id = :id")
    suspend fun getById(id: Long): StockTransaction?

    @Query("SELECT * FROM stock_transactions WHERE id = :id")
    fun observeById(id: Long): Flow<StockTransaction?>

    @Query(
        """
        SELECT ti.id AS id, ti.transaction_id AS transactionId, ti.product_id AS productId,
               p.name AS productName, p.unit AS unit, ti.quantity AS quantity, ti.unit_price AS unitPrice
        FROM transaction_items ti
        INNER JOIN products p ON p.id = ti.product_id
        WHERE ti.transaction_id = :transactionId
        ORDER BY ti.id ASC
        """
    )
    fun observeItemsForTransaction(transactionId: Long): Flow<List<TransactionItemDetail>>

    @Query("SELECT COUNT(*) FROM stock_transactions WHERE type = :type")
    suspend fun countByType(type: TransactionType): Int

    // ---- Hisobotlar uchun so'rovlar ----

    @Query(
        """
        SELECT COALESCE(SUM(ti.quantity * ti.unit_price), 0)
        FROM stock_transactions t INNER JOIN transaction_items ti ON ti.transaction_id = t.id
        WHERE t.type = :type AND t.date BETWEEN :from AND :to
        """
    )
    suspend fun totalAmountByType(type: TransactionType, from: Long, to: Long): Double

    @Query(
        """
        SELECT COALESCE(SUM(ti.quantity), 0)
        FROM stock_transactions t INNER JOIN transaction_items ti ON ti.transaction_id = t.id
        WHERE t.type = :type AND t.date BETWEEN :from AND :to
        """
    )
    suspend fun totalQuantityByType(type: TransactionType, from: Long, to: Long): Double

    @Query("SELECT COUNT(*) FROM stock_transactions WHERE type = :type AND date BETWEEN :from AND :to")
    suspend fun countByTypeInRange(type: TransactionType, from: Long, to: Long): Int

    @Query(
        """
        SELECT p.id AS productId, p.name AS productName, p.unit AS unit, SUM(ti.quantity) AS totalQuantity,
               p.image_paths AS imagePaths
        FROM transaction_items ti
        INNER JOIN stock_transactions t ON t.id = ti.transaction_id
        INNER JOIN products p ON p.id = ti.product_id
        WHERE t.type = :type AND t.date BETWEEN :from AND :to
        GROUP BY p.id
        ORDER BY totalQuantity DESC
        LIMIT :limit
        """
    )
    suspend fun topProductsByType(
        type: TransactionType,
        from: Long,
        to: Long,
        limit: Int
    ): List<ProductMovementSummary>

    @Query(
        """
        SELECT c.id AS clientId, c.name AS clientName, COUNT(DISTINCT t.id) AS transactionCount,
               COALESCE(SUM(ti.quantity * ti.unit_price), 0) AS totalAmount
        FROM stock_transactions t
        INNER JOIN clients c ON c.id = t.client_id
        LEFT JOIN transaction_items ti ON ti.transaction_id = t.id
        WHERE t.type = 'STOCK_OUT' AND t.date BETWEEN :from AND :to
        GROUP BY c.id
        ORDER BY totalAmount DESC
        LIMIT :limit
        """
    )
    suspend fun topClients(from: Long, to: Long, limit: Int): List<ClientActivitySummary>
}
