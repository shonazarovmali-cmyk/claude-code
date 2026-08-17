package com.hanfood.warehouse.ui.screens.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanfood.warehouse.data.local.entity.StockTransaction
import com.hanfood.warehouse.data.local.entity.TransactionItemDetail
import com.hanfood.warehouse.data.repository.WarehouseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Whether the counterparty of this invoice is a client or a supplier — resolved to text in the UI layer. */
enum class CounterpartyKind { CLIENT, SUPPLIER }

data class InvoiceDetailUiState(
    val transaction: StockTransaction? = null,
    val items: List<TransactionItemDetail> = emptyList(),
    val counterpartyKind: CounterpartyKind = CounterpartyKind.SUPPLIER,
    val counterparty: CounterpartyName = CounterpartyName.NoSupplier,
    val loading: Boolean = true,
    val deleted: Boolean = false
)

class InvoiceDetailViewModel(
    private val repository: WarehouseRepository,
    private val transactionId: Long
) : ViewModel() {

    private val _deleted = MutableStateFlow(false)

    fun delete() {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId)
            _deleted.value = true
        }
    }

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
                        counterpartyKind = CounterpartyKind.CLIENT,
                        counterparty = client?.let { CounterpartyName.Known(it.name) } ?: CounterpartyName.UnknownClient,
                        loading = false
                    )
                }
                else -> flowOf(
                    InvoiceDetailUiState(
                        transaction = tx,
                        items = items,
                        counterpartyKind = CounterpartyKind.SUPPLIER,
                        counterparty = tx.supplierName?.let { CounterpartyName.Known(it) } ?: CounterpartyName.NoSupplier,
                        loading = false
                    )
                )
            }
        }
        .combine(_deleted) { state, deleted -> state.copy(deleted = deleted) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InvoiceDetailUiState())
}
