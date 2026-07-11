package com.sinop.sist.presentation.assets.detail

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sinop.sist.domain.model.AssetTransaction
import com.sinop.sist.domain.model.AssetTransactionType
import com.sinop.sist.domain.model.AssetType
import com.sinop.sist.domain.model.AssetWithPrice
import com.sinop.sist.ui.theme.ExpenseRed
import com.sinop.sist.ui.theme.IncomeGreen
import com.sinop.sist.util.formatCurrency
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailScreen(
    assetId: Long,
    onBackClick: () -> Unit,
    onEditAssetClick: (Long) -> Unit,
    onAddTransactionClick: (Long) -> Unit,
    onEditTransactionClick: (Long, Long) -> Unit,
    viewModel: AssetDetailViewModel = viewModel(factory = AssetDetailViewModel.factory(assetId))
) {
    val state by viewModel.state.collectAsState()
    var showPriceDialog by remember { mutableStateOf(false) }
    var showDeleteAssetDialog by remember { mutableStateOf(false) }
    var showDeleteTransactionDialog by remember { mutableStateOf<AssetTransaction?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.asset?.symbol ?: "Varlık Detayı") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    var menuExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menü")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Fiyat Güncelle") },
                            onClick = {
                                menuExpanded = false
                                showPriceDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Varlığı Düzenle") },
                            onClick = {
                                menuExpanded = false
                                onEditAssetClick(assetId)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Varlığı Sil") },
                            onClick = {
                                menuExpanded = false
                                showDeleteAssetDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
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
                onClick = { onAddTransactionClick(assetId) },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "İşlem ekle", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            state.assetWithPrice?.let { assetWithPrice ->
                AssetDetailHeader(asset = assetWithPrice)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = "İşlemler",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (state.transactions.isEmpty()) {
                EmptyTransactionsView()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.transactions, key = { it.id }) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onClick = { onEditTransactionClick(assetId, transaction.id) },
                            onDeleteClick = { showDeleteTransactionDialog = transaction }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showPriceDialog) {
        UpdatePriceDialog(
            currentPrice = state.assetWithPrice?.currentPrice,
            onDismiss = { showPriceDialog = false },
            onConfirm = { price ->
                viewModel.updatePrice(price)
                showPriceDialog = false
            }
        )
    }

    if (showDeleteAssetDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAssetDialog = false },
            title = { Text("Varlığı Sil") },
            text = { Text("Bu varlığı ve tüm işlemlerini silmek istediğine emin misin?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAsset(onBackClick)
                        showDeleteAssetDialog = false
                    }
                ) { Text("Sil", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAssetDialog = false }) { Text("İptal") }
            }
        )
    }

    showDeleteTransactionDialog?.let { transaction ->
        AlertDialog(
            onDismissRequest = { showDeleteTransactionDialog = null },
            title = { Text("İşlemi Sil") },
            text = { Text("Bu işlemi silmek istediğine emin misin?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(transaction)
                        showDeleteTransactionDialog = null
                    }
                ) { Text("Sil", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteTransactionDialog = null }) { Text("İptal") }
            }
        )
    }
}

@Composable
private fun AssetDetailHeader(asset: AssetWithPrice) {
    val profitLoss = asset.profitLoss
    val isPositive = profitLoss != null && profitLoss >= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = asset.asset.symbol,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    asset.asset.name?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(assetTypeColor(asset.asset.assetType).copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = assetTypeLabel(asset.asset.assetType),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = assetTypeColor(asset.asset.assetType)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                DetailItem("Adet", asset.totalQuantity.formatQuantity(), Modifier.weight(1f))
                DetailItem("Ort. Maliyet", asset.averageCost.formatCurrency(), Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                DetailItem(
                    "Güncel Fiyat",
                    asset.currentPrice?.formatCurrency() ?: "—",
                    Modifier.weight(1f)
                )
                DetailItem(
                    "Güncel Değer",
                    asset.currentValue?.formatCurrency() ?: "—",
                    Modifier.weight(1f)
                )
            }

            if (profitLoss != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kar/Zarar",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    val percent = asset.profitLossPercent
                    Text(
                        text = "${if (isPositive) "+" else ""}${profitLoss.formatCurrency()} ${if (percent != null) "(%${"%.2f".format(percent)})" else ""}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) IncomeGreen else ExpenseRed
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TransactionItem(
    transaction: AssetTransaction,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isBuy = transaction.transactionType == AssetTransactionType.BUY
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("tr", "TR"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isBuy) IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isBuy) "A" else "S",
                    color = if (isBuy) IncomeGreen else ExpenseRed,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBuy) "Alış" else "Satış",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = transaction.transactionDate.format(formatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (transaction.note != null) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${transaction.quantity.formatQuantity()} adet",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "@ ${transaction.pricePerUnit.formatCurrency()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (transaction.fee > 0) {
                    Text(
                        text = "Ücret: ${transaction.fee.formatCurrency()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EmptyTransactionsView() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Henüz işlem yok",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Alış veya satış eklemek için + butonuna dokunun",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UpdatePriceDialog(
    currentPrice: Double?,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var price by remember { mutableStateOf(currentPrice?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fiyat Güncelle") },
        text = {
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Güncel fiyat") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    price.toDoubleOrNull()?.let { onConfirm(it) }
                }
            ) { Text("Güncelle") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

private fun assetTypeLabel(type: AssetType): String = when (type) {
    AssetType.STOCK -> "Hisse"
    AssetType.FUND -> "Fon"
    AssetType.CURRENCY -> "Döviz"
    AssetType.GOLD -> "Altın"
}

@Composable
private fun assetTypeColor(type: AssetType) = when (type) {
    AssetType.STOCK -> MaterialTheme.colorScheme.primary
    AssetType.FUND -> IncomeGreen
    AssetType.CURRENCY -> androidx.compose.ui.graphics.Color(0xFF2196F3)
    AssetType.GOLD -> androidx.compose.ui.graphics.Color(0xFFFFA000)
}

private fun Double.formatQuantity(): String = when {
    this % 1.0 == 0.0 -> this.toInt().toString()
    else -> "%.4f".format(this).trimEnd('0').trimEnd('.')
}