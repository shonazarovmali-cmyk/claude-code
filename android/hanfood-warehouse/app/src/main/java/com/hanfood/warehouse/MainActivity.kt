package com.hanfood.warehouse

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.hanfood.warehouse.ui.navigation.HanFoodNavGraph
import com.hanfood.warehouse.ui.theme.HanFoodTheme

/**
 * Yagona Activity — barcha ekranlar Jetpack Compose Navigation orqali
 * shu Activity ichida almashadi. BiometricPrompt uchun FragmentActivity'dan
 * meros olingan (AppCompatActivity ham FragmentActivity hisoblanadi).
 *
 * MUHIM: aynan AppCompatActivity (oddiy FragmentActivity emas) bo'lishi
 * shart — AppCompatDelegate.setApplicationLocales() orqali til
 * almashtirish (Sozlamalar → Til) faqat AppCompatActivity'larni kuzatadi
 * va ularni avtomatik qayta yaratadi (API < 33'da); oddiy FragmentActivity
 * bo'lganda til tanlansa ham UI hech qachon yangilanmas edi.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as HanFoodApp

        setContent {
            HanFoodTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    HanFoodNavGraph(app = app, activity = this)
                }
            }
        }
    }
}
