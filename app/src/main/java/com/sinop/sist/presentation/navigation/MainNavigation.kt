package com.sinop.sist.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sinop.sist.presentation.assets.AssetsScreen
import com.sinop.sist.presentation.assets.add.AddAssetScreen
import com.sinop.sist.presentation.assets.detail.AssetDetailScreen
import com.sinop.sist.presentation.assets.transaction.AddAssetTransactionScreen
import com.sinop.sist.presentation.budget.BudgetsScreen
import com.sinop.sist.presentation.categories.CategoriesScreen
import com.sinop.sist.presentation.debts.DebtAndInstallmentScreen
import com.sinop.sist.presentation.home.HomeScreen
import com.sinop.sist.presentation.recurring.RecurringTransactionsScreen
import com.sinop.sist.presentation.settings.SettingsScreen
import com.sinop.sist.presentation.transactions.TransactionsScreen
import com.sinop.sist.presentation.transactions.add.AddTransactionScreen

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    data object Home : BottomNavItem(Screen.Home.route, Icons.Default.Home, "Ana Sayfa")
    data object Transactions : BottomNavItem(Screen.Transactions.route, Icons.Default.Wallet, "İşlemler")
    data object Assets : BottomNavItem(Screen.Assets.route, Icons.Default.ShowChart, "Portföy")
    data object More : BottomNavItem("more", Icons.Default.MoreHoriz, "Diğer")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current
    var showMoreSheet by remember { mutableStateOf(false) }
    val moreSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val bottomNavRoutes = listOf(
        BottomNavItem.Home,
        BottomNavItem.Transactions,
        BottomNavItem.Assets,
        BottomNavItem.More
    )

    val showBottomBar = currentDestination?.route in bottomNavRoutes.map { it.route }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(visible = showBottomBar) {
                SistBottomNavigation(
                    items = bottomNavRoutes,
                    currentDestination = currentDestination,
                    onNavigate = { route ->
                        if (route == BottomNavItem.More.route) {
                            showMoreSheet = true
                            return@SistBottomNavigation
                        }
                        if (currentDestination?.route == route) return@SistBottomNavigation
                        if (route == Screen.Home.route) {
                            navController.popBackStack(Screen.Home.route, inclusive = false, saveState = false)
                        } else {
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onAddTransactionClick = { navController.navigate(Screen.AddTransaction.createRoute()) },
                    onViewBudgetsClick = { navController.navigate(Screen.Budgets.route) }
                )
            }
            composable(Screen.Transactions.route) {
                TransactionsScreen(
                    onAddClick = { navController.navigate(Screen.AddTransaction.createRoute()) },
                    onEditClick = { id ->
                        navController.navigate(Screen.AddTransaction.createRoute(id))
                    }
                )
            }
            composable(Screen.AddTransaction.route) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString("transactionId")?.toLongOrNull()
                AddTransactionScreen(
                    onBackClick = { navController.popBackStack() },
                    transactionId = transactionId
                )
            }
            composable(Screen.Categories.route) {
                CategoriesScreen()
            }
            composable(Screen.Budgets.route) {
                BudgetsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Recurring.route) {
                RecurringTransactionsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Debts.route) {
                DebtAndInstallmentScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onCategoriesClick = { navController.navigate(Screen.Categories.route) },
                    onRecurringClick = { navController.navigate(Screen.Recurring.route) },
                    onNotificationsClick = {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                )
            }
            composable(Screen.Assets.route) {
                AssetsScreen(
                    onBackClick = { navController.popBackStack() },
                    onAddAssetClick = { navController.navigate(Screen.AddAsset.createRoute()) },
                    onAssetClick = { id -> navController.navigate(Screen.AssetDetail.createRoute(id)) }
                )
            }
            composable(Screen.AddAsset.route) {
                AddAssetScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.AssetDetail.route) { backStackEntry ->
                val assetId = backStackEntry.arguments?.getString("assetId")?.toLongOrNull() ?: 0
                AssetDetailScreen(
                    assetId = assetId,
                    onBackClick = { navController.popBackStack() },
                    onEditAssetClick = { id -> navController.navigate(Screen.AddAsset.createRoute(id)) },
                    onAddTransactionClick = { id -> navController.navigate(Screen.AddAssetTransaction.createRoute(id)) },
                    onEditTransactionClick = { assetId, transactionId ->
                        navController.navigate(Screen.AddAssetTransaction.createRoute(assetId, transactionId))
                    }
                )
            }
            composable(Screen.AddAssetTransaction.route) { backStackEntry ->
                val assetId = backStackEntry.arguments?.getString("assetId")?.toLongOrNull() ?: 0
                val transactionId = backStackEntry.arguments?.getString("transactionId")?.toLongOrNull()
                AddAssetTransactionScreen(
                    assetId = assetId,
                    transactionId = transactionId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }

    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            sheetState = moreSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Diğer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                MoreSheetItem(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    title = "Borç / Taksit",
                    onClick = {
                        showMoreSheet = false
                        navController.navigate(Screen.Debts.route)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                MoreSheetItem(
                    icon = Icons.Default.Settings,
                    title = "Ayarlar",
                    onClick = {
                        showMoreSheet = false
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }
        }
    }
}

@Composable
private fun SistBottomNavigation(
    items: List<BottomNavItem>,
    currentDestination: androidx.navigation.NavDestination?,
    onNavigate: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = if (item.route == "more") {
                    false
                } else {
                    currentDestination?.hierarchy?.any { it.route == item.route } == true
                }
                BottomNavItem(
                    item = item,
                    selected = selected,
                    onClick = { onNavigate(item.route) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val iconColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        androidx.compose.ui.graphics.Color.Transparent
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = iconColor
        )
    }
}

@Composable
private fun MoreSheetItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
