package com.hanfood.warehouse

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.hanfood.warehouse.ui.navigation.HanFoodNavGraph
import com.hanfood.warehouse.ui.theme.HanFoodTheme

/**
 * Yagona Activity — barcha ekranlar Jetpack Compose Navigation orqali
 * shu Activity ichida almashadi. BiometricPrompt uchun FragmentActivity'dan
 * meros olingan.
 */
class MainActivity : FragmentActivity() {
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
