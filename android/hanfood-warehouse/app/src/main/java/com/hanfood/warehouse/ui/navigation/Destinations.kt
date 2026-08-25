package com.hanfood.warehouse.ui.navigation

/** Ilovadagi barcha ekranlarning marshrutlari (navigation routes) bitta joyda. */
object Routes {
    const val SPLASH = "splash"
    const val PIN_UNLOCK = "pin_unlock"
    const val PIN_SETUP = "pin_setup"

    const val DASHBOARD = "dashboard"
    const val PRODUCTS = "products"
    const val CLIENTS = "clients"
    const val INVOICES = "invoices"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"

    const val PRODUCT_EDIT_ARG = "productId"
    const val PRODUCT_EDIT = "product_edit?$PRODUCT_EDIT_ARG={$PRODUCT_EDIT_ARG}"
    fun productEdit(productId: Long? = null) = "product_edit?$PRODUCT_EDIT_ARG=${productId ?: -1}"

    const val CLIENT_EDIT_ARG = "clientId"
    const val CLIENT_EDIT = "client_edit?$CLIENT_EDIT_ARG={$CLIENT_EDIT_ARG}"
    fun clientEdit(clientId: Long? = null) = "client_edit?$CLIENT_EDIT_ARG=${clientId ?: -1}"

    const val INVOICE_DETAIL_ARG = "transactionId"
    const val INVOICE_DETAIL = "invoice_detail/{$INVOICE_DETAIL_ARG}"
    fun invoiceDetail(transactionId: Long) = "invoice_detail/$transactionId"

    const val MOVEMENT_ARG = "movementType"
    const val MOVEMENT = "movement/{$MOVEMENT_ARG}"
    fun movement(type: String) = "movement/$type"

    const val SCANNER = "scanner"
    const val LOCATION_PICKER = "location_picker"

    /** Bottom navigatsiya panelidagi asosiy 5 ta bo'lim. */
    val bottomTabs = listOf(DASHBOARD, PRODUCTS, CLIENTS, INVOICES, REPORTS)
}
