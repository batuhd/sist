package com.sinop.sist.presentation.transactions

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sinop.sist.presentation.components.EmptyState
import com.sinop.sist.presentation.components.SistTopBar
import com.sinop.sist.presentation.components.SummaryCards
import com.sinop.sist.presentation.components.TransactionItem
import com.sinop.sist.ui.theme.ExpenseRed
import com.sinop.sist.ui.theme.IncomeGreen
import com.sinop.sist.util.formatCurrency
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onAddClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: TransactionsViewModel = viewModel(factory = TransactionsViewModel.factory())
) {
    val state by viewModel.state.collectAsState()
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SistTopBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = { viewModel.selectMonth(state.selectedMonth.minusMonths(1)) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Önceki ay"
                            )
                        }
                        Text(
                            text = state.selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("tr", "TR")))
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.width(180.dp),
                            textAlign = TextAlign.Center
                        )
                        IconButton(onClick = { viewModel.selectMonth(state.selectedMonth.plusMonths(1)) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Sonraki ay"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ekle")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SummaryCards(
                income = state.totalIncome,
                expense = state.totalExpense,
                balance = state.balance
            )

            ControlBar(
                viewMode = state.viewMode,
                sortOrder = state.sortOrder,
                onViewModeChange = { viewModel.setViewMode(it) },
                onSortChange = { viewModel.setSortOrder(it) },
                sortMenuExpanded = sortMenuExpanded,
                onSortMenuExpandedChange = { sortMenuExpanded = it }
            )

            when (state.viewMode) {
                TransactionViewMode.LIST -> {
                    if (state.transactions.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.Category,
                            title = "Henüz işlem yok",
                            subtitle = "İlk gelir veya giderini eklemek için + butonuna dokun"
                        )
                    } else {
                        Column {
                            state.transactions.forEach { transaction ->
                                TransactionItem(
                                    transaction = transaction,
                                    category = state.categories.find { it.id == transaction.categoryId },
                                    onEdit = { onEditClick(transaction.id) },
                                    onDelete = { viewModel.deleteTransaction(transaction) }
                                )
                            }
                        }
                    }
                }
                TransactionViewMode.CHART -> {
                    ChartContent(
                        income = state.totalIncome,
                        expense = state.totalExpense,
                        expensesByCategory = state.expensesByCategory
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ControlBar(
    viewMode: TransactionViewMode,
    sortOrder: TransactionSortOrder,
    onViewModeChange: (TransactionViewMode) -> Unit,
    onSortChange: (TransactionSortOrder) -> Unit,
    sortMenuExpanded: Boolean,
    onSortMenuExpandedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = viewMode == TransactionViewMode.LIST,
                onClick = { onViewModeChange(TransactionViewMode.LIST) },
                label = { Text("Liste") },
                leadingIcon = { Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            FilterChip(
                selected = viewMode == TransactionViewMode.CHART,
                onClick = { onViewModeChange(TransactionViewMode.CHART) },
                label = { Text("Grafik") },
                leadingIcon = { Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        if (viewMode == TransactionViewMode.LIST) {
            Box {
                FilterChip(
                    selected = false,
                    onClick = { onSortMenuExpandedChange(true) },
                    label = { Text(sortOrder.label()) },
                    leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { onSortMenuExpandedChange(false) }
                ) {
                    TransactionSortOrder.entries.forEach { order ->
                        DropdownMenuItem(
                            text = { Text(order.label()) },
                            onClick = {
                                onSortChange(order)
                                onSortMenuExpandedChange(false)
                            },
                            leadingIcon = { Icon(order.icon(), contentDescription = null) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionSortOrder.label(): String = when (this) {
    TransactionSortOrder.DATE_DESC -> "Tarih (Yeni)"
    TransactionSortOrder.DATE_ASC -> "Tarih (Eski)"
    TransactionSortOrder.AMOUNT_DESC -> "Tutar (Yüksek)"
    TransactionSortOrder.AMOUNT_ASC -> "Tutar (Düşük)"
}

@Composable
private fun TransactionSortOrder.icon() = when (this) {
    TransactionSortOrder.DATE_DESC,
    TransactionSortOrder.DATE_ASC -> Icons.Default.CalendarMonth
    TransactionSortOrder.AMOUNT_DESC,
    TransactionSortOrder.AMOUNT_ASC -> Icons.Default.BarChart
}

@Composable
private fun ChartContent(
    income: Double,
    expense: Double,
    expensesByCategory: List<CategoryExpense>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Gelir & Gider",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        IncomeExpenseBarChart(income = income, expense = expense)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Gider Dağılımı",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (expensesByCategory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Gider bulunmuyor",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            ExpensePieChart(expensesByCategory = expensesByCategory)
            Spacer(modifier = Modifier.height(16.dp))
            ExpenseLegend(expensesByCategory = expensesByCategory)
        }
    }
}

@Composable
private fun IncomeExpenseBarChart(
    income: Double,
    expense: Double,
    modifier: Modifier = Modifier
) {
    val maxValue = maxOf(income, expense, 1.0)
    val incomeRatio = (income / maxValue).toFloat().coerceIn(0f, 1f)
    val expenseRatio = (expense / maxValue).toFloat().coerceIn(0f, 1f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        BarColumn(label = "Gelir", amount = income, ratio = incomeRatio, color = IncomeGreen)
        BarColumn(label = "Gider", amount = expense, ratio = expenseRatio, color = ExpenseRed)
    }
}

@Composable
private fun BarColumn(label: String, amount: Double, ratio: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = amount.formatCurrency(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(48.dp)
                .height((ratio * 100).dp.coerceAtLeast(4.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.85f))
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExpensePieChart(
    expensesByCategory: List<CategoryExpense>,
    modifier: Modifier = Modifier
) {
    val total = expensesByCategory.sumOf { it.amount }.toFloat().coerceAtLeast(1f)
    val colors = expensesByCategory.map { Color(android.graphics.Color.parseColor(it.category.colorHex)) }
    val proportions = expensesByCategory.map { (it.amount.toFloat() / total).coerceIn(0f, 1f) }
    val fallbackColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            var startAngle = -90f
            val size = Size(size.width, size.height)
            val strokeWidth = size.width * 0.3f
            val innerRadius = (size.width - strokeWidth) / 2f

            proportions.forEachIndexed { index, proportion ->
                val sweepAngle = proportion * 360f
                val color = colors.getOrElse(index) { fallbackColor }

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset.Zero,
                    size = size,
                    style = Stroke(width = strokeWidth)
                )

                if (proportion > 0.08f) {
                    val labelAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                    val labelRadius = innerRadius + strokeWidth / 2f
                    val x = (center.x + labelRadius * cos(labelAngle)).toFloat()
                    val y = (center.y + labelRadius * sin(labelAngle)).toFloat()
                    val percent = (proportion * 100).toInt()
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            "$percent%",
                            x,
                            y,
                            android.graphics.Paint().apply {
                                this.color = Color.White.toArgb()
                                this.textAlign = android.graphics.Paint.Align.CENTER
                                this.textSize = 32f
                                this.isFakeBoldText = true
                            }
                        )
                    }
                }

                startAngle += sweepAngle
            }
        }
    }
}

@Composable
private fun ExpenseLegend(expensesByCategory: List<CategoryExpense>) {
    val total = expensesByCategory.sumOf { it.amount }.coerceAtLeast(0.01)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        expensesByCategory.forEach { item ->
            val backgroundColor = Color(android.graphics.Color.parseColor(item.category.colorHex))
            val percent = ((item.amount / total) * 100).toInt()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(backgroundColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = item.category.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = "${item.amount.formatCurrency()} ($percent%)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
