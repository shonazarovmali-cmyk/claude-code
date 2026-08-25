package com.hanfood.warehouse.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hanfood.warehouse.R
import com.hanfood.warehouse.security.PinManager

/** Ilova ochilganda ko'rsatiladigan qulf ekrani — PIN yoki barmoq izi bilan. */
@Composable
fun PinUnlockScreen(
    pinManager: PinManager,
    biometricAvailable: Boolean,
    onUnlocked: () -> Unit,
    onRequestBiometric: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    val biometricEnabled = pinManager.isBiometricEnabled && biometricAvailable
    val wrongPinError = stringResource(R.string.pin_unlock_wrong)

    LaunchedEffect(Unit) {
        if (biometricEnabled) onRequestBiometric()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.pin_unlock_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )
            PinDots(length = input.length)
            if (errorText != null) {
                Text(
                    errorText!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            Column(modifier = Modifier.padding(top = 40.dp)) {
                NumericKeypad(
                    onDigit = { digit ->
                        if (input.length < 4) {
                            input += digit
                            errorText = null
                            if (input.length == 4) {
                                if (pinManager.verifyPin(input)) {
                                    onUnlocked()
                                } else {
                                    errorText = wrongPinError
                                    input = ""
                                }
                            }
                        }
                    },
                    onBackspace = { if (input.isNotEmpty()) input = input.dropLast(1) }
                )
            }
            if (biometricEnabled) {
                IconButton(onClick = onRequestBiometric, modifier = Modifier.padding(top = 20.dp)) {
                    Icon(
                        Icons.Filled.Fingerprint,
                        contentDescription = stringResource(R.string.cd_biometric_unlock),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
