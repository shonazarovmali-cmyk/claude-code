package com.hanfood.warehouse.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.hanfood.warehouse.BuildConfig
import com.hanfood.warehouse.security.BiometricHelper
import com.hanfood.warehouse.security.PinManager
import com.hanfood.warehouse.ui.components.BackTopBar
import com.hanfood.warehouse.util.DatabaseExporter

@Composable
fun SettingsScreen(
    pinManager: PinManager,
    onBack: () -> Unit,
    onChangePin: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val biometricAvailable = activity?.let { BiometricHelper.isAvailable(it) } ?: false
    var biometricEnabled by remember { mutableStateOf(pinManager.isBiometricEnabled) }

    Scaffold(topBar = { BackTopBar(title = "Sozlamalar", onBack = onBack) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

            SettingsSection(title = "Xavfsizlik") {
                SettingsRow(title = "PIN-kodni o'zgartirish", subtitle = "4 xonali kirish kodi", onClick = onChangePin)
                if (biometricAvailable) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Barmoq izi bilan kirish")
                            Text(
                                "PIN o'rniga biometrik autentifikatsiyadan foydalanish",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = {
                                biometricEnabled = it
                                pinManager.isBiometricEnabled = it
                            }
                        )
                    }
                }
            }

            SettingsSection(title = "Ma'lumotlar") {
                SettingsRow(
                    title = "Zaxira nusxa yaratish",
                    subtitle = "Ombor bazasini fayl sifatida ulashish (Telegram, Drive va h.k.)",
                    onClick = {
                        val uri = DatabaseExporter.exportDatabase(context) ?: return@SettingsRow
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/octet-stream"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Zaxira nusxani ulashish"))
                    }
                )
            }

            SettingsSection(title = "Ilova haqida") {
                Text("HAN FOOD Ombor v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Yuk kirim-chiqimi, mijozlar, fakturalar, hisobotlar, shtrix-kod skaneri va AI tahlil yordamchisi bilan ombor boshqaruvi. Barcha ma'lumotlar shu qurilmada, internetsiz saqlanadi.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) { content() }
        }
    }
}

@Composable
private fun SettingsRow(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title)
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
