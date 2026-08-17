package com.hanfood.warehouse.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hanfood.warehouse.R
import com.hanfood.warehouse.ui.theme.BrandGreen
import com.hanfood.warehouse.ui.theme.BrandGreenDark
import com.hanfood.warehouse.ui.theme.BrandInk

/**
 * Dark green gradient brand header used at the top of primary screens —
 * carries the HAN FOOD identity (ornament watermark + white wordmark)
 * instead of a plain Material app bar.
 */
@Composable
fun BrandHeader(
    title: String,
    subtitle: String? = null,
    trailingIcon: ImageVector? = null,
    trailingContentDescription: String? = null,
    onTrailingClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(BrandInk, BrandGreenDark, BrandGreen))
            )
    ) {
        // Large, faint ornament watermark bleeding off the top-right corner.
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopEnd)
                .alpha(0.16f)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = androidx.compose.ui.graphics.Color.White
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.75f)
                    )
                }
            }

            if (trailingIcon != null && onTrailingClick != null) {
                IconButton(onClick = onTrailingClick) {
                    Icon(trailingIcon, contentDescription = trailingContentDescription, tint = androidx.compose.ui.graphics.Color.White)
                }
            }
        }
    }
}
