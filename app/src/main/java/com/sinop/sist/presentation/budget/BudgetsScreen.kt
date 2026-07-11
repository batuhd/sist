package com.sinop.sist.presentation.budget

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sinop.sist.domain.model.Budget
import com.sinop.sist.domain.model.BudgetWithSpending
import com.sinop.sist.domain.model.Category
import com.sinop.sist.presentation.components.SistTopBar
import com.sinop.sist.ui.theme.ExpenseRed
import com.sinop.sist.ui.theme.IncomeGreen
import com.sinop.sist.ui.theme.IncomeGreenLight
import com.sinop.sist.ui.theme.Primary100
import com.sinop.sist.util.formatCurrency
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    onBackClick: () -> Unit,
    viewModel: BudgetsViewModel = viewModel(factory = BudgetsViewModel.factory())
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            SistTopBar(
                title = "Bütçeler",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Bütçe ekle",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item { MonthSelector(month = state.month, onMonthChange = viewModel::setMonth) }

            val general = state.budgets.find { it.budget.categoryId == null }
            val categories = state.budgets.filter { it.budget.categoryId != null }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                if (general != null) {
                    GeneralBudgetCard(
                        budget = general,
                        onClick = { viewModel.showAddDialog(general) }
                    )
                } else {
                    EmptyBudgetCard(
                        title = "Genel Aylık Bütçe",
                        subtitle = "Henüz tanımlanmamış",
                        onClick = { viewModel.showAddDialog() }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Kategori Bütçeleri",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (categories.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Kategori bütçesi yok. Eklemek için + butonuna basın.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(categories) { budget ->
                    CategoryBudgetCard(
                        budget = budget,
                        category = state.categories.find { it.id == budget.budget.categoryId },
                        onClick = { viewModel.showAddDialog(budget) },
                        onDelete = { viewModel.deleteBudget(budget.budget) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    state.dialogError?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::consumeDialogError,
            title = { Text("Hata") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = viewModel::consumeDialogError) { Text("Tamam") }
            }
        )
    }

    if (state.showDialog) {
        AddBudgetDialog(
            editingBudget = state.editingBudget,
            categories = state.categories,
            onDismiss = viewModel::dismissDialog,
            onConfirm = { categoryId, limit -> viewModel.saveBudget(categoryId, limit) }
        )
    }
}

@Composable
private fun MonthSelector(
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("tr", "TR"))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onMonthChange(month.minusMonths(1)) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Önceki ay"
            )
        }
        Text(
            text = month.format(formatter).replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = { onMonthChange(month.plusMonths(1)) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Sonraki ay"
            )
        }
    }
}

@Composable
private fun GeneralBudgetCard(
    budget: BudgetWithSpending,
    onClick: () -> Unit
) {
    BudgetCard(
        title = "Genel Aylık Bütçe",
        icon = Icons.Default.Savings,
        iconTint = IncomeGreen,
        iconBackground = IncomeGreenLight,
        budget = budget,
        onClick = onClick,
        showDelete = false
    )
}

@Composable
private fun EmptyBudgetCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(IncomeGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = IncomeGreen,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CategoryBudgetCard(
    budget: BudgetWithSpending,
    category: Category?,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val color = category?.colorHex?.let { Color(android.graphics.Color.parseColor(it)) }
        ?: MaterialTheme.colorScheme.primary
    val iconBackground = color.copy(alpha = 0.12f)
    val iconTint = if (color.luminance() > 0.5f) Color.Black else Color.White
    BudgetCard(
        title = category?.name ?: "Kategori",
        icon = com.sinop.sist.util.getCategoryIcon(category?.iconName ?: "category"),
        iconTint = iconTint,
        iconBackground = iconBackground,
        budget = budget,
        onClick = onClick,
        onDelete = onDelete,
        showDelete = true
    )
}

@Composable
private fun BudgetCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBackground: Color,
    budget: BudgetWithSpending,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    showDelete: Boolean
) {
    val progressColor = when {
        budget.percentage < 0.5f -> Primary100
        budget.percentage < 0.85f -> MaterialTheme.colorScheme.tertiary
        else -> ExpenseRed
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(iconBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Limit: ${budget.budget.monthlyLimit.formatCurrency()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (showDelete) {
                        IconButton(onClick = { onDelete?.invoke() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Sil",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { budget.percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Harcanan: ${budget.spent.formatCurrency()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Kalan: ${budget.remaining.formatCurrency()}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (budget.remaining < 0) ExpenseRed else Primary100
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBudgetDialog(
    editingBudget: Budget?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (Long?, Double) -> Unit
) {
    var selectedCategoryId by remember(editingBudget) {
        mutableLongStateOf(editingBudget?.categoryId ?: -1L)
    }
    var limitText by remember(editingBudget) {
        mutableStateOf(editingBudget?.monthlyLimit?.toString() ?: "")
    }
    var expanded by remember { mutableStateOf(false) }

    val isGeneral = selectedCategoryId == -1L
    val selectedCategory = categories.find { it.id == selectedCategoryId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editingBudget != null) "Bütçeyi Düzenle" else "Bütçe Ekle") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { if (editingBudget == null) expanded = it }
                ) {
                    OutlinedTextField(
                        value = if (isGeneral) "Genel (tüm giderler)" else (selectedCategory?.name ?: "Kategori seç"),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        trailingIcon = {
                            if (editingBudget == null) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Genel (tüm giderler)") },
                            onClick = {
                                selectedCategoryId = -1L
                                expanded = false
                            }
                        )
                        HorizontalDivider()
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Aylık limit") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitText.toDoubleOrNull() ?: 0.0
                    if (limit > 0) {
                        onConfirm(if (isGeneral) null else selectedCategoryId, limit)
                    }
                },
                enabled = limitText.toDoubleOrNull()?.let { it > 0 } == true
            ) { Text("Kaydet") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

private fun Color.luminance(): Float {
    val red = red * 0.2126f
    val green = green * 0.7152f
    val blue = blue * 0.0722f
    return red + green + blue
}
