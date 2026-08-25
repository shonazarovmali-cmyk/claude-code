package com.hanfood.warehouse.ui.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Skanerlash ekrani bilan uni chaqirgan ekran o'rtasidagi eng oddiy "pochta
 * qutisi". Navigation-Compose orqali natijani argument sifatida uzatish
 * ko'plab ekranlar uchun ortiqcha murakkablik keltirgani sababli, o'rniga bitta
 * umumiy oqim (StateFlow) ishlatiladi: skaner kodni topganda shu yerga yozadi,
 * chaqiruvchi ekran esa uni kuzatib, iste'mol qilingach tozalaydi.
 */
object ScannerBus {
    private val _lastScanned = MutableStateFlow<String?>(null)
    val lastScanned: StateFlow<String?> = _lastScanned

    fun emit(code: String) {
        _lastScanned.value = code
    }

    fun consume() {
        _lastScanned.value = null
    }
}
