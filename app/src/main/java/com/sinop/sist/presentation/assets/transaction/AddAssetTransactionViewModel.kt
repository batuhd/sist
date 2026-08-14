package com.sinop.sist.presentation.assets.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sinop.sist.SistApplication
import com.sinop.sist.domain.model.Account
import com.sinop.sist.domain.model.AccountType
import com.sinop.sist.domain.model.AssetTransaction
import com.sinop.sist.domain.model.AssetTransactionType
import com.sinop.sist.domain.model.Transaction
import com.sinop.sist.domain.model.TransactionType
import com.sinop.sist.domain.repository.AccountRepository
import com.sinop.sist.domain.repository.AssetRepository
import com.sinop.sist.domain.repository.CategoryRepository
import com.sinop.sist.domain.repository.TransactionRepository
import com.sinop.sist.util.formatCurrency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class AddAssetTransactionViewModel(
    private val assetRepository: AssetRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val assetId: Long,
    private val transactionId: Long?
) : ViewModel() {

    private val _state = MutableStateFlow(AddAssetTransactionState())
    val state: StateFlow<AddAssetTransactionState> = _state.asStateFlow()

    private val _event = MutableStateFlow<AddAssetTransactionEvent?>(null)
    val event: StateFlow<AddAssetTransactionEvent?> = _event.asStateFlow()

    init {
        loadAccounts()
        transactionId?.let { loadTransaction(it) }
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            val accounts = accountRepository.seedDefaultAccounts()
            val sourceId = _state.value.sourceAccountId
                ?: accounts.firstOrNull { it.type == AccountType.CASH }?.id
            _state.value = _state.value.copy(
                accounts = accounts,
                sourceAccountId = sourceId,
                selectedAccountBalance = computeAccountBalance(sourceId)
            )
        }
    }

    private suspend fun computeAccountBalance(accountId: Long?): Double? {
        if (accountId == null) return null
        return transactionRepository.getAll().first().sumOf { transaction ->
            when {
                transaction.type == TransactionType.TRANSFER && transaction.accountId == accountId -> -transaction.amount
                transaction.type == TransactionType.TRANSFER && transaction.toAccountId == accountId -> transaction.amount
                transaction.type == TransactionType.INCOME && transaction.accountId == accountId -> transaction.amount
                transaction.type == TransactionType.EXPENSE && transaction.accountId == accountId -> -transaction.amount
                else -> 0.0
            }
        }
    }

    private fun loadTransaction(id: Long) {
        viewModelScope.launch {
            assetRepository.getTransactionById(id)?.let { transaction ->
                _state.value = _state.value.copy(
                    transactionType = transaction.transactionType,
                    quantity = transaction.quantity.toString(),
                    pricePerUnit = transaction.pricePerUnit.toString(),
                    fee = transaction.fee.toString(),
                    date = transaction.transactionDate.toLocalDate(),
                    time = transaction.transactionDate.toLocalTime(),
                    note = transaction.note ?: "",
                    isEditing = true
                )
            }
        }
    }

    fun onTransactionTypeChange(type: AssetTransactionType) {
        _state.value = _state.value.copy(transactionType = type, error = null)
    }

    fun onQuantityChange(quantity: String) {
        _state.value = _state.value.copy(quantity = quantity, error = null)
    }

    fun onPricePerUnitChange(price: String) {
        _state.value = _state.value.copy(pricePerUnit = price, error = null)
    }

    fun onFeeChange(fee: String) {
        _state.value = _state.value.copy(fee = fee, error = null)
    }

    fun onDateChange(date: LocalDate) {
        _state.value = _state.value.copy(date = date)
    }

    fun onTimeChange(time: LocalTime) {
        _state.value = _state.value.copy(time = time)
    }

    fun onNoteChange(note: String) {
        _state.value = _state.value.copy(note = note)
    }

    fun onLinkTransferChange(linkTransfer: Boolean) {
        viewModelScope.launch {
            val balance = if (linkTransfer) computeAccountBalance(_state.value.sourceAccountId) else null
            _state.value = _state.value.copy(
                linkTransfer = linkTransfer,
                selectedAccountBalance = balance,
                error = null
            )
        }
    }

    fun onSourceAccountChange(accountId: Long?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                sourceAccountId = accountId,
                selectedAccountBalance = computeAccountBalance(accountId),
                error = null
            )
        }
    }

    fun saveTransaction() {
        val currentState = _state.value
        val quantity = currentState.quantity.toDoubleOrNull()
        val pricePerUnit = currentState.pricePerUnit.toDoubleOrNull()
        val fee = currentState.fee.toDoubleOrNull() ?: 0.0

        if (quantity == null || quantity <= 0) {
            _state.value = currentState.copy(error = "Geçerli adet girin")
            return
        }
        if (pricePerUnit == null || pricePerUnit < 0) {
            _state.value = currentState.copy(error = "Geçerli birim fiyat girin")
            return
        }
        if (currentState.linkTransfer && currentState.sourceAccountId == null) {
            _state.value = currentState.copy(error = "Para çekilecek hesabı seçin")
            return
        }
        if (currentState.transactionType == AssetTransactionType.BUY &&
            currentState.linkTransfer &&
            currentState.selectedAccountBalance != null
        ) {
            val totalAmount = quantity * pricePerUnit + fee
            if (totalAmount > currentState.selectedAccountBalance) {
                _state.value = currentState.copy(
                    error = "Yetersiz bakiye. Kullanılabilir: ${currentState.selectedAccountBalance.formatCurrency()}"
                )
                return
            }
        }

        viewModelScope.launch {
            if (currentState.transactionType == AssetTransactionType.SELL && transactionId == null) {
                val existing = assetRepository.getTransactionsByAssetId(assetId).first()
                val owned = existing
                    .filter { it.transactionType == AssetTransactionType.BUY }
                    .sumOf { it.quantity } -
                    existing
                        .filter { it.transactionType == AssetTransactionType.SELL }
                        .sumOf { it.quantity }
                if (quantity > owned) {
                    _state.value = currentState.copy(error = "Satış için yeterli adet yok (sahip olunan: ${owned.formatQuantity()})")
                    return@launch
                }
            }

            val assetTransaction = AssetTransaction(
                id = transactionId ?: 0,
                assetId = assetId,
                quantity = quantity,
                pricePerUnit = pricePerUnit,
                transactionDate = LocalDateTime.of(currentState.date, currentState.time),
                transactionType = currentState.transactionType,
                fee = fee,
                note = currentState.note.takeIf { it.isNotBlank() },
                currencyCode = "TRY"
            )

            if (transactionId != null) {
                assetRepository.updateTransaction(assetTransaction)
            } else {
                assetRepository.insertTransaction(assetTransaction)
                if (currentState.transactionType == AssetTransactionType.BUY && currentState.linkTransfer) {
                    insertLinkedCashTransfer(
                        currentState = currentState,
                        totalAmount = quantity * pricePerUnit + fee
                    )
                }
            }
            _event.value = AddAssetTransactionEvent.Saved
        }
    }

    private suspend fun insertLinkedCashTransfer(
        currentState: AddAssetTransactionState,
        totalAmount: Double
    ) {
        if (totalAmount <= 0) return
        val sourceAccountId = currentState.sourceAccountId ?: return
        val targetAccountId = currentState.accounts
            .firstOrNull { it.type == AccountType.INVESTMENT }?.id ?: return
        val asset = assetRepository.getAssetById(assetId) ?: return
        val transferCategoryId = categoryRepository.getTransferCategoryId() ?: return

        val transfer = Transaction(
            amount = totalAmount,
            type = TransactionType.TRANSFER,
            categoryId = transferCategoryId,
            accountId = sourceAccountId,
            toAccountId = targetAccountId,
            date = LocalDateTime.of(currentState.date, currentState.time),
            note = "Varlık alımı: ${asset.symbol}",
            tags = emptyList(),
            paymentMethod = null,
            currencyCode = "TRY"
        )
        transactionRepository.insert(transfer)
    }

    private fun Double.formatQuantity(): String = when {
        this % 1.0 == 0.0 -> this.toInt().toString()
        else -> "%.4f".format(this).trimEnd('0').trimEnd('.')
    }

    fun consumeEvent() {
        _event.value = null
    }

    companion object {
        fun factory(assetId: Long, transactionId: Long? = null): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = SistApplication.instance
                    ?: throw IllegalStateException("Application not initialized")
                return AddAssetTransactionViewModel(
                    app.container.assetRepository,
                    app.container.accountRepository,
                    app.container.transactionRepository,
                    app.container.categoryRepository,
                    assetId,
                    transactionId
                ) as T
            }
        }
    }
}

data class AddAssetTransactionState(
    val transactionType: AssetTransactionType = AssetTransactionType.BUY,
    val quantity: String = "",
    val pricePerUnit: String = "",
    val fee: String = "",
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val note: String = "",
    val isEditing: Boolean = false,
    val linkTransfer: Boolean = false,
    val sourceAccountId: Long? = null,
    val accounts: List<Account> = emptyList(),
    val selectedAccountBalance: Double? = null,
    val error: String? = null
)

sealed class AddAssetTransactionEvent {
    data object Saved : AddAssetTransactionEvent()
}
