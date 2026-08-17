package com.hanfood.warehouse.ui.screens.invoices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hanfood.warehouse.R
import com.hanfood.warehouse.data.local.entity.TransactionType
import com.hanfood.warehouse.data.repository.WarehouseRepository
import com.hanfood.warehouse.ui.components.EmptyState
import com.hanfood.warehouse.ui.components.transactionTypeLabel
import com.hanfood.warehouse.ui.theme.DangerRed
import com.hanfood.warehouse.ui.theme.SuccessGreen
import com.hanfood.warehouse.ui.theme.WarningAmber
import com.hanfood.warehouse.util.GenericViewModelFactory
import com.hanfood.warehouse.util.formatDateTime
import com.hanfood.warehouse.util.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    repository: WarehouseRepository,
    onOpen: (Long) -> Unit
) {
    val viewModel: InvoiceListViewModel = viewModel(factory = GenericViewModelFactory { InvoiceListViewModel(repository) })
    val rows by viewModel.rows.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(stringResource(R.string.invoices_title), fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = selectedType == null, onClick = { viewModel.selectType(null) }, label = { Text(stringResource(R.string.filter_all)) })
                FilterChip(selected = selectedType == TransactionType.STOCK_IN, onClick = { viewModel.selectType(TransactionType.STOCK_IN) }, label = { Text(stringResource(R.string.transaction_type_stock_in)) })
                FilterChip(selected = selectedType == TransactionType.STOCK_OUT, onClick = { viewModel.selectType(TransactionType.STOCK_OUT) }, label = { Text(stringResource(R.string.transaction_type_stock_out)) })
                FilterChip(selected = selectedType == TransactionType.RETURN, onClick = { viewModel.selectType(TransactionType.RETURN) }, label = { Text(stringResource(R.string.transaction_type_return)) })
            }

            if (rows.isEmpty()) {
                EmptyState(title = stringResource(R.string.invoices_empty_title), subtitle = stringResource(R.string.invoices_empty_subtitle))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rows, key = { it.transaction.id }) { row ->
                        InvoiceRowCard(row, onClick = { onOpen(row.transaction.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun counterpartyText(counterparty: CounterpartyName): String = when (counterparty) {
    is CounterpartyName.Known -> counterparty.name
    CounterpartyName.UnknownClient -> stringResource(R.string.counterparty_unknown_client)
    CounterpartyName.NoSupplier -> stringResource(R.string.counterparty_no_supplier)
}

@Composable
private fun InvoiceRowCard(row: InvoiceRow, onClick: () -> Unit) {
    val color = when (row.transaction.type) {
        TransactionType.STOCK_IN -> SuccessGreen
        TransactionType.STOCK_OUT -> WarningAmber
        TransactionType.RETURN -> DangerRed
    }
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(row.transaction.invoiceNumber, fontWeight = FontWeight.SemiBold)
                Text(counterpartyText(row.counterparty), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatDateTime(row.transaction.date), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                TypeBadge(transactionTypeLabel(row.transaction.type), color)
                Text(formatMoney(row.transaction.totalAmount), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

@Composable
private fun TypeBadge(label: String, color: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))) {
        Text(
            label,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
