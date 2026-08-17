package com.hanfood.warehouse.ui.screens.products

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.hanfood.warehouse.R
import com.hanfood.warehouse.data.repository.WarehouseRepository
import com.hanfood.warehouse.ui.components.BackTopBar
import com.hanfood.warehouse.ui.components.ConfirmDeleteDialog
import com.hanfood.warehouse.ui.components.DeleteAction
import com.hanfood.warehouse.ui.navigation.ScannerBus
import com.hanfood.warehouse.util.FileStorage
import com.hanfood.warehouse.util.GenericViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ProductEditScreen(
    repository: WarehouseRepository,
    productId: Long,
    onBack: () -> Unit,
    onScan: () -> Unit
) {
    val viewModel: ProductEditViewModel = viewModel(
        key = "product_edit_$productId",
        factory = GenericViewModelFactory { ProductEditViewModel(repository, productId) }
    )
    val state by viewModel.state.collectAsState()
    val scanned by ScannerBus.lastScanned.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = ProductEditViewModel.MAX_IMAGES)
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val paths = withContext(Dispatchers.IO) {
                    uris.mapNotNull { uri -> FileStorage.saveProductImage(context, uri) }
                }
                if (paths.isNotEmpty()) viewModel.addImages(paths)
            }
        }
    }

    LaunchedEffect(scanned) {
        scanned?.let {
            viewModel.applyScannedBarcode(it)
            ScannerBus.consume()
        }
    }
    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }
    LaunchedEffect(state.deleted) {
        if (state.deleted) onBack()
    }

    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = stringResource(R.string.confirm_delete_product_title),
            message = stringResource(R.string.confirm_delete_product_message),
            onConfirm = {
                showDeleteConfirm = false
                viewModel.delete()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    Scaffold(
        topBar = {
            BackTopBar(
                title = stringResource(if (productId == 0L) R.string.product_edit_title_new else R.string.product_edit_title_edit),
                onBack = onBack,
                actions = {
                    if (productId != 0L) {
                        DeleteAction(
                            contentDescription = stringResource(R.string.action_delete_product),
                            onClick = { showDeleteConfirm = true }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProductPhotoPicker(
                imagePaths = state.imagePaths,
                onPick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                onRemove = { path -> viewModel.removeImage(path) }
            )

            OutlinedTextField(
                value = state.name,
                onValueChange = { v -> viewModel.update { it.copy(name = v) } },
                label = { Text(stringResource(R.string.product_field_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.barcode,
                    onValueChange = { v -> viewModel.update { it.copy(barcode = v) } },
                    label = { Text(stringResource(R.string.product_field_barcode)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = onScan) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = stringResource(R.string.cd_scan))
                }
            }

            OutlinedTextField(
                value = state.unit,
                onValueChange = { v -> viewModel.update { it.copy(unit = v) } },
                label = { Text(stringResource(R.string.product_field_unit)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.quantity,
                    onValueChange = { v -> viewModel.update { it.copy(quantity = v) } },
                    label = { Text(stringResource(R.string.product_field_quantity)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = state.minQuantity,
                    onValueChange = { v -> viewModel.update { it.copy(minQuantity = v) } },
                    label = { Text(stringResource(R.string.product_field_min_quantity)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.purchasePrice,
                    onValueChange = { v -> viewModel.update { it.copy(purchasePrice = v) } },
                    label = { Text(stringResource(R.string.product_field_purchase_price)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = state.sellPrice,
                    onValueChange = { v -> viewModel.update { it.copy(sellPrice = v) } },
                    label = { Text(stringResource(R.string.product_field_sell_price)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            OutlinedTextField(
                value = state.category,
                onValueChange = { v -> viewModel.update { it.copy(category = v) } },
                label = { Text(stringResource(R.string.product_field_category)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            val errorRes = state.errorRes
            if (errorRes != null) {
                Text(stringResource(errorRes), color = MaterialTheme.colorScheme.error)
            }

            Button(onClick = { viewModel.save() }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}

/**
 * Up to [ProductEditViewModel.MAX_IMAGES] photos per product, swiped through
 * like an Instagram multi-photo post — a [HorizontalPager] with dot
 * indicators, plus a trailing "add more" page once under the cap.
 */
@Composable
private fun ProductPhotoPicker(
    imagePaths: List<String>,
    onPick: () -> Unit,
    onRemove: (String) -> Unit
) {
    val canAddMore = imagePaths.size < ProductEditViewModel.MAX_IMAGES
    // Pages: one per existing photo, plus a trailing "add" page if there's room.
    val pageCount = imagePaths.size + if (canAddMore) 1 else 0

    if (pageCount == 0) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AddPhotoTile(onPick = onPick)
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { pageCount })

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(160.dp)
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (page < imagePaths.size) {
                    val path = imagePaths[page]
                    AsyncImage(
                        model = File(path),
                        contentDescription = stringResource(R.string.cd_product_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = { onRemove(path) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), RoundedCornerShape(50))
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.product_photo_remove))
                    }
                } else {
                    AddPhotoTile(onPick = onPick)
                }
            }
        }

        if (pageCount > 1) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pageCount) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (selected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }
        }

        Text(
            stringResource(R.string.product_photo_count, imagePaths.size, ProductEditViewModel.MAX_IMAGES),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun AddPhotoTile(onPick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onPick, modifier = Modifier.size(48.dp)) {
            Icon(
                Icons.Filled.AddAPhoto,
                contentDescription = stringResource(R.string.cd_product_photo),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            stringResource(R.string.product_photo_add),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
