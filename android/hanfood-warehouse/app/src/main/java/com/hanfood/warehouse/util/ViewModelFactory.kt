package com.hanfood.warehouse.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Hilt/Dagger ishlatilmagani uchun ViewModel'larni qo'lda yaratish uchun
 * yordamchi factory. Har bir ekran o'zining ViewModel'ini shu orqali,
 * repository'ni argument sifatida uzatib yaratadi.
 */
class GenericViewModelFactory(private val creator: () -> ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = creator() as T
}
