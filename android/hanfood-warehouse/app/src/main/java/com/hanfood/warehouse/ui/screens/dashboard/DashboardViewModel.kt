package com.hanfood.warehouse.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanfood.warehouse.data.local.entity.Product
import com.hanfood.warehouse.data.local.entity.ProductMovementSummary
import com.hanfood.warehouse.data.local.entity.StockTransaction
import com.hanfood.warehouse.data.repository.WarehouseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val totalStockValue: Double = 0.0,
    val totalUnits: Double = 0.0,
    val productCount: Int = 0,
    val clientCount: Int = 0,
    val lowStock: List<Product> = emptyList(),
    val recentTransactions: List<StockTransaction> = emptyList(),
    val topProducts: List<ProductMovementSummary> = emptyList(),
    val loading: Boolean = true
)

private data class Counters(
    val totalStockValue: Double,
    val totalUnits: Double,
    val productCount: Int,
    val clientCount: Int,
    val lowStock: List<Product>
)

class DashboardViewModel(repository: WarehouseRepository) : ViewModel() {

    // Bir martalik so'rov (Hisobotlar ekranidagi kabi) — oxirgi 30 kunda eng
    // ko'p sotilgan mahsulotlar, real-vaqtli oqim emas.
    private val topProducts = MutableStateFlow<List<ProductMovementSummary>>(emptyList())

    init {
        viewModelScope.launch {
            topProducts.value = repository.topSellingProducts()
        }
    }

    private val counters = combine(
        repository.totalStockValue,
        repository.totalUnits,
        repository.activeProductCount,
        repository.activeClientCount,
        repository.lowStockProducts
    ) { totalValue, totalUnits, productCount, clientCount, lowStock ->
        Counters(totalValue, totalUnits, productCount, clientCount, lowStock)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        counters,
        repository.recentTransactions(6),
        topProducts
    ) { counters, recent, top ->
        DashboardUiState(
            totalStockValue = counters.totalStockValue,
            totalUnits = counters.totalUnits,
            productCount = counters.productCount,
            clientCount = counters.clientCount,
            lowStock = counters.lowStock,
            recentTransactions = recent,
            topProducts = top,
            loading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}
