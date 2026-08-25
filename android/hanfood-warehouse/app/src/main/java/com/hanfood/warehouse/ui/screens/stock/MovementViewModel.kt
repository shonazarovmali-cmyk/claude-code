package com.hanfood.warehouse.ui.screens.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanfood.warehouse.R
import com.hanfood.warehouse.data.local.entity.TransactionType
import com.hanfood.warehouse.data.repository.CartLine
import com.hanfood.warehouse.data.repository.InsufficientStockException
import com.hanfood.warehouse.data.repository.WarehouseRepository
import com.hanfood.warehouse.util.UiMessage
import com.hanfood.warehouse.util.formatQuantity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MovementUiState(
    val type: TransactionType = TransactionType.STOCK_IN,
    val supplierName: String = "",
    val clientId: Long? = null,
    val clientName: String = "",
    val title: String = "",
    val note: String = "",
    val lines: List<CartLine> = emptyList(),
    val attachments: List<String> = emptyList(),
    val saving: Boolean = false,
    val error: UiMessage? = null,
    val infoMessage: UiMessage? = null,
    val savedTransactionId: Long? = null
) {
    val requiresClient: Boolean get() = type != TransactionType.STOCK_IN
    val total: Double get() = lines.sumOf { it.quantity * it.unitPrice }
    val canSave: Boolean get() = lines.isNotEmpty() && (!requiresClient || clientId != null)
}

class MovementViewModel(
    private val repository: WarehouseRepository,
    type: TransactionType
) : ViewModel() {

    private val _state = MutableStateFlow(MovementUiState(type = type))
    val state: StateFlow<MovementUiState> = _state

    fun setSupplierName(value: String) {
        _state.value = _state.value.copy(supplierName = value)
    }

    fun selectClient(id: Long, name: String) {
        _state.value = _state.value.copy(clientId = id, clientName = name)
    }

    fun clearClient() {
        _state.value = _state.value.copy(clientId = null, clientName = "")
    }

    fun setNote(value: String) {
        _state.value = _state.value.copy(note = value)
    }

    fun setTitle(value: String) {
        _state.value = _state.value.copy(title = value)
    }

    fun addAttachment(path: String) {
        _state.value = _state.value.copy(attachments = _state.value.attachments + path)
    }

    fun removeAttachment(path: String) {
        _state.value = _state.value.copy(attachments = _state.value.attachments.filterNot { it == path })
    }

    fun dismissMessages() {
        _state.value = _state.value.copy(error = null, infoMessage = null)
    }

    fun addProductByBarcode(barcode: String) {
        viewModelScope.launch {
            val product = repository.getProductByBarcode(barcode)
            if (product == null) {
                _state.value = _state.value.copy(
                    error = UiMessage(R.string.error_barcode_not_found, listOf(barcode))
                )
                return@launch
            }
            addOrIncrementLine(
                CartLine(
                    productId = product.id,
                    productName = product.name,
                    unit = product.unit,
                    quantity = 1.0,
                    unitPrice = if (_state.value.type == TransactionType.STOCK_IN) product.purchasePrice else product.sellPrice
                )
            )
        }
    }

    fun addProduct(productId: Long, name: String, unit: String, defaultPrice: Double) {
        addOrIncrementLine(
            CartLine(productId = productId, productName = name, unit = unit, quantity = 1.0, unitPrice = defaultPrice)
        )
    }

    private fun addOrIncrementLine(line: CartLine) {
        val current = _state.value.lines
        val existingIndex = current.indexOfFirst { it.productId == line.productId }
        val updated = if (existingIndex >= 0) {
            current.toMutableList().also {
                val existing = it[existingIndex]
                it[existingIndex] = existing.copy(quantity = existing.quantity + line.quantity)
            }
        } else {
            current + line
        }
        _state.value = _state.value.copy(
            lines = updated,
            infoMessage = UiMessage(R.string.info_product_added, listOf(line.productName))
        )
    }

    fun updateLineQuantity(productId: Long, quantity: Double) {
        _state.value = _state.value.copy(
            lines = _state.value.lines.map { if (it.productId == productId) it.copy(quantity = quantity) else it }
        )
    }

    fun updateLinePrice(productId: Long, price: Double) {
        _state.value = _state.value.copy(
            lines = _state.value.lines.map { if (it.productId == productId) it.copy(unitPrice = price) else it }
        )
    }

    fun removeLine(productId: Long) {
        _state.value = _state.value.copy(lines = _state.value.lines.filterNot { it.productId == productId })
    }

    fun save() {
        val s = _state.value
        if (!s.canSave) return
        _state.value = s.copy(saving = true, error = null)
        viewModelScope.launch {
            try {
                val transactionId = when (s.type) {
                    TransactionType.STOCK_IN -> repository.recordStockIn(
                        supplierName = s.supplierName.trim().ifBlank { null },
                        note = s.note.trim().ifBlank { null },
                        lines = s.lines,
                        title = s.title,
                        attachmentPaths = s.attachments
                    )
                    TransactionType.STOCK_OUT -> repository.recordStockOut(
                        clientId = requireNotNull(s.clientId),
                        note = s.note.trim().ifBlank { null },
                        lines = s.lines,
                        title = s.title,
                        attachmentPaths = s.attachments
                    )
                    TransactionType.RETURN -> repository.recordReturn(
                        clientId = requireNotNull(s.clientId),
                        note = s.note.trim().ifBlank { null },
                        lines = s.lines,
                        title = s.title,
                        attachmentPaths = s.attachments
                    )
                }
                _state.value = _state.value.copy(saving = false, savedTransactionId = transactionId)
            } catch (e: InsufficientStockException) {
                _state.value = _state.value.copy(
                    saving = false,
                    error = UiMessage(
                        R.string.error_insufficient_stock,
                        listOf(e.product.name, formatQuantity(e.product.quantity), formatQuantity(e.requested))
                    )
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    saving = false,
                    error = UiMessage(R.string.error_generic_save, listOf(e.message ?: ""))
                )
            }
        }
    }
}
