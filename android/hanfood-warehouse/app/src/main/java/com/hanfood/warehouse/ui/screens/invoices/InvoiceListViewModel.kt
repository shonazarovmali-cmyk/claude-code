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

data class InvoiceRow(val transaction: StockTransaction, val counterpartyName: String)

class InvoiceListViewModel(repository: WarehouseRepository) : ViewModel() {

    val selectedType = MutableStateFlow<TransactionType?>(null)

    private val transactions = selectedType
        .flatMapLatest { type -> if (type == null) repository.allTransactions else repository.transactionsByType(type) }

    val rows: StateFlow<List<InvoiceRow>> = combine(transactions, repository.clients) { txs, clients ->
        val clientMap = clients.associateBy { it.id }
        txs.map { tx ->
            val name = when {
                tx.clientId != null -> clientMap[tx.clientId]?.name ?: "Noma'lum mijoz"
                !tx.supplierName.isNullOrBlank() -> tx.supplierName
                else -> "Ta'minotchi ko'rsatilmagan"
            }
            InvoiceRow(tx, name)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectType(type: TransactionType?) {
        selectedType.value = type
    }
}
