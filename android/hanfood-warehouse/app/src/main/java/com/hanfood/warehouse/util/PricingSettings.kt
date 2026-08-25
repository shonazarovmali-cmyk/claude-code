package com.hanfood.warehouse.util

import android.content.Context

/**
 * Persisted global pricing defaults for wholesale/import products — the
 * EUR→PLN exchange rate and the default markup percentage used when
 * suggesting a sell price from a purchase price. Plain (unencrypted)
 * SharedPreferences: these aren't sensitive values, just numbers the user
 * tunes occasionally in Settings.
 */
object PricingSettings {

    private const val PREFS_NAME = "pricing_settings"
    private const val KEY_EUR_PLN_RATE = "eur_pln_rate"
    private const val KEY_DEFAULT_MARKUP_PERCENT = "default_markup_percent"

    private const val DEFAULT_EUR_PLN_RATE = 4.30f
    private const val DEFAULT_MARKUP_PERCENT = 50f

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getEurToPlnRate(context: Context): Float = prefs(context).getFloat(KEY_EUR_PLN_RATE, DEFAULT_EUR_PLN_RATE)

    fun setEurToPlnRate(context: Context, rate: Float) {
        prefs(context).edit().putFloat(KEY_EUR_PLN_RATE, rate).apply()
    }

    fun getDefaultMarkupPercent(context: Context): Float = prefs(context).getFloat(KEY_DEFAULT_MARKUP_PERCENT, DEFAULT_MARKUP_PERCENT)

    fun setDefaultMarkupPercent(context: Context, percent: Float) {
        prefs(context).edit().putFloat(KEY_DEFAULT_MARKUP_PERCENT, percent).apply()
    }
}
