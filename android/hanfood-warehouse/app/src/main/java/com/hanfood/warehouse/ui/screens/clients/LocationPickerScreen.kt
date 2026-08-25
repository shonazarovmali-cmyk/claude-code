package com.hanfood.warehouse.ui.screens.clients

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.hanfood.warehouse.R
import com.hanfood.warehouse.ui.components.BackTopBar
import com.hanfood.warehouse.ui.navigation.LocationPickerBus
import com.hanfood.warehouse.util.LocationHelper
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

/** Fallback starting point when there's no saved location and GPS is unavailable/denied. */
private val DEFAULT_CENTER = GeoPoint(41.2995, 69.2401) // Tashkent
private const val DEFAULT_ZOOM = 15.0

/**
 * Full-screen map for manually marking a client's location — the classic
 * "drag the map, the pin always stays dead-center" pattern (Uber/Bolt/Yandex
 * style), which avoids fiddly tap-to-geo-coordinate conversion. Built on
 * osmdroid (OpenStreetMap tiles) rather than Google Maps, so no Maps API
 * key or billing account is required.
 */
@Composable
fun LocationPickerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mapViewState = remember { mutableStateOf<MapView?>(null) }

    DisposableEffect(Unit) {
        onDispose { mapViewState.value?.onDetach() }
    }

    LaunchedEffect(Unit) {
        val initial = LocationPickerBus.consumeInitial()
        val target = when {
            initial != null -> GeoPoint(initial.latitude, initial.longitude)
            LocationHelper.hasLocationPermission(context) -> {
                val current = LocationHelper.getCurrentLocation(context)
                if (current != null) GeoPoint(current.first, current.second) else null
            }
            else -> null
        }
        if (target != null) {
            mapViewState.value?.controller?.setCenter(target)
        }
    }

    Scaffold(
        topBar = {
            BackTopBar(title = stringResource(R.string.location_picker_title), onBack = onBack)
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    Configuration.getInstance()
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(DEFAULT_ZOOM)
                        controller.setCenter(DEFAULT_CENTER)
                        mapViewState.value = this
                    }
                }
            )

            // Fixed center pin — the map pans underneath it; its bottom tip (not
            // its visual center) marks the actual geo-point, so it's nudged up
            // by roughly half its own height.
            Icon(
                Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 20.dp)
                    .size(44.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 4.dp
                ) {
                    Text(
                        stringResource(R.string.location_picker_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Button(
                    onClick = {
                        val center = mapViewState.value?.mapCenter
                        if (center != null) {
                            scope.launch {
                                LocationPickerBus.emitResult(center.latitude, center.longitude)
                                onBack()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.action_confirm_location))
                }
            }
        }
    }
}
