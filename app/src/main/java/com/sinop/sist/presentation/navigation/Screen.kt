package com.sinop.sist.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Transactions : Screen("transactions")
    data object AddTransaction : Screen("add_transaction?transactionId={transactionId}") {
        fun createRoute(transactionId: Long? = null) =
            "add_transaction${transactionId?.let { "?transactionId=$it" } ?: ""}"
    }
    data object Categories : Screen("categories")
    data object Budgets : Screen("budgets")
    data object Recurring : Screen("recurring")
    data object Debts : Screen("debts")
    data object Installments : Screen("installments")
    data object Assets : Screen("assets")
    data object AddAsset : Screen("add_asset?assetId={assetId}") {
        fun createRoute(assetId: Long? = null) =
            "add_asset${assetId?.let { "?assetId=$it" } ?: ""}"
    }
    data object AssetDetail : Screen("asset_detail/{assetId}") {
        fun createRoute(assetId: Long) = "asset_detail/$assetId"
    }
    data object AddAssetTransaction : Screen("add_asset_transaction/{assetId}?transactionId={transactionId}") {
        fun createRoute(assetId: Long, transactionId: Long? = null) =
            "add_asset_transaction/$assetId${transactionId?.let { "?transactionId=$it" } ?: ""}"
    }
    data object Reports : Screen("reports")
    data object Settings : Screen("settings")
}
