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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hanfood.warehouse.data.local.entity.StockTransaction
import com.hanfood.warehouse.data.local.entity.TransactionType
import com.hanfood.warehouse.data.repository.WarehouseRepository
import com.hanfood.warehouse.ui.components.StatCard
import com.hanfood.warehouse.util.GenericViewModelFactory
import com.hanfood.warehouse.util.formatDateTime
import com.hanfood.warehouse.util.formatMoney
import com.hanfood.warehouse.util.formatQuantity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    repository: WarehouseRepository,
    onQuickAction: (String) -> Unit,
    onScanner: () -> Unit,
    onAssistant: () -> Unit,
    onSettings: () -> Unit,
    onOpenInvoice: (Long) -> Unit
) {
    val viewModel: DashboardViewModel = viewModel(factory = GenericViewModelFactory { DashboardViewModel(repository) })
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("HAN FOOD Ombor", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Sozlamalar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { StatsGrid(state, padding) }

            if (state.lowStock.isNotEmpty()) {
                item { LowStockBanner(count = state.lowStock.size) }
            }

            item {
                Text(
                    "Tezkor amallar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            item { QuickActionsGrid(onQuickAction, onScanner, onAssistant) }

            item {
                Text(
                    "So'nggi harakatlar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (state.recentTransactions.isEmpty()) {
                item {
                    Text(
                        "Hali hech qanday harakat qayd etilmagan",
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
}

@Composable
private fun StatsGrid(state: DashboardUiState, outerPadding: PaddingValues) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                label = "Ombor qiymati",
                value = formatMoney(state.totalStockValue),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Jami dona/birlik",
                value = formatQuantity(state.totalUnits),
                modifier = Modifier.weight(1f),
                accent = MaterialTheme.colorScheme.secondary
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                label = "Mahsulot turlari",
                value = state.productCount.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Mijozlar",
                value = state.clientCount.toString(),
                modifier = Modifier.weight(1f),
                accent = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun LowStockBanner(count: Int) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Text(
                "$count ta mahsulot kam qolgan — Hisobotlar bo'limidan ko'ring",
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private data class QuickAction(val title: String, val icon: ImageVector, val action: String)

@Composable
private fun QuickActionsGrid(onQuickAction: (String) -> Unit, onScanner: () -> Unit, onAssistant: () -> Unit) {
    val actions = listOf(
        QuickAction("Kirim qilish", Icons.Filled.Inventory2, "STOCK_IN"),
        QuickAction("Yuk berish", Icons.Filled.LocalShipping, "STOCK_OUT"),
        QuickAction("Qaytarish", Icons.Filled.Undo, "RETURN"),
        QuickAction("Skanerlash", Icons.Filled.QrCodeScanner, "SCAN"),
        QuickAction("AI Yordamchi", Icons.Filled.SmartToy, "ASSISTANT")
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.aspectRatio(1.55f)
    ) {
        items(actions) { action ->
            Card(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                onClick = {
                    when (action.action) {
                        "SCAN" -> onScanner()
                        "ASSISTANT" -> onAssistant()
                        else -> onQuickAction(action.action)
                    }
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(action.icon, contentDescription = action.title, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        action.title,
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
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column {
                Text(tx.invoiceNumber, fontWeight = FontWeight.SemiBold)
                Text(
                    typeLabel(tx.type) + " • " + formatDateTime(tx.date),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(formatMoney(tx.totalAmount), fontWeight = FontWeight.Medium)
        }
    }
}

private fun typeLabel(type: TransactionType): String = when (type) {
    TransactionType.STOCK_IN -> "Kirim"
    TransactionType.STOCK_OUT -> "Chiqim"
    TransactionType.RETURN -> "Qaytarish"
}
