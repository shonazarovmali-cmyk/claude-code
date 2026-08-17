package com.hanfood.warehouse.util

import androidx.annotation.StringRes

/**
 * A localizable UI message: a string resource id plus format arguments.
 * ViewModels build these instead of raw Strings so the actual translated
 * text is only resolved inside a @Composable (via `stringResource`), keeping
 * ViewModels free of any Context/Resources dependency.
 */
data class UiMessage(@StringRes val res: Int, val args: List<Any> = emptyList())
