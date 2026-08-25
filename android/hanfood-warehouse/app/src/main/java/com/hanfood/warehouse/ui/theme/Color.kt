package com.hanfood.warehouse.ui.theme

import androidx.compose.ui.graphics.Color

// HAN FOOD brand palette. Per the client's direction the app itself should
// read as green-and-white (matching the wordmark's ink and the logo's light
// backdrop); the ornament's teal/gold/navy are kept only as small brand
// accents (header gradient, launcher icon), not as the app's dominant colors.

val BrandGreen = Color(0xFF1E6F4C)
val BrandGreenDark = Color(0xFF123F2C)
val BrandGreenLight = Color(0xFFDCEEE3)
val BrandInk = Color(0xFF0B1F16)          // near-black green — wordmark, header gradient

// Ornament accent colors (used sparingly: header watermark, launcher icon).
val BrandTeal = Color(0xFF04808C)
val BrandTealDeep = Color(0xFF045A64)
val BrandTealLight = Color(0xFFBFE6E8)
val BrandGold = Color(0xFF9C7A3D)
val BrandGoldLight = Color(0xFFE8D9B8)
val BrandNavy = Color(0xFF0A3D5C)
val BrandTerracotta = Color(0xFFC1652F)

// Matches the cream backdrop baked into the extracted logo bitmap (used only
// behind that image on the splash screen, so its anti-aliased edges blend
// seamlessly — the rest of the app uses AppBackground/white instead).
val LogoBackdrop = Color(0xFFF6EFDF)

val AppBackground = Color(0xFFFFFFFF)
val AppSurface = Color(0xFFFFFFFF)
val AppSurfaceVariant = Color(0xFFEAF2ED)

val CharcoalBackground = Color(0xFF0E1913)
val CharcoalSurface = Color(0xFF16241C)
val CharcoalSurfaceVariant = Color(0xFF203024)

val DangerRed = Color(0xFFB3402A)
val WarningAmber = Color(0xFFA5762A)
val SuccessGreen = Color(0xFF2E7D5B)

val TextPrimaryLight = Color(0xFF162019)
val TextPrimaryDark = Color(0xFFE8F1EA)
val TextSecondaryLight = Color(0xFF5C6B62)
val TextSecondaryDark = Color(0xFFA9B6AC)
