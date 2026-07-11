package com.sinop.sist.presentation.assets.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sinop.sist.SistApplication
import com.sinop.sist.domain.model.AssetTransaction
import com.sinop.sist.domain.model.AssetTransactionType
import com.sinop.sist.domain.repository.AssetRepository
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
    private val assetId: Long,
    private val transactionId: Long?
) : ViewModel() {

    private val _state = MutableStateFlow(AddAssetTransactionState())
    val state: StateFlow<AddAssetTransactionState> = _state.asStateFlow()

    private val _event = MutableStateFlow<AddAssetTransactionEvent?>(null)
    val event: StateFlow<AddAssetTransactionEvent?> = _event.asStateFlow()

    init {
        transactionId?.let { loadTransaction(it) }
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
        _state.value = _state.value.copy(transactionType = type)
    }

    fun onQuantityChange(quantity: String) {
        _state.value = _state.value.copy(quantity = quantity)
    }

    fun onPricePerUnitChange(price: String) {
        _state.value = _state.value.copy(pricePerUnit = price)
    }

    fun onFeeChange(fee: String) {
        _state.value = _state.value.copy(fee = fee)
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

            val transaction = AssetTransaction(
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
                assetRepository.updateTransaction(transaction)
            } else {
                assetRepository.insertTransaction(transaction)
            }
            _event.value = AddAssetTransactionEvent.Saved
        }
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
    val error: String? = null
)

sealed class AddAssetTransactionEvent {
    data object Saved : AddAssetTransactionEvent()
}