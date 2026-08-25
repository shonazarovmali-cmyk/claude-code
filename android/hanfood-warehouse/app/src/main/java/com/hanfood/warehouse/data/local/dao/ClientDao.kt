package com.hanfood.warehouse.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hanfood.warehouse.data.local.entity.Client
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(client: Client): Long

    @Update
    suspend fun update(client: Client)

    @Delete
    suspend fun delete(client: Client)

    @Query("SELECT * FROM clients WHERE is_archived = 0 ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Client>>

    @Query(
        """
        SELECT * FROM clients
        WHERE is_archived = 0 AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%')
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun search(query: String): Flow<List<Client>>

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getById(id: Long): Client?

    @Query("SELECT * FROM clients WHERE id = :id")
    fun observeById(id: Long): Flow<Client?>

    @Query("SELECT COUNT(*) FROM clients WHERE is_archived = 0")
    fun observeActiveClientCount(): Flow<Int>
}
