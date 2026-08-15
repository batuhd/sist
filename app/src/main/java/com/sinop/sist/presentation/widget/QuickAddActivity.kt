package com.sinop.sist.presentation.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sinop.sist.SistApplication
import com.sinop.sist.domain.model.Account
import com.sinop.sist.domain.model.AccountType
import com.sinop.sist.domain.model.Category
import com.sinop.sist.domain.model.CategoryType
import com.sinop.sist.domain.model.PaymentMethod
import com.sinop.sist.domain.model.Transaction
import com.sinop.sist.domain.model.TransactionType
import com.sinop.sist.ui.theme.SistTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * Hizli Ekle widget'larindan acilan tam ekran islem girisi ekrani.
 * Ana uygulama ekranlarina dokunmaz; widget paketinin bir parcasi olarak
 * dogrudan repository'lere yazar.
 */
class QuickAddActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialType = when (intent.getStringExtra(QuickAddActivityContract.EXTRA_TYPE)) {
            QuickAddActivityContract.TYPE_INCOME -> TransactionType.INCOME
            QuickAddActivityContract.TYPE_TRANSFER -> TransactionType.TRANSFER
            else -> TransactionType.EXPENSE
        }
        val initialCategoryId = intent.getLongExtra(QuickAddActivityContract.EXTRA_CATEGORY_ID, -1L)

        setContent {
            SistTheme {
                QuickAddScreen(
                    initialType = initialType,
                    initialCategoryId = initialCategoryId,
                    onClose = { finish() }
                )
            }
        }
    }
}

@Composable
private fun QuickAddScreen(
    initialType: TransactionType,
    initialCategoryId: Long,
    onClose: () -> Unit
) {
    val app = LocalContext.current.applicationContext as SistApplication
    val scope = rememberCoroutineScope()

    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var accounts by remember { mutableStateOf<List<Account>>(emptyList()) }
    var type by remember { mutableStateOf(initialType) }
    var amountText by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf(if (initialCategoryId > 0) initialCategoryId else -1L) }
    var accountId by remember { mutableStateOf(-1L) }
    var fromAccountId by remember { mutableStateOf(-1L) }
    var toAccountId by remember { mutableStateOf(-1L) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        categories = app.container.categoryRepository.getAll().first()
        val seeded = app.container.accountRepository.seedDefaultAccounts()
        accounts = seeded
        val cash = seeded.firstOrNull { it.type == AccountType.CASH } ?: seeded.firstOrNull()
        val second = seeded.firstOrNull { it.id != cash?.id }
        accountId = cash?.id ?: -1L
        fromAccountId = cash?.id ?: -1L
        toAccountId = second?.id ?: -1L
        if (categoryId < 0) {
            categoryId = when (type) {
                TransactionType.INCOME -> categories.firstOrNull { it.type == CategoryType.INCOME || it.type == CategoryType.BOTH }?.id ?: -1L
                TransactionType.EXPENSE -> categories.firstOrNull { it.type == CategoryType.EXPENSE || it.type == CategoryType.BOTH }?.id ?: -1L
                else -> -1L
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    text = "Hızlı İşlem",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Birkaç dokunuşla kaydet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Kapat")
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = type == TransactionType.INCOME,
                    onClick = { type = TransactionType.INCOME; categoryId = -1L; error = null },
                    label = { Text("Gelir") }
                )
                FilterChip(
                    selected = type == TransactionType.EXPENSE,
                    onClick = { type = TransactionType.EXPENSE; categoryId = -1L; error = null },
                    label = { Text("Gider") }
                )
                FilterChip(
                    selected = type == TransactionType.TRANSFER,
                    onClick = { type = TransactionType.TRANSFER; error = null },
                    label = { Text("Transfer") }
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                label = { Text("Tutar") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (type != TransactionType.TRANSFER) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Kategori",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories
                        .filter {
                            it.type == CategoryType.BOTH ||
                                (type == TransactionType.INCOME && it.type == CategoryType.INCOME) ||
                                (type == TransactionType.EXPENSE && it.type == CategoryType.EXPENSE)
                        }
                        .forEach { category ->
                            FilterChip(
                                selected = categoryId == category.id,
                                onClick = {
                                    categoryId = category.id
                                    error = null
                                },
                                label = { Text(category.name) }
                            )
                        }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Hesap",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { account ->
                        FilterChip(
                            selected = accountId == account.id,
                            onClick = { accountId = account.id; error = null },
                            label = { Text(account.name) }
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Kaynak hesap",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { account ->
                        FilterChip(
                            selected = fromAccountId == account.id,
                            onClick = { fromAccountId = account.id; error = null },
                            label = { Text(account.name) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Hedef hesap",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { account ->
                        FilterChip(
                            selected = toAccountId == account.id,
                            onClick = { toAccountId = account.id; error = null },
                            label = { Text(account.name) }
                        )
                    }
                }
            }

            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(20.dp))
        }

        Button(
            onClick = {
                val amount = amountText.replace(',', '.').toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    error = "Geçerli bir tutar girin"
                } else if (type == TransactionType.TRANSFER && (fromAccountId < 0 || toAccountId < 0 || fromAccountId == toAccountId)) {
                    error = "Farklı kaynak ve hedef hesap seçin"
                } else if (type != TransactionType.TRANSFER && categoryId < 0) {
                    error = "Kategori seçin"
                } else if (type != TransactionType.TRANSFER && accountId < 0) {
                    error = "Hesap seçin"
                } else {
                    saving = true
                    scope.launch {
                        try {
                            val transferCategoryId = app.container.categoryRepository.getTransferCategoryId()
                            val now = LocalDateTime.now()
                            val selectedAccountId = if (type == TransactionType.TRANSFER) fromAccountId else accountId
                            val selectedAccount = accounts.firstOrNull { it.id == selectedAccountId }
                            val transaction = Transaction(
                                id = 0,
                                amount = amount,
                                type = type,
                                categoryId = if (type == TransactionType.TRANSFER) (transferCategoryId ?: 0L) else categoryId,
                                accountId = selectedAccountId,
                                toAccountId = if (type == TransactionType.TRANSFER) toAccountId else null,
                                date = now,
                                note = null,
                                tags = emptyList(),
                                paymentMethod = when {
                                    type == TransactionType.TRANSFER -> null
                                    selectedAccount?.type == AccountType.BANK -> PaymentMethod.BANK
                                    else -> PaymentMethod.CASH
                                }
                            )
                            app.container.transactionRepository.insert(transaction)
                            onClose()
                        } catch (e: Exception) {
                            saving = false
                            error = "Kaydedilemedi: ${e.message}"
                        }
                    }
                }
            },
            enabled = !saving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(if (saving) "Kaydediliyor..." else "Kaydet")
        }
    }
}
