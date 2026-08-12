package com.hanfood.warehouse.ui.screens.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanfood.warehouse.data.local.entity.StockTransaction
import com.hanfood.warehouse.data.local.entity.TransactionItemDetail
import com.hanfood.warehouse.data.repository.WarehouseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class InvoiceDetailUiState(
    val transaction: StockTransaction? = null,
    val items: List<TransactionItemDetail> = emptyList(),
    val counterpartyLabel: String = "",
    val counterpartyName: String = "",
    val loading: Boolean = true
)

class InvoiceDetailViewModel(repository: WarehouseRepository, transactionId: Long) : ViewModel() {

    val uiState: StateFlow<InvoiceDetailUiState> = combine(
        repository.observeTransaction(transactionId),
        repository.itemsForTransaction(transactionId)
    ) { tx, items -> tx to items }
        .flatMapLatest { (tx, items) ->
            when {
                tx == null -> flowOf(InvoiceDetailUiState(loading = false))
                tx.clientId != null -> repository.observeClient(tx.clientId).map { client ->
                    InvoiceDetailUiState(
                        transaction = tx,
                        items = items,
                        counterpartyLabel = "Mijoz",
                        counterpartyName = client?.name ?: "Noma'lum mijoz",
                        loading = false
                    )
                }
                else -> flowOf(
                    InvoiceDetailUiState(
                        transaction = tx,
                        items = items,
                        counterpartyLabel = "Ta'minotchi",
                        counterpartyName = tx.supplierName ?: "Ko'rsatilmagan",
                        loading = false
                    )
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InvoiceDetailUiState())
}
