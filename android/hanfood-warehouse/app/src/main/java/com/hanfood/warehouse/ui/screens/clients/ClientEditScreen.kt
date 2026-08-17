package com.hanfood.warehouse.ui.screens.clients

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hanfood.warehouse.R
import com.hanfood.warehouse.data.repository.WarehouseRepository
import com.hanfood.warehouse.ui.components.BackTopBar
import com.hanfood.warehouse.ui.components.ConfirmDeleteDialog
import com.hanfood.warehouse.ui.components.DeleteAction
import com.hanfood.warehouse.ui.navigation.LocationPickerBus
import com.hanfood.warehouse.util.GenericViewModelFactory
import com.hanfood.warehouse.util.GeocodingHelper
import com.hanfood.warehouse.util.LocationHelper
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ClientEditScreen(
    repository: WarehouseRepository,
    clientId: Long,
    onBack: () -> Unit,
    onPickOnMap: () -> Unit
) {
    val viewModel: ClientEditViewModel = viewModel(
        key = "client_edit_$clientId",
        factory = GenericViewModelFactory { ClientEditViewModel(repository, clientId) }
    )
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }
    LaunchedEffect(state.deleted) {
        if (state.deleted) onBack()
    }

    val pickedLocation by LocationPickerBus.result.collectAsState()
    LaunchedEffect(pickedLocation) {
        val picked = pickedLocation
        if (picked != null) {
            viewModel.setLocation(picked.latitude, picked.longitude)
            LocationPickerBus.consumeResult()
        }
    }

    fun geocodeAddress() {
        if (state.address.isBlank()) return
        viewModel.setGeocoding(true)
        scope.launch {
            val result = GeocodingHelper.geocode(context, state.address)
            if (result != null) {
                viewModel.setLocation(result.first, result.second)
            } else {
                viewModel.setGeocoding(false)
                viewModel.update { it.copy(errorRes = R.string.error_geocode_not_found) }
            }
        }
    }

    fun fetchLocation() {
        viewModel.setLocating(true)
        scope.launch {
            val result = LocationHelper.getCurrentLocation(context)
            if (result != null) {
                viewModel.setLocation(result.first, result.second)
            } else {
                viewModel.setLocating(false)
                viewModel.update { it.copy(errorRes = R.string.error_location_unavailable) }
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        val allowed = granted[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            granted[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (allowed) {
            fetchLocation()
        } else {
            viewModel.update { it.copy(errorRes = R.string.error_location_permission_denied) }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = stringResource(R.string.confirm_delete_client_title),
            message = stringResource(R.string.confirm_delete_client_message),
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
                title = stringResource(if (clientId == 0L) R.string.client_edit_title_new else R.string.client_edit_title_edit),
                onBack = onBack,
                actions = {
                    if (clientId != 0L) {
                        DeleteAction(
                            contentDescription = stringResource(R.string.action_delete_client),
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
            OutlinedTextField(
                value = state.name,
                onValueChange = { v -> viewModel.update { it.copy(name = v) } },
                label = { Text(stringResource(R.string.client_field_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.phone,
                onValueChange = { v -> viewModel.update { it.copy(phone = v) } },
                label = { Text(stringResource(R.string.client_field_phone)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            OutlinedTextField(
                value = state.address,
                onValueChange = { v -> viewModel.update { it.copy(address = v) } },
                label = { Text(stringResource(R.string.client_field_address)) },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text(stringResource(R.string.client_address_geocode_hint)) },
                trailingIcon = {
                    if (state.geocoding) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = { geocodeAddress() }, enabled = state.address.isNotBlank()) {
                            Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.action_find_on_map))
                        }
                    }
                }
            )
            OutlinedTextField(
                value = state.note,
                onValueChange = { v -> viewModel.update { it.copy(note = v) } },
                label = { Text(stringResource(R.string.client_field_note)) },
                modifier = Modifier.fillMaxWidth()
            )

            ClientLocationSection(
                latitude = state.latitude,
                longitude = state.longitude,
                locating = state.locating,
                onCapture = {
                    viewModel.update { it.copy(errorRes = null) }
                    if (LocationHelper.hasLocationPermission(context)) {
                        fetchLocation()
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    }
                },
                onPickOnMap = {
                    LocationPickerBus.setInitial(state.latitude, state.longitude)
                    onPickOnMap()
                },
                onClear = { viewModel.clearLocation() },
                onViewOnMap = { lat, lon -> LocationHelper.openInMaps(context, lat, lon, state.name) }
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

@Composable
private fun ClientLocationSection(
    latitude: Double?,
    longitude: Double?,
    locating: Boolean,
    onCapture: () -> Unit,
    onPickOnMap: () -> Unit,
    onClear: () -> Unit,
    onViewOnMap: (Double, Double) -> Unit
) {
    if (latitude != null && longitude != null) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.client_field_location),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            String.format(Locale.US, "%.6f, %.6f", latitude, longitude),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onClear) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_remove_location))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onViewOnMap(latitude, longitude) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(stringResource(R.string.action_view_on_map), modifier = Modifier.padding(start = 8.dp))
                    }
                    TextButton(onClick = onPickOnMap) {
                        Icon(Icons.Filled.EditLocationAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(stringResource(R.string.action_adjust_on_map), modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onCapture, enabled = !locating, modifier = Modifier.fillMaxWidth()) {
                if (locating) {
                    Box(modifier = Modifier.size(18.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    }
                    Text(stringResource(R.string.location_capturing), modifier = Modifier.padding(start = 8.dp))
                } else {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.action_get_location), modifier = Modifier.padding(start = 8.dp))
                }
            }
            OutlinedButton(onClick = onPickOnMap, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(stringResource(R.string.action_pick_on_map), modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
