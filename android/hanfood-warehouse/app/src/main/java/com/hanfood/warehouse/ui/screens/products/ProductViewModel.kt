package com.hanfood.warehouse.ui.screens.products

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanfood.warehouse.R
import com.hanfood.warehouse.data.local.entity.Product
import com.hanfood.warehouse.data.repository.WarehouseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductListViewModel(private val repository: WarehouseRepository) : ViewModel() {

    val query = MutableStateFlow("")

    val products: StateFlow<List<Product>> = query
        .flatMapLatest { q -> if (q.isBlank()) repository.products else repository.searchProducts(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun archive(product: Product) {
        viewModelScope.launch { repository.archiveProduct(product) }
    }
}

data class ProductEditUiState(
    val id: Long = 0L,
    val name: String = "",
    val barcode: String = "",
    val unit: String = "dona",
    val quantity: String = "0",
    val minQuantity: String = "0",
    val purchasePrice: String = "0",
    val sellPrice: String = "0",
    val category: String = "",
    val imagePath: String? = null,
    val loaded: Boolean = false,
    val saved: Boolean = false,
    @StringRes val errorRes: Int? = null
)

class ProductEditViewModel(
    private val repository: WarehouseRepository,
    private val productId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(ProductEditUiState(loaded = productId == 0L))
    val state: StateFlow<ProductEditUiState> = _state

    init {
        if (productId != 0L) {
            viewModelScope.launch {
                val product = repository.getProduct(productId)
                if (product != null) {
                    _state.value = ProductEditUiState(
                        id = product.id,
                        name = product.name,
                        barcode = product.barcode.orEmpty(),
                        unit = product.unit,
                        quantity = product.quantity.toPlainStringTrimmed(),
                        minQuantity = product.minQuantity.toPlainStringTrimmed(),
                        purchasePrice = product.purchasePrice.toPlainStringTrimmed(),
                        sellPrice = product.sellPrice.toPlainStringTrimmed(),
                        category = product.category.orEmpty(),
                        imagePath = product.imagePath,
                        loaded = true
                    )
                } else {
                    _state.value = _state.value.copy(loaded = true, errorRes = R.string.error_product_not_found)
                }
            }
        }
    }

    fun update(transform: (ProductEditUiState) -> ProductEditUiState) {
        _state.value = transform(_state.value)
    }

    fun applyScannedBarcode(code: String) {
        _state.value = _state.value.copy(barcode = code)
    }

    fun applyPickedImage(path: String) {
        _state.value = _state.value.copy(imagePath = path)
    }

    fun clearImage() {
        _state.value = _state.value.copy(imagePath = null)
    }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.value = s.copy(errorRes = R.string.error_product_name_required)
            return
        }
        viewModelScope.launch {
            val product = Product(
                id = s.id,
                name = s.name.trim(),
                barcode = s.barcode.trim().ifBlank { null },
                unit = s.unit.trim().ifBlank { "dona" },
                quantity = s.quantity.toDoubleOrNull() ?: 0.0,
                minQuantity = s.minQuantity.toDoubleOrNull() ?: 0.0,
                purchasePrice = s.purchasePrice.toDoubleOrNull() ?: 0.0,
                sellPrice = s.sellPrice.toDoubleOrNull() ?: 0.0,
                category = s.category.trim().ifBlank { null },
                imagePath = s.imagePath
            )
            try {
                repository.upsertProduct(product)
                _state.value = _state.value.copy(saved = true, errorRes = null)
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorRes = R.string.error_product_save_duplicate)
            }
        }
    }
}

private fun Double.toPlainStringTrimmed(): String =
    if (this == this.toLong().toDouble()) this.toLong().toString() else this.toString()
