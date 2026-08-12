package com.hanfood.warehouse.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanfood.warehouse.data.repository.ReportSummary
import com.hanfood.warehouse.data.repository.WarehouseRepository
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ReportPeriod(val label: String) {
    TODAY("Bugun"),
    WEEK("Shu hafta"),
    MONTH("Shu oy"),
    ALL_TIME("Barcha vaqt")
}

data class ReportsUiState(
    val period: ReportPeriod = ReportPeriod.MONTH,
    val summary: ReportSummary? = null,
    val loading: Boolean = true
)

class ReportsViewModel(private val repository: WarehouseRepository) : ViewModel() {

    private val _state = MutableStateFlow(ReportsUiState())
    val state: StateFlow<ReportsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun selectPeriod(period: ReportPeriod) {
        _state.value = _state.value.copy(period = period)
        load()
    }

    private fun load() {
        val period = _state.value.period
        _state.value = _state.value.copy(loading = true)
        viewModelScope.launch {
            val (from, to) = rangeFor(period)
            val summary = repository.reportSummary(from, to)
            _state.value = _state.value.copy(summary = summary, loading = false)
        }
    }

    private fun rangeFor(period: ReportPeriod): Pair<Long, Long> {
        val now = Calendar.getInstance()
        val to = now.timeInMillis
        val from = Calendar.getInstance()
        when (period) {
            ReportPeriod.TODAY -> {
                from.set(Calendar.HOUR_OF_DAY, 0); from.set(Calendar.MINUTE, 0); from.set(Calendar.SECOND, 0)
            }
            ReportPeriod.WEEK -> {
                from.set(Calendar.DAY_OF_WEEK, from.firstDayOfWeek)
                from.set(Calendar.HOUR_OF_DAY, 0); from.set(Calendar.MINUTE, 0); from.set(Calendar.SECOND, 0)
            }
            ReportPeriod.MONTH -> {
                from.set(Calendar.DAY_OF_MONTH, 1)
                from.set(Calendar.HOUR_OF_DAY, 0); from.set(Calendar.MINUTE, 0); from.set(Calendar.SECOND, 0)
            }
            ReportPeriod.ALL_TIME -> {
                from.timeInMillis = 0L
            }
        }
        return from.timeInMillis to to
    }
}
