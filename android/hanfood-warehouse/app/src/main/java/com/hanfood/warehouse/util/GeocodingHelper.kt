package com.hanfood.warehouse.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Turns a typed address into map coordinates ("find location from address" —
 * the trailing search icon on the client address field). Uses OpenStreetMap's
 * free Nominatim search API — no Google Maps API key or billing account
 * needed, matching the rest of the app's zero-paid-dependency approach.
 *
 * Only called on an explicit user tap (never per-keystroke), which keeps
 * request volume well within Nominatim's public usage policy — a custom
 * User-Agent (required by that policy) is set via [org.osmdroid.config.Configuration]
 * in [com.hanfood.warehouse.HanFoodApp], reused here.
 */
object GeocodingHelper {

    private const val ENDPOINT = "https://nominatim.openstreetmap.org/search"

    /** Returns the best-matching (latitude, longitude) for [query], or null if not found/failed. */
    suspend fun geocode(context: Context, query: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext null
        try {
            val encoded = URLEncoder.encode(query.trim(), "UTF-8")
            val url = URL("$ENDPOINT?format=json&limit=1&q=$encoded")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", context.packageName)
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.inputStream.bufferedReader().use { reader ->
                val body = reader.readText()
                val results = JSONArray(body)
                if (results.length() == 0) return@withContext null
                val first = results.getJSONObject(0)
                first.getString("lat").toDouble() to first.getString("lon").toDouble()
            }
        } catch (e: Exception) {
            null
        }
    }
}
