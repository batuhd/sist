package com.sinop.sist.presentation.assets.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sinop.sist.SistApplication
import com.sinop.sist.domain.model.Asset
import com.sinop.sist.domain.model.AssetTransaction
import com.sinop.sist.domain.model.AssetWithPrice
import com.sinop.sist.domain.model.PriceCache
import com.sinop.sist.domain.repository.AssetRepository
import com.sinop.sist.domain.repository.PriceCacheRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AssetDetailViewModel(
    private val assetRepository: AssetRepository,
    private val priceCacheRepository: PriceCacheRepository,
    private val assetId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(AssetDetailState())
    val state: StateFlow<AssetDetailState> = _state.asStateFlow()

    init {
        loadAsset()
        loadTransactions()
        loadAssetWithPrice()
    }

    private fun loadAsset() {
        viewModelScope.launch {
            assetRepository.getAssetById(assetId)?.let { asset ->
                _state.value = _state.value.copy(asset = asset)
            }
        }
    }

    private fun loadTransactions() {
        assetRepository.getTransactionsByAssetId(assetId)
            .onEach { transactions ->
                _state.value = _state.value.copy(transactions = transactions)
            }
            .launchIn(viewModelScope)
    }

    private fun loadAssetWithPrice() {
        assetRepository.getAssetsWithPrices()
            .onEach { assets ->
                assets.find { it.asset.id == assetId }?.let { assetWithPrice ->
                    _state.value = _state.value.copy(assetWithPrice = assetWithPrice)
                }
            }
            .launchIn(viewModelScope)
    }

    fun updatePrice(price: Double) {
        viewModelScope.launch {
            val symbol = _state.value.asset?.symbol ?: return@launch
            priceCacheRepository.savePrice(
                PriceCache(
                    symbol = symbol,
                    lastPrice = price,
                    lastUpdated = LocalDateTime.now(),
                    source = "manual"
                )
            )
        }
    }

    fun deleteTransaction(transaction: AssetTransaction) {
        viewModelScope.launch {
            assetRepository.deleteTransaction(transaction)
        }
    }

    fun deleteAsset(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _state.value.asset?.let { asset ->
                assetRepository.deleteAsset(asset)
                onDeleted()
            }
        }
    }

    companion object {
        fun factory(assetId: Long): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = SistApplication.instance
                    ?: throw IllegalStateException("Application not initialized")
                return AssetDetailViewModel(
                    app.container.assetRepository,
                    app.container.priceCacheRepository,
                    assetId
                ) as T
            }
        }
    }
}

data class AssetDetailState(
    val asset: Asset? = null,
    val transactions: List<AssetTransaction> = emptyList(),
    val assetWithPrice: AssetWithPrice? = null
)