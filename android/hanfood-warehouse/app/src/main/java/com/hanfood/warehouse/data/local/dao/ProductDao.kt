package com.hanfood.warehouse.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hanfood.warehouse.data.local.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(product: Product): Long

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("SELECT * FROM products WHERE is_archived = 0 ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Product>>

    @Query(
        """
        SELECT * FROM products
        WHERE is_archived = 0 AND (name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%')
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun search(query: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): Product?

    @Query("SELECT * FROM products WHERE id = :id")
    fun observeById(id: Long): Flow<Product?>

    @Query("SELECT * FROM products WHERE barcode = :barcode AND is_archived = 0 LIMIT 1")
    suspend fun getByBarcode(barcode: String): Product?

    @Query(
        """
        SELECT * FROM products
        WHERE is_archived = 0 AND min_quantity > 0 AND quantity <= min_quantity
        ORDER BY (quantity - min_quantity) ASC
        """
    )
    fun observeLowStock(): Flow<List<Product>>

    @Query(
        """
        SELECT * FROM products
        WHERE is_archived = 0 AND min_quantity > 0 AND quantity <= min_quantity
        ORDER BY (quantity - min_quantity) ASC
        """
    )
    suspend fun getLowStockOnce(): List<Product>

    @Query("UPDATE products SET quantity = quantity + :delta WHERE id = :id")
    suspend fun adjustQuantity(id: Long, delta: Double)

    @Query("SELECT COALESCE(SUM(quantity * purchase_price), 0) FROM products WHERE is_archived = 0")
    fun observeTotalStockValue(): Flow<Double>

    @Query("SELECT COUNT(*) FROM products WHERE is_archived = 0")
    fun observeActiveProductCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM products WHERE is_archived = 0")
    fun observeTotalUnits(): Flow<Double>
}
