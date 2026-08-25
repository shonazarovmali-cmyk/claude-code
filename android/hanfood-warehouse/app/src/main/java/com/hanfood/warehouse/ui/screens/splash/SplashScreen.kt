package com.hanfood.warehouse.ui.screens.splash

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hanfood.warehouse.R
import com.hanfood.warehouse.ui.theme.LogoBackdrop
import kotlinx.coroutines.delay

/**
 * Brand entrance screen shown once per cold start, before the PIN gate.
 * Fades/scales the real HAN FOOD logo in, holds briefly, then hands off.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 650, easing = EaseOutCubic),
        label = "splashAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.9f,
        animationSpec = tween(durationMillis = 650, easing = EaseOutCubic),
        label = "splashScale"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(1500)
        onFinished()
    }

    // The logo artwork is a fixed dark-ink-on-cream lockup (not theme-adaptive), so the
    // splash background is pinned to the brand cream regardless of light/dark system theme.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LogoBackdrop),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.han_food_logo),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .alpha(alpha)
                .scale(scale)
        )
    }
}
