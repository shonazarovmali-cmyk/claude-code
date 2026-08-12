package com.hanfood.warehouse.ui.screens.invoices

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hanfood.warehouse.data.local.entity.TransactionItemDetail
import com.hanfood.warehouse.data.local.entity.TransactionType
import com.hanfood.warehouse.data.repository.WarehouseRepository
import com.hanfood.warehouse.ui.components.BackTopBar
import com.hanfood.warehouse.util.GenericViewModelFactory
import com.hanfood.warehouse.util.InvoicePdfExporter
import com.hanfood.warehouse.util.formatDateTime
import com.hanfood.warehouse.util.formatMoney
import com.hanfood.warehouse.util.formatQuantity

@Composable
fun InvoiceDetailScreen(
    repository: WarehouseRepository,
    transactionId: Long,
    onBack: () -> Unit
) {
    val viewModel: InvoiceDetailViewModel = viewModel(
        key = "invoice_detail_$transactionId",
        factory = GenericViewModelFactory { InvoiceDetailViewModel(repository, transactionId) }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            BackTopBar(
                title = state.transaction?.invoiceNumber ?: "Faktura",
                onBack = onBack,
                actions = {
                    if (state.transaction != null) {
                        IconButton(onClick = {
                            val uri = InvoicePdfExporter.export(
                                context = context,
                                transaction = state.transaction!!,
                                counterpartyLabel = state.counterpartyLabel,
                                counterpartyName = state.counterpartyName,
                                items = state.items
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Fakturani ulashish"))
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "Ulashish")
                        }
                    }
                }
            )
        }
    ) { padding ->
        val tx = state.transaction
        if (tx == null) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text(if (state.loading) "Yuklanmoqda..." else "Faktura topilmadi")
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                InfoRow("Turi", typeLabel(tx.type))
                InfoRow("Sana", formatDateTime(tx.date))
                InfoRow(state.counterpartyLabel, state.counterpartyName)
                if (!tx.note.isNullOrBlank()) InfoRow("Izoh", tx.note)
            }
            Divider()
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.items, key = { it.id }) { item -> InvoiceItemRow(item) }
            }
            Divider()
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Jami summa", style = MaterialTheme.typography.titleMedium)
                Text(formatMoney(tx.totalAmount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("$label:", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun InvoiceItemRow(item: TransactionItemDetail) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(item.productName, fontWeight = FontWeight.Medium)
                Text(
                    "${formatQuantity(item.quantity)} ${item.unit} × ${formatMoney(item.unitPrice)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(formatMoney(item.lineTotal), fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun typeLabel(type: TransactionType): String = when (type) {
    TransactionType.STOCK_IN -> "Kirim"
    TransactionType.STOCK_OUT -> "Chiqim (yuk berish)"
    TransactionType.RETURN -> "Qaytarish"
}
