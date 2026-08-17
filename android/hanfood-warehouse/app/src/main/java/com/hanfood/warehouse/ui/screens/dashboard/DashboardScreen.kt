package com.hanfood.warehouse.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hanfood.warehouse.R
import com.hanfood.warehouse.data.local.entity.StockTransaction
import com.hanfood.warehouse.data.repository.WarehouseRepository
import com.hanfood.warehouse.ui.components.StatCard
import com.hanfood.warehouse.ui.components.transactionTypeLabel
import com.hanfood.warehouse.ui.theme.BrandGold
import com.hanfood.warehouse.ui.theme.BrandNavy
import com.hanfood.warehouse.ui.theme.BrandTeal
import com.hanfood.warehouse.ui.theme.SuccessGreen
import com.hanfood.warehouse.util.GenericViewModelFactory
import com.hanfood.warehouse.util.formatDateTime
import com.hanfood.warehouse.util.formatMoney
import com.hanfood.warehouse.util.formatQuantity

@Composable
fun DashboardScreen(
    repository: WarehouseRepository,
    onQuickAction: (String) -> Unit,
    onScanner: () -> Unit,
    onAssistant: () -> Unit,
    onOpenInvoice: (Long) -> Unit
) {
    val viewModel: DashboardViewModel = viewModel(factory = GenericViewModelFactory { DashboardViewModel(repository) })
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { StatsGrid(state) }

        if (state.lowStock.isNotEmpty()) {
            item { LowStockBanner(count = state.lowStock.size) }
        }

        item {
            Text(
                stringResource(R.string.dashboard_quick_actions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        item { QuickActionsGrid(onQuickAction, onScanner, onAssistant) }

        item {
            Text(
                stringResource(R.string.dashboard_recent_transactions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (state.recentTransactions.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.dashboard_no_transactions_yet),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(state.recentTransactions) { tx ->
                RecentTransactionRow(tx, onClick = { onOpenInvoice(tx.id) })
            }
        }
    }
}

@Composable
private fun StatsGrid(state: DashboardUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                label = stringResource(R.string.dashboard_stat_stock_value),
                value = formatMoney(state.totalStockValue),
                icon = Icons.Filled.AttachMoney,
                accent = BrandTeal,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.dashboard_stat_total_units),
                value = formatQuantity(state.totalUnits),
                icon = Icons.Filled.Scale,
                accent = BrandGold,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                label = stringResource(R.string.dashboard_stat_product_types),
                value = state.productCount.toString(),
                icon = Icons.Filled.Inventory,
                accent = BrandNavy,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.dashboard_stat_clients),
                value = state.clientCount.toString(),
                icon = Icons.Filled.Groups,
                accent = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LowStockBanner(count: Int) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Text(
                stringResource(R.string.dashboard_low_stock_banner, count),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private data class QuickAction(val titleRes: Int, val icon: ImageVector, val action: String)

@Composable
private fun QuickActionsGrid(onQuickAction: (String) -> Unit, onScanner: () -> Unit, onAssistant: () -> Unit) {
    val actions = listOf(
        QuickAction(R.string.quick_action_stock_in, Icons.Filled.Inventory2, "STOCK_IN"),
        QuickAction(R.string.quick_action_stock_out, Icons.Filled.LocalShipping, "STOCK_OUT"),
        QuickAction(R.string.quick_action_return, Icons.Filled.Undo, "RETURN"),
        QuickAction(R.string.quick_action_scan, Icons.Filled.QrCodeScanner, "SCAN"),
        QuickAction(R.string.quick_action_assistant, Icons.Filled.SmartToy, "ASSISTANT")
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.aspectRatio(1.55f)
    ) {
        items(actions) { action ->
            val title = stringResource(action.titleRes)
            Card(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                shape = RoundedCornerShape(16.dp),
                onClick = {
                    when (action.action) {
                        "SCAN" -> onScanner()
                        "ASSISTANT" -> onAssistant()
                        else -> onQuickAction(action.action)
                    }
                },
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(action.icon, contentDescription = title, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentTransactionRow(tx: StockTransaction, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column {
                Text(tx.invoiceNumber, fontWeight = FontWeight.SemiBold)
                Text(
                    transactionTypeLabel(tx.type) + " • " + formatDateTime(tx.date),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(formatMoney(tx.totalAmount), fontWeight = FontWeight.Medium)
        }
    }
}
