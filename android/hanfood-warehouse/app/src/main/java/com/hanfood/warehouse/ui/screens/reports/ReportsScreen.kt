package com.hanfood.warehouse.ui.screens.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hanfood.warehouse.R
import com.hanfood.warehouse.data.local.entity.ClientActivitySummary
import com.hanfood.warehouse.data.local.entity.ProductMovementSummary
import com.hanfood.warehouse.data.repository.WarehouseRepository
import com.hanfood.warehouse.ui.components.SectionHeader
import com.hanfood.warehouse.ui.components.StatCard
import com.hanfood.warehouse.ui.components.reportPeriodLabel
import com.hanfood.warehouse.ui.theme.DangerRed
import com.hanfood.warehouse.ui.theme.SuccessGreen
import com.hanfood.warehouse.ui.theme.WarningAmber
import com.hanfood.warehouse.util.GenericViewModelFactory
import com.hanfood.warehouse.util.formatMoney
import com.hanfood.warehouse.util.formatQuantity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(repository: WarehouseRepository) {
    val viewModel: ReportsViewModel = viewModel(factory = GenericViewModelFactory { ReportsViewModel(repository) })
    val state by viewModel.state.collectAsStateWithLifecycle()

    run {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ReportPeriod.entries.toList()) { period ->
                    FilterChip(
                        selected = state.period == period,
                        onClick = { viewModel.selectPeriod(period) },
                        label = { Text(reportPeriodLabel(period)) }
                    )
                }
            }

            if (state.loading || state.summary == null) {
                Row(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val summary = state.summary!!
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatCard(
                                label = stringResource(R.string.reports_stat_stock_in),
                                value = formatMoney(summary.stockInAmount),
                                caption = stringResource(R.string.reports_invoices_count, summary.stockInCount),
                                accent = SuccessGreen,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                label = stringResource(R.string.reports_stat_stock_out),
                                value = formatMoney(summary.stockOutAmount),
                                caption = stringResource(R.string.reports_invoices_count, summary.stockOutCount),
                                accent = WarningAmber,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        StatCard(
                            label = stringResource(R.string.reports_stat_return),
                            value = formatMoney(summary.returnAmount),
                            caption = stringResource(R.string.reports_return_caption, summary.returnCount, formatQuantity(summary.returnQty)),
                            accent = DangerRed
                        )
                    }

                    if (summary.lowStock.isNotEmpty()) {
                        item { SectionHeader(stringResource(R.string.reports_low_stock_header, summary.lowStock.size)) }
                        items(summary.lowStock) { product ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(product.name)
                                    Text(
                                        "${formatQuantity(product.quantity)} / ${formatQuantity(product.minQuantity)} ${product.unit}",
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    if (summary.topOutProducts.isNotEmpty()) {
                        item { SectionHeader(stringResource(R.string.reports_top_out_header)) }
                        items(summary.topOutProducts) { row -> ProductMovementRow(row) }
                    }

                    if (summary.topInProducts.isNotEmpty()) {
                        item { SectionHeader(stringResource(R.string.reports_top_in_header)) }
                        items(summary.topInProducts) { row -> ProductMovementRow(row) }
                    }

                    if (summary.topClients.isNotEmpty()) {
                        item { SectionHeader(stringResource(R.string.reports_top_clients_header)) }
                        items(summary.topClients) { row -> ClientActivityRow(row) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductMovementRow(row: ProductMovementSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(row.productName)
            Text("${formatQuantity(row.totalQuantity)} ${row.unit}", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ClientActivityRow(row: ClientActivitySummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(row.clientName, fontWeight = FontWeight.Medium)
                Text(
                    stringResource(R.string.reports_transactions_count, row.transactionCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(formatMoney(row.totalAmount), fontWeight = FontWeight.SemiBold)
        }
    }
}
