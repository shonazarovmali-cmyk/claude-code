package com.hanfood.warehouse.ui.screens.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hanfood.warehouse.R
import com.hanfood.warehouse.data.repository.WarehouseRepository
import com.hanfood.warehouse.ui.components.BackTopBar
import com.hanfood.warehouse.ui.navigation.ScannerBus
import com.hanfood.warehouse.util.GenericViewModelFactory

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

    LaunchedEffect(scanned) {
        scanned?.let {
            viewModel.applyScannedBarcode(it)
            ScannerBus.consume()
        }
    }
    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Scaffold(
        topBar = {
            BackTopBar(
                title = stringResource(if (productId == 0L) R.string.product_edit_title_new else R.string.product_edit_title_edit),
                onBack = onBack
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
