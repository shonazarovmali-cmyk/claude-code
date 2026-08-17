package com.hanfood.warehouse.ui.screens.stock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hanfood.warehouse.R
import com.hanfood.warehouse.data.local.entity.TransactionType
import com.hanfood.warehouse.data.repository.CartLine
import com.hanfood.warehouse.data.repository.WarehouseRepository
import com.hanfood.warehouse.ui.components.BackTopBar
import com.hanfood.warehouse.ui.components.movementScreenTitle
import com.hanfood.warehouse.ui.navigation.ScannerBus
import com.hanfood.warehouse.util.GenericViewModelFactory
import com.hanfood.warehouse.util.UiMessage
import com.hanfood.warehouse.util.formatMoney
import kotlinx.coroutines.launch

@Composable
fun MovementScreen(
    repository: WarehouseRepository,
    movementType: TransactionType,
    onBack: () -> Unit,
    onScan: () -> Unit,
    onSaved: (Long) -> Unit
) {
    val viewModel: MovementViewModel = viewModel(
        key = "movement_${movementType.name}",
        factory = GenericViewModelFactory { MovementViewModel(repository, movementType) }
    )
    val state by viewModel.state.collectAsState()
    val scanned by ScannerBus.lastScanned.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showProductPicker by remember { mutableStateOf(false) }
    var showClientPicker by remember { mutableStateOf(false) }

    LaunchedEffect(scanned) {
        scanned?.let {
            viewModel.addProductByBarcode(it)
            ScannerBus.consume()
        }
    }
    LaunchedEffect(state.savedTransactionId) {
        state.savedTransactionId?.let { onSaved(it) }
    }
    val message = state.error ?: state.infoMessage
    val messageText = message?.let { resolveMessage(it) }
    LaunchedEffect(messageText) {
        if (messageText != null) {
            scope.launch { snackbarHostState.showSnackbar(messageText) }
            viewModel.dismissMessages()
        }
    }

    Scaffold(
        topBar = { BackTopBar(title = movementScreenTitle(movementType), onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.requiresClient) {
                    OutlinedButton(onClick = { showClientPicker = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(state.clientName.ifBlank { stringResource(R.string.movement_client_select) })
                    }
                } else {
                    OutlinedTextField(
                        value = state.supplierName,
                        onValueChange = viewModel::setSupplierName,
                        label = { Text(stringResource(R.string.movement_supplier_optional)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onScan, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                        Text("  " + stringResource(R.string.action_scan))
                    }
                    OutlinedButton(onClick = { showProductPicker = true }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.action_pick_manually))
                    }
                }
            }

            Divider()

            if (state.lines.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.movement_cart_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.lines, key = { it.productId }) { line ->
                        CartLineRow(
                            line = line,
                            onQuantityChange = { viewModel.updateLineQuantity(line.productId, it) },
                            onPriceChange = { viewModel.updateLinePrice(line.productId, it) },
                            onRemove = { viewModel.removeLine(line.productId) }
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.note,
                    onValueChange = viewModel::setNote,
                    label = { Text(stringResource(R.string.movement_field_note)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.movement_total_label), style = MaterialTheme.typography.titleMedium)
                    Text(formatMoney(state.total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { viewModel.save() },
                    enabled = state.canSave && !state.saving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.saving) {
                        CircularProgressIndicator(modifier = Modifier.heightIn(max = 20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(stringResource(R.string.action_save_invoice))
                    }
                }
            }
        }
    }

    if (showProductPicker) {
        ProductPickerDialog(
            repository = repository,
            defaultsForType = state.type,
            onDismiss = { showProductPicker = false },
            onPick = { id, name, unit, price ->
                viewModel.addProduct(id, name, unit, price)
                showProductPicker = false
            }
        )
    }

    if (showClientPicker) {
        ClientPickerDialog(
            repository = repository,
            onDismiss = { showClientPicker = false },
            onPick = { id, name ->
                viewModel.selectClient(id, name)
                showClientPicker = false
            }
        )
    }
}

@Composable
private fun resolveMessage(message: UiMessage): String = stringResource(message.res, *message.args.toTypedArray())

@Composable
private fun CartLineRow(
    line: CartLine,
    onQuantityChange: (Double) -> Unit,
    onPriceChange: (Double) -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(line.productName, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.cd_delete), tint = MaterialTheme.colorScheme.error)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = if (line.quantity == line.quantity.toLong().toDouble()) line.quantity.toLong().toString() else line.quantity.toString(),
                    onValueChange = { it.toDoubleOrNull()?.let(onQuantityChange) },
                    label = { Text(stringResource(R.string.cart_field_quantity, line.unit)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = if (line.unitPrice == line.unitPrice.toLong().toDouble()) line.unitPrice.toLong().toString() else line.unitPrice.toString(),
                    onValueChange = { it.toDoubleOrNull()?.let(onPriceChange) },
                    label = { Text(stringResource(R.string.cart_field_price)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            Text(
                stringResource(R.string.cart_line_total, formatMoney(line.quantity * line.unitPrice)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductPickerDialog(
    repository: WarehouseRepository,
    defaultsForType: TransactionType,
    onDismiss: () -> Unit,
    onPick: (id: Long, name: String, unit: String, price: Double) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val products by (if (query.isBlank()) repository.products else repository.searchProducts(query)).collectAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.movement_picker_products_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.movement_picker_search)) },
                    singleLine = true
                )
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp).padding(top = 8.dp)) {
                    items(products, key = { it.id }) { product ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                                .clickable {
                                    onPick(
                                        product.id,
                                        product.name,
                                        product.unit,
                                        if (defaultsForType == TransactionType.STOCK_IN) product.purchasePrice else product.sellPrice
                                    )
                                },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(product.name)
                            Text("${product.quantity} ${product.unit}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_close)) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientPickerDialog(
    repository: WarehouseRepository,
    onDismiss: () -> Unit,
    onPick: (id: Long, name: String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val clients by (if (query.isBlank()) repository.clients else repository.searchClients(query)).collectAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.movement_picker_clients_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.movement_picker_search)) },
                    singleLine = true
                )
                if (clients.isEmpty()) {
                    Text(
                        stringResource(R.string.movement_picker_no_clients),
                        modifier = Modifier.padding(top = 12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp).padding(top = 8.dp)) {
                    items(clients, key = { it.id }) { client ->
                        Text(
                            client.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .clickable { onPick(client.id, client.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_close)) }
        }
    )
}
