package com.hanfood.warehouse.ui.screens.clients

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanfood.warehouse.R
import com.hanfood.warehouse.data.local.entity.Client
import com.hanfood.warehouse.data.repository.WarehouseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClientListViewModel(private val repository: WarehouseRepository) : ViewModel() {

    val query = MutableStateFlow("")

    val clients: StateFlow<List<Client>> = query
        .flatMapLatest { q -> if (q.isBlank()) repository.clients else repository.searchClients(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun archive(client: Client) {
        viewModelScope.launch { repository.archiveClient(client) }
    }
}

data class ClientEditUiState(
    val id: Long = 0L,
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val note: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locating: Boolean = false,
    val geocoding: Boolean = false,
    val loaded: Boolean = false,
    val saved: Boolean = false,
    val deleted: Boolean = false,
    @StringRes val errorRes: Int? = null
)

class ClientEditViewModel(
    private val repository: WarehouseRepository,
    private val clientId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(ClientEditUiState(loaded = clientId == 0L))
    val state: StateFlow<ClientEditUiState> = _state

    init {
        if (clientId != 0L) {
            viewModelScope.launch {
                val client = repository.getClient(clientId)
                if (client != null) {
                    _state.value = ClientEditUiState(
                        id = client.id,
                        name = client.name,
                        phone = client.phone.orEmpty(),
                        address = client.address.orEmpty(),
                        note = client.note.orEmpty(),
                        latitude = client.latitude,
                        longitude = client.longitude,
                        loaded = true
                    )
                } else {
                    _state.value = _state.value.copy(loaded = true, errorRes = R.string.error_client_not_found)
                }
            }
        }
    }

    fun update(transform: (ClientEditUiState) -> ClientEditUiState) {
        _state.value = transform(_state.value)
    }

    fun setLocating(value: Boolean) {
        _state.value = _state.value.copy(locating = value)
    }

    fun setGeocoding(value: Boolean) {
        _state.value = _state.value.copy(geocoding = value)
    }

    fun setLocation(latitude: Double?, longitude: Double?) {
        _state.value = _state.value.copy(latitude = latitude, longitude = longitude, locating = false, geocoding = false)
    }

    fun clearLocation() {
        _state.value = _state.value.copy(latitude = null, longitude = null)
    }

    fun delete() {
        if (clientId == 0L) return
        viewModelScope.launch {
            val client = repository.getClient(clientId) ?: return@launch
            repository.archiveClient(client)
            _state.value = _state.value.copy(deleted = true)
        }
    }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.value = s.copy(errorRes = R.string.error_client_name_required)
            return
        }
        viewModelScope.launch {
            repository.upsertClient(
                Client(
                    id = s.id,
                    name = s.name.trim(),
                    phone = s.phone.trim().ifBlank { null },
                    address = s.address.trim().ifBlank { null },
                    note = s.note.trim().ifBlank { null },
                    latitude = s.latitude,
                    longitude = s.longitude
                )
            )
            _state.value = _state.value.copy(saved = true, errorRes = null)
        }
    }
}
