package com.hanfood.warehouse.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.hanfood.warehouse.BuildConfig
import com.hanfood.warehouse.R
import com.hanfood.warehouse.security.BiometricHelper
import com.hanfood.warehouse.security.PinManager
import com.hanfood.warehouse.ui.components.BackTopBar
import com.hanfood.warehouse.util.AppLanguage
import com.hanfood.warehouse.util.DatabaseExporter
import com.hanfood.warehouse.util.LanguageManager
import com.hanfood.warehouse.util.PricingSettings

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
    var showLanguageDialog by remember { mutableStateOf(false) }
    var currentLanguage by remember { mutableStateOf(LanguageManager.currentLanguage()) }
    val backupShareTitle = stringResource(R.string.settings_backup_share_title)
    var eurPlnRateText by remember { mutableStateOf(PricingSettings.getEurToPlnRate(context).toPlainText()) }
    var markupPercentText by remember { mutableStateOf(PricingSettings.getDefaultMarkupPercent(context).toPlainText()) }

    Scaffold(topBar = { BackTopBar(title = stringResource(R.string.settings_title), onBack = onBack) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

            SettingsSection(title = stringResource(R.string.settings_section_language)) {
                SettingsRow(
                    title = stringResource(R.string.settings_section_language),
                    subtitle = currentLanguage.displayName.ifBlank { stringResource(R.string.settings_language_system) },
                    onClick = { showLanguageDialog = true }
                )
            }

            SettingsSection(title = stringResource(R.string.settings_section_pricing)) {
                Text(
                    stringResource(R.string.settings_pricing_subtitle),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
                OutlinedTextField(
                    value = eurPlnRateText,
                    onValueChange = { value ->
                        eurPlnRateText = value
                        value.toFloatOrNull()?.let { PricingSettings.setEurToPlnRate(context, it) }
                    },
                    label = { Text(stringResource(R.string.settings_eur_pln_rate)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = markupPercentText,
                    onValueChange = { value ->
                        markupPercentText = value
                        value.toFloatOrNull()?.let { PricingSettings.setDefaultMarkupPercent(context, it) }
                    },
                    label = { Text(stringResource(R.string.settings_default_markup)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            SettingsSection(title = stringResource(R.string.settings_section_security)) {
                SettingsRow(
                    title = stringResource(R.string.settings_change_pin),
                    subtitle = stringResource(R.string.settings_change_pin_subtitle),
                    onClick = onChangePin
                )
                if (biometricAvailable) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.settings_biometric_title))
                            Text(
                                stringResource(R.string.settings_biometric_subtitle),
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

            SettingsSection(title = stringResource(R.string.settings_section_data)) {
                SettingsRow(
                    title = stringResource(R.string.settings_backup),
                    subtitle = stringResource(R.string.settings_backup_subtitle),
                    onClick = {
                        val uri = DatabaseExporter.exportDatabase(context) ?: return@SettingsRow
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/octet-stream"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, backupShareTitle))
                    }
                )
            }

            SettingsSection(title = stringResource(R.string.settings_section_about)) {
                Image(
                    painter = painterResource(R.drawable.han_food_logo),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .heightIn(max = 90.dp)
                )
                Text(
                    stringResource(R.string.settings_about_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    stringResource(R.string.settings_about_body),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }

    if (showLanguageDialog) {
        LanguageDialog(
            current = currentLanguage,
            onDismiss = { showLanguageDialog = false },
            onSelect = { language ->
                LanguageManager.setLanguage(language)
                currentLanguage = language
                showLanguageDialog = false
            }
        )
    }
}

@Composable
private fun LanguageDialog(
    current: AppLanguage,
    onDismiss: () -> Unit,
    onSelect: (AppLanguage) -> Unit
) {
    val systemLabel = stringResource(R.string.settings_language_system)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_language_dialog_title)) },
        text = {
            Column {
                AppLanguage.entries.forEach { language ->
                    val label = if (language == AppLanguage.SYSTEM) systemLabel else language.displayName
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(language) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = current == language, onClick = { onSelect(language) })
                        Text(label, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        }
    )
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

private fun Float.toPlainText(): String =
    if (this == this.toLong().toFloat()) this.toLong().toString() else this.toString()
