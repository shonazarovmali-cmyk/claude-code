package com.hanfood.warehouse.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hanfood.warehouse.R
import com.hanfood.warehouse.data.local.entity.TransactionType
import com.hanfood.warehouse.ui.screens.reports.ReportPeriod

/** Short label for a transaction type (used in lists/badges), localized. */
@Composable
fun transactionTypeLabel(type: TransactionType): String = when (type) {
    TransactionType.STOCK_IN -> stringResource(R.string.transaction_type_stock_in)
    TransactionType.STOCK_OUT -> stringResource(R.string.transaction_type_stock_out)
    TransactionType.RETURN -> stringResource(R.string.transaction_type_return)
}

/** Longer, action-oriented title (used as a screen title), localized. */
@Composable
fun movementScreenTitle(type: TransactionType): String = when (type) {
    TransactionType.STOCK_IN -> stringResource(R.string.movement_title_stock_in)
    TransactionType.STOCK_OUT -> stringResource(R.string.movement_title_stock_out)
    TransactionType.RETURN -> stringResource(R.string.movement_title_return)
}

/** Localized label for a report time period. */
@Composable
fun reportPeriodLabel(period: ReportPeriod): String = when (period) {
    ReportPeriod.TODAY -> stringResource(R.string.period_today)
    ReportPeriod.WEEK -> stringResource(R.string.period_week)
    ReportPeriod.MONTH -> stringResource(R.string.period_month)
    ReportPeriod.ALL_TIME -> stringResource(R.string.period_all_time)
}
