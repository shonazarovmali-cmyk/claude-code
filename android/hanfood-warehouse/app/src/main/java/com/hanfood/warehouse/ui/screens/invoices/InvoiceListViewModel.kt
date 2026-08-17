package com.hanfood.warehouse.ui.screens.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanfood.warehouse.data.local.entity.StockTransaction
import com.hanfood.warehouse.data.local.entity.TransactionType
import com.hanfood.warehouse.data.repository.WarehouseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/** Either a real client/supplier name, or a localizable fallback resolved in the UI layer. */
sealed class CounterpartyName {
    data class Known(val name: String) : CounterpartyName()
    data object UnknownClient : CounterpartyName()
    data object NoSupplier : CounterpartyName()
}

data class InvoiceRow(val transaction: StockTransaction, val counterparty: CounterpartyName)

class InvoiceListViewModel(repository: WarehouseRepository) : ViewModel() {

    val selectedType = MutableStateFlow<TransactionType?>(null)

    private val transactions = selectedType
        .flatMapLatest { type -> if (type == null) repository.allTransactions else repository.transactionsByType(type) }

    val rows: StateFlow<List<InvoiceRow>> = combine(transactions, repository.clients) { txs, clients ->
        val clientMap = clients.associateBy { it.id }
        txs.map { tx ->
            val counterparty = when {
                tx.clientId != null -> clientMap[tx.clientId]?.let { CounterpartyName.Known(it.name) } ?: CounterpartyName.UnknownClient
                !tx.supplierName.isNullOrBlank() -> CounterpartyName.Known(tx.supplierName)
                else -> CounterpartyName.NoSupplier
            }
            InvoiceRow(tx, counterparty)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectType(type: TransactionType?) {
        selectedType.value = type
    }
}
