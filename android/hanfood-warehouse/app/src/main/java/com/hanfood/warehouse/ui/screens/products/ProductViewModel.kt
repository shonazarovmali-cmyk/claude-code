package com.hanfood.warehouse.ui.screens.products

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanfood.warehouse.R
import com.hanfood.warehouse.data.local.entity.Product
import com.hanfood.warehouse.data.local.entity.toAttachmentList
import com.hanfood.warehouse.data.local.entity.toAttachmentPathsString
import com.hanfood.warehouse.data.repository.ExcelImportResult
import com.hanfood.warehouse.data.repository.WarehouseRepository
import com.hanfood.warehouse.util.ExcelImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Products screen's "import from Excel" flow state. */
sealed interface ExcelImportUiState {
    data object Idle : ExcelImportUiState
    data object Importing : ExcelImportUiState
    data object Error : ExcelImportUiState
    data class Success(val result: ExcelImportResult) : ExcelImportUiState
}

class ProductListViewModel(private val repository: WarehouseRepository) : ViewModel() {

    val query = MutableStateFlow("")

    val products: StateFlow<List<Product>> = query
        .flatMapLatest { q -> if (q.isBlank()) repository.products else repository.searchProducts(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _importState = MutableStateFlow<ExcelImportUiState>(ExcelImportUiState.Idle)
    val importState: StateFlow<ExcelImportUiState> = _importState

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun archive(product: Product) {
        viewModelScope.launch { repository.archiveProduct(product) }
    }

    fun importExcel(context: Context, uri: Uri) {
        _importState.value = ExcelImportUiState.Importing
        viewModelScope.launch {
            val rows = withContext(Dispatchers.IO) { ExcelImporter.parse(context, uri) }
            if (rows.isNullOrEmpty()) {
                _importState.value = ExcelImportUiState.Error
                return@launch
            }
            val result = repository.importProductsFromExcel(rows, sourceLabel = null)
            _importState.value = ExcelImportUiState.Success(result)
        }
    }

    fun dismissImportState() {
        _importState.value = ExcelImportUiState.Idle
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
    val imagePaths: List<String> = emptyList(),
    val articleNumber: String = "",
    val hsCode: String = "",
    val piecesPerBox: String = "",
    val priceEur: String = "",
    val status: String = "",
    val loaded: Boolean = false,
    val saved: Boolean = false,
    val deleted: Boolean = false,
    @StringRes val errorRes: Int? = null
) {
    /** Live pricing summary — only meaningful once [piecesPerBox] is set (a boxed/wholesale product). */
    val pricingSummary: ProductPricingSummary?
        get() {
            val pieces = piecesPerBox.toDoubleOrNull() ?: return null
            if (pieces <= 0) return null
            val purchase = purchasePrice.toDoubleOrNull() ?: 0.0
            val sell = sellPrice.toDoubleOrNull() ?: 0.0
            val boxes = quantity.toDoubleOrNull() ?: 0.0
            return ProductPricingSummary(
                boxPrice = purchase * pieces,
                boxPriceWithMarkup = sell * pieces,
                totalPrice = purchase * pieces * boxes,
                totalPriceWithMarkup = sell * pieces * boxes
            )
        }
}

data class ProductPricingSummary(
    val boxPrice: Double,
    val boxPriceWithMarkup: Double,
    val totalPrice: Double,
    val totalPriceWithMarkup: Double
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
                        imagePaths = product.imagePaths.toAttachmentList(),
                        articleNumber = product.articleNumber.orEmpty(),
                        hsCode = product.hsCode.orEmpty(),
                        piecesPerBox = product.piecesPerBox?.toPlainStringTrimmed().orEmpty(),
                        priceEur = product.priceEur?.toPlainStringTrimmed().orEmpty(),
                        status = product.status.orEmpty(),
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

    fun addImages(paths: List<String>) {
        val current = _state.value.imagePaths
        val remaining = (MAX_IMAGES - current.size).coerceAtLeast(0)
        if (remaining == 0 || paths.isEmpty()) return
        _state.value = _state.value.copy(imagePaths = current + paths.take(remaining))
    }

    fun removeImage(path: String) {
        _state.value = _state.value.copy(imagePaths = _state.value.imagePaths.filterNot { it == path })
    }

    /** Fills [ProductEditUiState.purchasePrice] from [ProductEditUiState.priceEur] × the configured EUR→PLN rate. */
    fun convertEurToPln(rate: Float) {
        val eur = _state.value.priceEur.toDoubleOrNull() ?: return
        _state.value = _state.value.copy(purchasePrice = (eur * rate).toPlainStringTrimmed())
    }

    /** Suggests [ProductEditUiState.sellPrice] as purchasePrice + the configured markup %. */
    fun calculateSellPrice(markupPercent: Float) {
        val purchase = _state.value.purchasePrice.toDoubleOrNull() ?: return
        _state.value = _state.value.copy(sellPrice = (purchase * (1 + markupPercent / 100.0)).toPlainStringTrimmed())
    }

    fun delete() {
        if (productId == 0L) return
        viewModelScope.launch {
            val product = repository.getProduct(productId) ?: return@launch
            repository.archiveProduct(product)
            _state.value = _state.value.copy(deleted = true)
        }
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
                imagePaths = s.imagePaths.toAttachmentPathsString(),
                articleNumber = s.articleNumber.trim().ifBlank { null },
                hsCode = s.hsCode.trim().ifBlank { null },
                piecesPerBox = s.piecesPerBox.toDoubleOrNull(),
                priceEur = s.priceEur.toDoubleOrNull(),
                status = s.status.trim().ifBlank { null }
            )
            try {
                repository.upsertProduct(product)
                _state.value = _state.value.copy(saved = true, errorRes = null)
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorRes = R.string.error_product_save_duplicate)
            }
        }
    }

    companion object {
        const val MAX_IMAGES = 10
    }
}

private fun Double.toPlainStringTrimmed(): String =
    if (this == this.toLong().toDouble()) this.toLong().toString() else this.toString()
