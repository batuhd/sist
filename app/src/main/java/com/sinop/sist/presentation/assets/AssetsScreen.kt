package com.sinop.sist.presentation.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sinop.sist.domain.model.AssetType
import com.sinop.sist.domain.model.AssetWithPrice
import com.sinop.sist.domain.model.PortfolioSummary
import com.sinop.sist.ui.theme.ExpenseRed
import com.sinop.sist.ui.theme.IncomeGreen
import com.sinop.sist.util.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(
    onBackClick: () -> Unit,
    onAddAssetClick: () -> Unit,
    onAssetClick: (Long) -> Unit,
    viewModel: AssetsViewModel = viewModel(factory = AssetsViewModel.factory())
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.refreshMessage) {
        state.refreshMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumeRefreshMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Portföyüm") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshPrices() },
                        enabled = !state.isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Fiyatları yenile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAssetClick,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Varlık ekle", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            state.summary?.let { summary ->
                PortfolioSummaryCard(summary = summary)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state.assets.isEmpty()) {
                EmptyPortfolioView()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.assets, key = { it.asset.id }) { asset ->
                        AssetListItem(
                            asset = asset,
                            onClick = { onAssetClick(asset.asset.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

}

@Composable
private fun PortfolioSummaryCard(summary: PortfolioSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Portföy Özeti",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryItem(
                    title = "Maliyet",
                    amount = summary.totalCost,
                    modifier = Modifier.weight(1f)
                )
                SummaryItem(
                    title = "Değer",
                    amount = summary.totalValue ?: 0.0,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            val profitLoss = summary.totalProfitLoss
            if (profitLoss != null) {
                val isPositive = profitLoss >= 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isPositive) IncomeGreen else ExpenseRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Kar/Zarar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    val percent = summary.totalProfitLossPercent
                    Text(
                        text = "${if (isPositive) "+" else ""}${profitLoss.formatCurrency()} ${if (percent != null) "(%${"%.2f".format(percent)})" else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) IncomeGreen else ExpenseRed
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    title: String,
    amount: Double,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = amount.formatCurrency(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun AssetListItem(
    asset: AssetWithPrice,
    onClick: () -> Unit
) {
    val profitLoss = asset.profitLoss
    val isPositive = profitLoss != null && profitLoss >= 0

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(assetTypeColor(asset.asset.assetType).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = assetTypeIcon(asset.asset.assetType),
                    contentDescription = null,
                    tint = assetTypeColor(asset.asset.assetType),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = asset.asset.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                asset.asset.name?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${asset.totalQuantity.formatQuantity()} adet · Ort. ${asset.averageCost.formatCurrency()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val sourceText = when {
                    asset.priceSource?.startsWith("yahoo") == true -> "Yahoo Finance"
                    asset.priceSource?.startsWith("fvt") == true -> "FVT"
                    asset.currentPrice != null -> "Manuel"
                    else -> "Fiyat yok"
                }
                val sourceColor = when (sourceText) {
                    "Yahoo Finance", "FVT" -> IncomeGreen
                    "Fiyat yok" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                }
                Text(
                    text = sourceText,
                    style = MaterialTheme.typography.labelSmall,
                    color = sourceColor
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = asset.currentValue?.formatCurrency() ?: "—",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (profitLoss != null) {
                    Text(
                        text = "${if (isPositive) "+" else ""}${profitLoss.formatCurrency()}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isPositive) IncomeGreen else ExpenseRed
                    )
                }
                asset.currentPrice?.let {
                    Text(
                        text = it.formatCurrency(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPortfolioView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShowChart,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Henüz varlık eklenmemiş",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hisse, fon, döviz veya altın eklemek için + butonuna dokunun",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun assetTypeIcon(type: AssetType) = when (type) {
    AssetType.STOCK -> Icons.Default.BarChart
    AssetType.FUND -> Icons.Default.AttachMoney
    AssetType.CURRENCY -> Icons.Default.AttachMoney
    AssetType.GOLD -> Icons.Default.ShowChart
}

@Composable
private fun assetTypeColor(type: AssetType): Color = when (type) {
    AssetType.STOCK -> MaterialTheme.colorScheme.primary
    AssetType.FUND -> IncomeGreen
    AssetType.CURRENCY -> Color(0xFF2196F3)
    AssetType.GOLD -> Color(0xFFFFA000)
}

private fun Double.formatQuantity(): String = when {
    this % 1.0 == 0.0 -> this.toInt().toString()
    else -> "%.4f".format(this).trimEnd('0').trimEnd('.')
}