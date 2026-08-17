package com.hanfood.warehouse.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Yukni oladigan / qaytaradigan mijoz (do'kon, restoran, dilerlar va h.k.). */
@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val phone: String? = null,
    val address: String? = null,
    val note: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @ColumnInfo(name = "is_archived", defaultValue = "0")
    val isArchived: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
