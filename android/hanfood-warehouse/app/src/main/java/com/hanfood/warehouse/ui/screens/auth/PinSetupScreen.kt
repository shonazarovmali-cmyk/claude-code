package com.hanfood.warehouse.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

/**
 * Birinchi marta ishga tushirishda yoki PIN o'zgartirishda ko'rsatiladigan
 * ekran: foydalanuvchi 4 xonali kodni ikki marta kiritadi (tasdiqlash uchun).
 */
@Composable
fun PinSetupScreen(pinManager: PinManager, onDone: () -> Unit) {
    var stage by remember { mutableStateOf(SetupStage.ENTER) }
    var firstPin by remember { mutableStateOf("") }
    var currentInput by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    val mismatchError = stringResource(R.string.pin_setup_mismatch)

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                text = when (stage) {
                    SetupStage.ENTER -> stringResource(R.string.pin_setup_title)
                    SetupStage.CONFIRM -> stringResource(R.string.pin_setup_confirm)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )
            PinDots(length = currentInput.length)
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
                        if (currentInput.length < 4) {
                            currentInput += digit
                            errorText = null
                            if (currentInput.length == 4) {
                                when (stage) {
                                    SetupStage.ENTER -> {
                                        firstPin = currentInput
                                        currentInput = ""
                                        stage = SetupStage.CONFIRM
                                    }
                                    SetupStage.CONFIRM -> {
                                        if (currentInput == firstPin) {
                                            pinManager.setPin(currentInput)
                                            onDone()
                                        } else {
                                            errorText = mismatchError
                                            firstPin = ""
                                            currentInput = ""
                                            stage = SetupStage.ENTER
                                        }
                                    }
                                }
                            }
                        }
                    },
                    onBackspace = { if (currentInput.isNotEmpty()) currentInput = currentInput.dropLast(1) }
                )
            }
        }
    }
}

private enum class SetupStage { ENTER, CONFIRM }
