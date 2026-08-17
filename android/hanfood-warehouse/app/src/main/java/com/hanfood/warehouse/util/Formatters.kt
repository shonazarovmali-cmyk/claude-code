package com.hanfood.warehouse.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val plnFormat: NumberFormat = NumberFormat.getNumberInstance(Locale("pl", "PL")).apply {
    maximumFractionDigits = 0
}

private val qtyFormat: NumberFormat = NumberFormat.getNumberInstance(Locale("pl", "PL")).apply {
    maximumFractionDigits = 2
}

private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

/** Formats a money amount as "1 250 000 zł" (Polish złoty), regardless of UI language. */
fun formatMoney(amount: Double): String = "${plnFormat.format(amount)} zł"

/** Miqdorni ortiqcha nol-kasrlarsiz formatlaydi (masalan 12 yoki 12.5). */
fun formatQuantity(quantity: Double): String = qtyFormat.format(quantity)

fun formatDateTime(millis: Long): String = dateTimeFormat.format(Date(millis))

fun formatDate(millis: Long): String = dateFormat.format(Date(millis))
