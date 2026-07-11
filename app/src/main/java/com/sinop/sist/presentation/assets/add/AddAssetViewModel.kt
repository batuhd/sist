package com.sinop.sist.presentation.assets.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sinop.sist.SistApplication
import com.sinop.sist.domain.model.Asset
import com.sinop.sist.domain.model.AssetType
import com.sinop.sist.domain.model.PriceCache
import com.sinop.sist.domain.repository.AssetRepository
import com.sinop.sist.domain.repository.PriceCacheRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AddAssetViewModel(
    private val assetRepository: AssetRepository,
    private val priceCacheRepository: PriceCacheRepository,
    private val assetId: Long?
) : ViewModel() {

    private val _state = MutableStateFlow(AddAssetState())
    val state: StateFlow<AddAssetState> = _state.asStateFlow()

    private val _event = MutableStateFlow<AddAssetEvent?>(null)
    val event: StateFlow<AddAssetEvent?> = _event.asStateFlow()

    init {
        assetId?.let { loadAsset(it) }
    }

    private fun loadAsset(id: Long) {
        viewModelScope.launch {
            assetRepository.getAssetById(id)?.let { asset ->
                val price = priceCacheRepository.getPrice(asset.symbol)?.lastPrice
                _state.value = _state.value.copy(
                    symbol = asset.symbol,
                    name = asset.name ?: "",
                    assetType = asset.assetType,
                    currencyCode = asset.currencyCode,
                    currentPrice = price?.toString() ?: "",
                    isEditing = true
                )
            }
        }
    }

    fun onSymbolChange(symbol: String) {
        _state.value = _state.value.copy(symbol = symbol.uppercase())
    }

    fun onNameChange(name: String) {
        _state.value = _state.value.copy(name = name)
    }

    fun onAssetTypeChange(type: AssetType) {
        _state.value = _state.value.copy(assetType = type)
    }

    fun onCurrentPriceChange(price: String) {
        _state.value = _state.value.copy(currentPrice = price)
    }

    fun onCurrencyChange(currency: String) {
        _state.value = _state.value.copy(currencyCode = currency.uppercase())
    }

    fun saveAsset() {
        val currentState = _state.value
        val symbol = currentState.symbol.trim().uppercase()
        if (symbol.isBlank()) {
            _state.value = currentState.copy(error = "Sembol girin")
            return
        }

        viewModelScope.launch {
            val existing = assetRepository.getAssetBySymbol(symbol)
            if (existing != null && existing.id != (assetId ?: 0)) {
                _state.value = currentState.copy(error = "Bu sembol zaten kayıtlı")
                return@launch
            }

            val asset = Asset(
                id = assetId ?: 0,
                symbol = symbol,
                assetType = currentState.assetType,
                name = currentState.name.takeIf { it.isNotBlank() },
                currencyCode = currentState.currencyCode
            )

            val assetIdResult = if (assetId != null) {
                assetRepository.updateAsset(asset)
                assetId
            } else {
                assetRepository.insertAsset(asset)
            }

            val priceValue = currentState.currentPrice.toDoubleOrNull()
            if (priceValue != null && priceValue >= 0) {
                priceCacheRepository.savePrice(
                    PriceCache(
                        symbol = symbol,
                        lastPrice = priceValue,
                        lastUpdated = LocalDateTime.now(),
                        source = "manual"
                    )
                )
            }

            _event.value = AddAssetEvent.Saved(assetIdResult)
        }
    }

    fun consumeEvent() {
        _event.value = null
    }

    companion object {
        fun factory(assetId: Long? = null): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = SistApplication.instance
                    ?: throw IllegalStateException("Application not initialized")
                return AddAssetViewModel(
                    app.container.assetRepository,
                    app.container.priceCacheRepository,
                    assetId
                ) as T
            }
        }
    }
}

data class AddAssetState(
    val symbol: String = "",
    val name: String = "",
    val assetType: AssetType = AssetType.STOCK,
    val currentPrice: String = "",
    val currencyCode: String = "TRY",
    val isEditing: Boolean = false,
    val error: String? = null
)

sealed class AddAssetEvent {
    data class Saved(val assetId: Long) : AddAssetEvent()
}