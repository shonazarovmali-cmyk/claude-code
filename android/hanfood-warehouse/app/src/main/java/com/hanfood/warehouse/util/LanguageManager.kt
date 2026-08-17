package com.hanfood.warehouse.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Supported in-app languages. Display names are intentionally shown in the
 * language itself (a standard UX convention for language pickers), so they
 * are not pulled from string resources — a Russian speaker should see
 * "Русский" no matter what the app's current language is.
 */
enum class AppLanguage(val tag: String, val displayName: String) {
    SYSTEM("", ""),
    ENGLISH("en", "English"),
    UZBEK("uz", "O'zbekcha"),
    RUSSIAN("ru", "Русский"),
    POLISH("pl", "Polski"),
    TURKISH("tr", "Türkçe"),
    UKRAINIAN("uk", "Українська"),
    GERMAN("de", "Deutsch")
}

/**
 * Wraps AndroidX's per-app language API (`AppCompatDelegate.setApplicationLocales`).
 * Works on all supported API levels (24+): on Android 13+ it's backed by the
 * platform's native per-app language settings; below that, AppCompat stores
 * the choice itself (enabled via the `autoStoreLocales` manifest meta-data)
 * and re-applies it on the next app start / activity recreation.
 */
object LanguageManager {

    fun setLanguage(language: AppLanguage) {
        val locales = if (language == AppLanguage.SYSTEM) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language.tag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }

    fun currentLanguage(): AppLanguage {
        val current = AppCompatDelegate.getApplicationLocales()
        if (current.isEmpty) return AppLanguage.SYSTEM
        val tag = current[0]?.language
        return AppLanguage.entries.firstOrNull { it.tag == tag } ?: AppLanguage.SYSTEM
    }
}
