package com.hanfood.warehouse.ui.screens.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hanfood.warehouse.data.repository.WarehouseRepository
import com.hanfood.warehouse.ui.components.BackTopBar
import com.hanfood.warehouse.util.GenericViewModelFactory

@Composable
fun ClientEditScreen(
    repository: WarehouseRepository,
    clientId: Long,
    onBack: () -> Unit
) {
    val viewModel: ClientEditViewModel = viewModel(
        key = "client_edit_$clientId",
        factory = GenericViewModelFactory { ClientEditViewModel(repository, clientId) }
    )
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Scaffold(
        topBar = { BackTopBar(title = if (clientId == 0L) "Yangi mijoz" else "Mijozni tahrirlash", onBack = onBack) }
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
                label = { Text("Mijoz nomi *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.phone,
                onValueChange = { v -> viewModel.update { it.copy(phone = v) } },
                label = { Text("Telefon raqami") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            OutlinedTextField(
                value = state.address,
                onValueChange = { v -> viewModel.update { it.copy(address = v) } },
                label = { Text("Manzil") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.note,
                onValueChange = { v -> viewModel.update { it.copy(note = v) } },
                label = { Text("Izoh (ixtiyoriy)") },
                modifier = Modifier.fillMaxWidth()
            )

            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }

            Button(onClick = { viewModel.save() }, modifier = Modifier.fillMaxWidth()) {
                Text("Saqlash")
            }
        }
    }
}
