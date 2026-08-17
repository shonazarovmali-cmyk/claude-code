package com.hanfood.warehouse.ui.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * [ScannerBus] bilan bir xil naqsh — xarita-tanlash ekrani (`LocationPickerScreen`)
 * bilan uni chaqirgan `ClientEditScreen` o'rtasidagi "pochta qutisi". Ikki
 * yo'nalishda ishlaydi: chaqiruvchi ekran boshlang'ich koordinatani
 * [setInitial] bilan yozadi, tanlash ekrani uni [consumeInitial] bilan
 * o'qiydi; foydalanuvchi xaritada joyni tasdiqlagach, natija [emitResult]
 * orqali yoziladi va chaqiruvchi ekran uni kuzatib iste'mol qiladi.
 */
object LocationPickerBus {

    data class LatLng(val latitude: Double, val longitude: Double)

    private var pendingInitial: LatLng? = null

    private val _result = MutableStateFlow<LatLng?>(null)
    val result: StateFlow<LatLng?> = _result

    fun setInitial(latitude: Double?, longitude: Double?) {
        pendingInitial = if (latitude != null && longitude != null) LatLng(latitude, longitude) else null
    }

    fun consumeInitial(): LatLng? {
        val value = pendingInitial
        pendingInitial = null
        return value
    }

    fun emitResult(latitude: Double, longitude: Double) {
        _result.value = LatLng(latitude, longitude)
    }

    fun consumeResult() {
        _result.value = null
    }
}
