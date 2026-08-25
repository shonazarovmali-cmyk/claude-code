package com.hanfood.warehouse.ui.screens.invoices

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.hanfood.warehouse.R
import com.hanfood.warehouse.data.local.entity.TransactionItemDetail
import com.hanfood.warehouse.data.local.entity.TransactionType
import com.hanfood.warehouse.data.local.entity.toAttachmentList
import com.hanfood.warehouse.data.repository.WarehouseRepository
import com.hanfood.warehouse.ui.components.BackTopBar
import com.hanfood.warehouse.ui.components.ConfirmDeleteDialog
import com.hanfood.warehouse.ui.components.DeleteAction
import com.hanfood.warehouse.ui.components.transactionTypeLabel
import com.hanfood.warehouse.util.FileStorage
import com.hanfood.warehouse.util.GenericViewModelFactory
import com.hanfood.warehouse.util.InvoicePdfExporter
import com.hanfood.warehouse.util.formatDateTime
import com.hanfood.warehouse.util.formatMoney
import com.hanfood.warehouse.util.formatQuantity
import java.io.File

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
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.deleted) {
        if (state.deleted) onBack()
    }

    val counterpartyLabelText = stringResource(
        if (state.counterpartyKind == CounterpartyKind.CLIENT) R.string.counterparty_label_client else R.string.counterparty_label_supplier
    )
    val counterpartyNameText = when (val c = state.counterparty) {
        is CounterpartyName.Known -> c.name
        CounterpartyName.UnknownClient -> stringResource(R.string.counterparty_unknown_client)
        CounterpartyName.NoSupplier -> stringResource(R.string.value_not_specified)
    }
    val shareTitle = stringResource(R.string.action_share_invoice)

    Scaffold(
        topBar = {
            BackTopBar(
                title = state.transaction?.let { it.title?.takeIf { t -> t.isNotBlank() } ?: it.invoiceNumber }
                    ?: stringResource(R.string.invoice_detail_title_fallback),
                onBack = onBack,
                actions = {
                    if (state.transaction != null) {
                        IconButton(onClick = {
                            val uri = InvoicePdfExporter.export(
                                context = context,
                                transaction = state.transaction!!,
                                counterpartyLabel = counterpartyLabelText,
                                counterpartyName = counterpartyNameText,
                                items = state.items
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, shareTitle))
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.action_share))
                        }
                        DeleteAction(
                            contentDescription = stringResource(R.string.action_delete_invoice),
                            onClick = { showDeleteConfirm = true }
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (showDeleteConfirm) {
            ConfirmDeleteDialog(
                title = stringResource(R.string.confirm_delete_invoice_title),
                message = stringResource(R.string.confirm_delete_invoice_message),
                onConfirm = {
                    showDeleteConfirm = false
                    viewModel.delete()
                },
                onDismiss = { showDeleteConfirm = false }
            )
        }

        val tx = state.transaction
        if (tx == null) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text(stringResource(if (state.loading) R.string.state_loading else R.string.invoice_not_found))
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                InfoRow(stringResource(R.string.invoice_field_type), transactionTypeLabel(tx.type))
                InfoRow(stringResource(R.string.invoice_field_date), formatDateTime(tx.date))
                InfoRow(counterpartyLabelText, counterpartyNameText)
                if (!tx.note.isNullOrBlank()) InfoRow(stringResource(R.string.invoice_field_note), tx.note)

                val attachments = tx.attachmentPaths.toAttachmentList()
                if (attachments.isNotEmpty()) {
                    Text(
                        stringResource(R.string.invoice_attachments_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        items(attachments, key = { it }) { path -> AttachmentPreview(path) }
                    }
                }
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
                Text(stringResource(R.string.invoice_total_label), style = MaterialTheme.typography.titleMedium)
                Text(formatMoney(tx.totalAmount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private val imageExtensions = setOf("jpg", "jpeg", "png", "webp")

@Composable
private fun AttachmentPreview(path: String) {
    val context = LocalContext.current
    val isImage = File(path).extension.lowercase() in imageExtensions
    val openLabel = stringResource(R.string.cd_open_attachment)

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable {
                val uri = FileStorage.contentUriFor(context, path)
                val mimeType = if (isImage) "image/*" else "*/*"
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                runCatching { context.startActivity(intent) }
            },
        contentAlignment = Alignment.Center
    ) {
        if (isImage) {
            AsyncImage(
                model = File(path),
                contentDescription = openLabel,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(Icons.Filled.InsertDriveFile, contentDescription = openLabel)
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
