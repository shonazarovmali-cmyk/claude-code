package com.hanfood.warehouse.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hanfood.warehouse.HanFoodApp
import com.hanfood.warehouse.R
import com.hanfood.warehouse.data.local.entity.TransactionType
import com.hanfood.warehouse.security.BiometricHelper
import com.hanfood.warehouse.ui.components.BrandHeader
import com.hanfood.warehouse.ui.components.TopTab
import com.hanfood.warehouse.ui.components.TopTabMenu
import com.hanfood.warehouse.ui.screens.auth.PinSetupScreen
import com.hanfood.warehouse.ui.screens.auth.PinUnlockScreen
import com.hanfood.warehouse.ui.screens.clients.ClientEditScreen
import com.hanfood.warehouse.ui.screens.clients.ClientListScreen
import com.hanfood.warehouse.ui.screens.clients.LocationPickerScreen
import com.hanfood.warehouse.ui.screens.dashboard.DashboardScreen
import com.hanfood.warehouse.ui.screens.invoices.InvoiceDetailScreen
import com.hanfood.warehouse.ui.screens.invoices.InvoiceListScreen
import com.hanfood.warehouse.ui.screens.products.ProductEditScreen
import com.hanfood.warehouse.ui.screens.products.ProductListScreen
import com.hanfood.warehouse.ui.screens.reports.ReportsScreen
import com.hanfood.warehouse.ui.screens.scanner.BarcodeScannerScreen
import com.hanfood.warehouse.ui.screens.settings.SettingsScreen
import com.hanfood.warehouse.ui.screens.splash.SplashScreen
import com.hanfood.warehouse.ui.screens.stock.MovementScreen

@Composable
private fun rememberTopTabs(): List<TopTab> = listOf(
    TopTab(Routes.DASHBOARD, stringResource(R.string.tab_dashboard), Icons.Filled.Home),
    TopTab(Routes.PRODUCTS, stringResource(R.string.tab_products), Icons.Filled.Inventory),
    TopTab(Routes.CLIENTS, stringResource(R.string.tab_clients), Icons.Filled.Groups),
    TopTab(Routes.INVOICES, stringResource(R.string.tab_invoices), Icons.Filled.ReceiptLong),
    TopTab(Routes.REPORTS, stringResource(R.string.tab_reports), Icons.Filled.Assessment)
)

@Composable
fun HanFoodNavGraph(app: HanFoodApp, activity: FragmentActivity) {
    val navController = rememberNavController()
    val repository = app.repository
    val pinManager = app.pinManager
    val topTabs = rememberTopTabs()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showTopTabs = topTabs.any { it.route == currentRoute }

    fun goToDashboardClearingBackstack() {
        navController.navigate(Routes.DASHBOARD) {
            popUpTo(0) { inclusive = true }
        }
    }

    fun goToAuthClearingBackstack() {
        val destination = if (pinManager.isPinSet()) Routes.PIN_UNLOCK else Routes.PIN_SETUP
        navController.navigate(destination) {
            popUpTo(0) { inclusive = true }
        }
    }

    val appName = stringResource(R.string.app_name)
    val biometricSubtitle = stringResource(R.string.biometric_prompt_subtitle)
    val cancelLabel = stringResource(R.string.action_cancel)

    Scaffold(
        topBar = {
            if (showTopTabs) {
                Column {
                    BrandHeader(
                        trailingIcon = Icons.Filled.Settings,
                        trailingContentDescription = stringResource(R.string.cd_settings),
                        onTrailingClick = { navController.navigate(Routes.SETTINGS) }
                    )
                    TopTabMenu(
                        tabs = topTabs,
                        currentRoute = currentRoute,
                        onSelect = { route ->
                            navController.navigate(route) {
                                popUpTo(Routes.DASHBOARD) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { outerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(outerPadding)
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(onFinished = { goToAuthClearingBackstack() })
            }

            composable(Routes.PIN_SETUP) {
                PinSetupScreen(pinManager = pinManager, onDone = { goToDashboardClearingBackstack() })
            }

            composable(Routes.PIN_UNLOCK) {
                PinUnlockScreen(
                    pinManager = pinManager,
                    biometricAvailable = BiometricHelper.isAvailable(activity),
                    onUnlocked = { goToDashboardClearingBackstack() },
                    onRequestBiometric = {
                        BiometricHelper.prompt(
                            activity = activity,
                            title = appName,
                            subtitle = biometricSubtitle,
                            negativeButtonText = cancelLabel,
                            onSuccess = { goToDashboardClearingBackstack() },
                            onError = { /* User falls back to entering the PIN */ }
                        )
                    }
                )
            }

            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    repository = repository,
                    onQuickAction = { type -> navController.navigate(Routes.movement(type)) },
                    onScanner = { navController.navigate(Routes.SCANNER) },
                    onOpenInvoice = { id -> navController.navigate(Routes.invoiceDetail(id)) }
                )
            }

            composable(Routes.PRODUCTS) {
                ProductListScreen(
                    repository = repository,
                    onAdd = { navController.navigate(Routes.productEdit()) },
                    onOpen = { id -> navController.navigate(Routes.productEdit(id)) },
                    onScan = { navController.navigate(Routes.SCANNER) }
                )
            }

            composable(
                Routes.PRODUCT_EDIT,
                arguments = listOf(navArgument(Routes.PRODUCT_EDIT_ARG) { type = NavType.LongType; defaultValue = -1L })
            ) { entry ->
                val id = entry.arguments?.getLong(Routes.PRODUCT_EDIT_ARG) ?: -1L
                ProductEditScreen(
                    repository = repository,
                    productId = if (id <= 0) 0L else id,
                    onBack = { navController.popBackStack() },
                    onScan = { navController.navigate(Routes.SCANNER) }
                )
            }

            composable(Routes.CLIENTS) {
                ClientListScreen(
                    repository = repository,
                    onAdd = { navController.navigate(Routes.clientEdit()) },
                    onOpen = { id -> navController.navigate(Routes.clientEdit(id)) }
                )
            }

            composable(
                Routes.CLIENT_EDIT,
                arguments = listOf(navArgument(Routes.CLIENT_EDIT_ARG) { type = NavType.LongType; defaultValue = -1L })
            ) { entry ->
                val id = entry.arguments?.getLong(Routes.CLIENT_EDIT_ARG) ?: -1L
                ClientEditScreen(
                    repository = repository,
                    clientId = if (id <= 0) 0L else id,
                    onBack = { navController.popBackStack() },
                    onPickOnMap = { navController.navigate(Routes.LOCATION_PICKER) }
                )
            }

            composable(Routes.LOCATION_PICKER) {
                LocationPickerScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.INVOICES) {
                InvoiceListScreen(repository = repository, onOpen = { id -> navController.navigate(Routes.invoiceDetail(id)) })
            }

            composable(
                Routes.INVOICE_DETAIL,
                arguments = listOf(navArgument(Routes.INVOICE_DETAIL_ARG) { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong(Routes.INVOICE_DETAIL_ARG) ?: 0L
                InvoiceDetailScreen(repository = repository, transactionId = id, onBack = { navController.popBackStack() })
            }

            composable(Routes.REPORTS) {
                ReportsScreen(repository = repository)
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    pinManager = pinManager,
                    onBack = { navController.popBackStack() },
                    onChangePin = { navController.navigate(Routes.PIN_SETUP) }
                )
            }

            composable(
                Routes.MOVEMENT,
                arguments = listOf(navArgument(Routes.MOVEMENT_ARG) { type = NavType.StringType })
            ) { entry ->
                val typeArg = entry.arguments?.getString(Routes.MOVEMENT_ARG) ?: TransactionType.STOCK_IN.name
                MovementScreen(
                    repository = repository,
                    movementType = TransactionType.valueOf(typeArg),
                    onBack = { navController.popBackStack() },
                    onScan = { navController.navigate(Routes.SCANNER) },
                    onSaved = { id ->
                        navController.navigate(Routes.invoiceDetail(id)) {
                            popUpTo(Routes.DASHBOARD)
                        }
                    }
                )
            }

            composable(Routes.SCANNER) {
                BarcodeScannerScreen(
                    onResult = { code ->
                        ScannerBus.emit(code)
                        navController.popBackStack()
                    },
                    onClose = { navController.popBackStack() }
                )
            }
        }
    }
}
