package com.hanfood.warehouse.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hanfood.warehouse.R
import com.hanfood.warehouse.ui.theme.BrandGreen

/**
 * Clean, light brand header used at the top of primary screens — shows the
 * real HAN FOOD wordmark (ornament + "HAN FOOD") at a size where it's
 * actually legible, on a plain white surface (MoySklad-style business-app
 * look) instead of a dark banner that buries the logo.
 */
@Composable
fun BrandHeader(
    trailingIcon: ImageVector? = null,
    trailingContentDescription: String? = null,
    onTrailingClick: (() -> Unit)? = null
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.han_food_header_lockup),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier.height(46.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                if (trailingIcon != null && onTrailingClick != null) {
                    IconButton(onClick = onTrailingClick) {
                        Icon(trailingIcon, contentDescription = trailingContentDescription, tint = BrandGreen)
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}
