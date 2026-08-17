package com.hanfood.warehouse.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Display/heading sizes use the system serif family to echo the elegant
// serif wordmark in the HAN FOOD logo; body/UI text stays on the default
// sans-serif for readability at small sizes.
private val DisplayFont = FontFamily.Serif

val HanFoodTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.2.sp
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 27.sp
    ),
    titleMedium = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 23.sp
    ),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp)
)
