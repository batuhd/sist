package com.sinop.sist.presentation.debts

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sinop.sist.domain.model.Debt
import com.sinop.sist.domain.model.DebtDirection
import com.sinop.sist.domain.model.Installment
import com.sinop.sist.presentation.components.EmptyState
import com.sinop.sist.presentation.components.SistTopBar
import com.sinop.sist.presentation.installments.InstallmentViewModel
import com.sinop.sist.util.formatCurrency
import com.sinop.sist.util.formatShort
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private enum class DialogType { NONE, DEBT, INSTALLMENT }

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DebtAndInstallmentScreen(
    debtViewModel: DebtViewModel = viewModel(factory = DebtViewModel.factory()),
    installmentViewModel: InstallmentViewModel = viewModel(factory = InstallmentViewModel.factory())
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    var dialogType by remember { mutableStateOf(DialogType.NONE) }
    val context = LocalContext.current

    Scaffold(
        topBar = { SistTopBar(title = "Borç & Taksit") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    dialogType = if (pagerState.currentPage == 0) DialogType.DEBT else DialogType.INSTALLMENT
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ekle", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text("Borçlar") }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("Taksitler") }
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> DebtList(debtViewModel)
                    1 -> InstallmentList(installmentViewModel)
                }
            }
        }
    }

    when (dialogType) {
        DialogType.DEBT -> AddDebtDialog(
            onDismiss = { dialogType = DialogType.NONE },
            onConfirm = { name, amount, direction, dueDate, note ->
                debtViewModel.addDebt(name, amount, direction, dueDate, note)
                Toast.makeText(context, "Borç eklendi", Toast.LENGTH_SHORT).show()
                dialogType = DialogType.NONE
            }
        )
        DialogType.INSTALLMENT -> AddInstallmentDialog(
            onDismiss = { dialogType = DialogType.NONE },
            onConfirm = { title, total, count, monthly, startDate, card, note ->
                installmentViewModel.addInstallment(title, total, count, monthly, startDate, card, note)
                Toast.makeText(context, "Taksit eklendi", Toast.LENGTH_SHORT).show()
                dialogType = DialogType.NONE
            }
        )
        DialogType.NONE -> {}
    }
}

@Composable
private fun DebtList(viewModel: DebtViewModel) {
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        item {
            SummaryCard(
                title = "Borç Özeti",
                items = listOf(
                    "Verdiğim Borç" to state.totalGiven.formatCurrency(),
                    "Aldığım Borç" to state.totalReceived.formatCurrency()
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.debts.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.Check,
                    title = "Henüz borç yok",
                    subtitle = "Verdiğiniz veya aldığınız borçları ekleyin"
                )
            }
        } else {
            items(state.debts, key = { it.id }) { debt ->
                DebtItem(
                    debt = debt,
                    onPay = { viewModel.markAsPaid(debt) },
                    onDelete = { viewModel.deleteDebt(debt) }
                )
            }
        }
    }
}

@Composable
private fun InstallmentList(viewModel: InstallmentViewModel) {
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        item {
            SummaryCard(
                title = "Taksit Özeti",
                items = listOf(
                    "Aylık Toplam" to state.totalMonthly.formatCurrency()
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.installments.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.CreditCard,
                    title = "Henüz taksit yok",
                    subtitle = "Kredi kartı taksitlerinizi ekleyin"
                )
            }
        } else {
            items(state.installments, key = { it.id }) { installment ->
                InstallmentItem(
                    installment = installment,
                    onPay = { viewModel.payInstallment(installment) },
                    onDelete = { viewModel.deleteInstallment(installment) }
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    items: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                items.forEach { (label, value) ->
                    Column {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DebtItem(
    debt: Debt,
    onPay: () -> Unit,
    onDelete: () -> Unit
) {
    val isGiven = debt.direction == DebtDirection.GIVEN
    val icon = if (isGiven) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack
    val iconBg = if (isGiven) Color(0xFFFFE8E8) else Color(0xFFE6F9F3)
    val iconColor = if (isGiven) Color(0xFFEF4444) else Color(0xFF00A884)
    val title = if (isGiven) "${debt.personName} (Verilen)" else "${debt.personName} (Alınan)"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    debt.dueDate?.let {
                        Text(
                            text = "Vade: ${it.formatShort()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (debt.isPaid) {
                        Text(
                            text = "Ödendi",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF00A884)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = debt.amount.formatCurrency(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isGiven) Color(0xFFEF4444) else Color(0xFF00A884)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!debt.isPaid) {
                        ActionButton(
                            text = "Öde",
                            color = Color(0xFF00A884),
                            onClick = onPay
                        )
                    }
                    ActionButton(
                        text = "Sil",
                        color = Color(0xFFEF4444),
                        onClick = onDelete
                    )
                }
            }
        }
    }
}

@Composable
private fun InstallmentItem(
    installment: Installment,
    onPay: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF3E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = installment.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${installment.remainingCount}/${installment.installmentCount} taksit kaldı",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    installment.cardOrAccount?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = installment.monthlyAmount.formatCurrency(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!installment.isCompleted) {
                        ActionButton(
                            text = "Öde",
                            color = Color(0xFF00A884),
                            onClick = onPay
                        )
                    }
                    ActionButton(
                        text = "Sil",
                        color = Color(0xFFEF4444),
                        onClick = onDelete
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDebtDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, DebtDirection, LocalDate?, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf(DebtDirection.GIVEN) }
    var dueDate by remember { mutableStateOf<LocalDate?>(null) }
    var note by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Borç") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Kişi adı") },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Tutar") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = direction == DebtDirection.GIVEN,
                        onClick = { direction = DebtDirection.GIVEN },
                        label = { Text("Verdim") }
                    )
                    FilterChip(
                        selected = direction == DebtDirection.RECEIVED,
                        onClick = { direction = DebtDirection.RECEIVED },
                        label = { Text("Aldım") }
                    )
                }
                OutlinedTextField(
                    value = dueDate?.formatShort() ?: "",
                    onValueChange = {},
                    label = { Text("Vade tarihi (opsiyonel)") },
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Tarih")
                        }
                    }
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Not (opsiyonel)") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    amount.toDoubleOrNull()?.let {
                        onConfirm(name, it, direction, dueDate, note.takeIf { n -> n.isNotBlank() })
                    }
                },
                enabled = name.isNotBlank() && amount.toDoubleOrNull() != null
            ) { Text("Ekle") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            dueDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) { Text("Tamam") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("İptal") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddInstallmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Int, Double, LocalDate, String?, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }
    var count by remember { mutableStateOf("") }
    var monthlyAmount by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var card by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Taksit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Başlık (örn: TV Taksidi)") },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = totalAmount,
                    onValueChange = { totalAmount = it },
                    label = { Text("Toplam tutar") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = count,
                        onValueChange = { count = it },
                        label = { Text("Taksit sayısı") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = monthlyAmount,
                        onValueChange = { monthlyAmount = it },
                        label = { Text("Aylık tutar") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                OutlinedTextField(
                    value = startDate.formatShort(),
                    onValueChange = {},
                    label = { Text("Başlangıç tarihi") },
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Tarih")
                        }
                    }
                )
                OutlinedTextField(
                    value = card,
                    onValueChange = { card = it },
                    label = { Text("Kart/Hesap (opsiyonel)") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val total = totalAmount.toDoubleOrNull()
                    val cnt = count.toIntOrNull()
                    val monthly = monthlyAmount.toDoubleOrNull()
                    if (total != null && cnt != null && monthly != null && title.isNotBlank()) {
                        onConfirm(title, total, cnt, monthly, startDate, card.takeIf { it.isNotBlank() }, note.takeIf { it.isNotBlank() })
                    }
                },
                enabled = title.isNotBlank() && totalAmount.toDoubleOrNull() != null &&
                        count.toIntOrNull() != null && monthlyAmount.toDoubleOrNull() != null
            ) { Text("Ekle") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            startDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) { Text("Tamam") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("İptal") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}
