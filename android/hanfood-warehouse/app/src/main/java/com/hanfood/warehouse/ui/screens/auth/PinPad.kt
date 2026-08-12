package com.hanfood.warehouse.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** 4 xonali PIN uchun nuqta indikatori. */
@Composable
fun PinDots(length: Int, maxLength: Int = 4, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        repeat(maxLength) { index ->
            val filled = index < length
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
            )
        }
    }
}

/** Raqamli PIN klaviaturasi (0-9 + backspace). */
@Composable
fun NumericKeypad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9')
    )
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { digit ->
                    KeypadButton(text = digit.toString(), modifier = Modifier.weight(1f)) { onDigit(digit) }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f))
            KeypadButton(text = "0", modifier = Modifier.weight(1f)) { onDigit('0') }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                IconButton(onClick = onBackspace) {
                    Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "O'chirish")
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .aspectRatio(1.4f)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(text, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium)
        }
    }
}
