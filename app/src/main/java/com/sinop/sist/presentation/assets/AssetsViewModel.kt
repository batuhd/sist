package com.sinop.sist.presentation.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sinop.sist.SistApplication
import com.sinop.sist.domain.model.AssetWithPrice
import com.sinop.sist.domain.model.PortfolioSummary
import com.sinop.sist.domain.repository.AssetRepository
import com.sinop.sist.domain.usecase.RefreshAssetPricesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AssetsViewModel(
    private val assetRepository: AssetRepository,
    private val refreshAssetPricesUseCase: RefreshAssetPricesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AssetsState())
    val state: StateFlow<AssetsState> = _state.asStateFlow()

    init {
        loadAssets()
        loadSummary()
        refreshPrices()
    }

    private fun loadAssets() {
        assetRepository.getAssetsWithPrices()
            .onEach { assets ->
                val hadAssets = _state.value.assets.isNotEmpty()
                _state.value = _state.value.copy(assets = assets)
                if (assets.isNotEmpty() && !hadAssets && !_state.value.isRefreshing) {
                    refreshPrices()
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadSummary() {
        assetRepository.getPortfolioSummary()
            .onEach { summary ->
                _state.value = _state.value.copy(summary = summary)
            }
            .launchIn(viewModelScope)
    }

    fun refreshPrices() {
        if (_state.value.isRefreshing) return
        val assets = _state.value.assets.map { it.asset }
        if (assets.isEmpty()) return

        _state.value = _state.value.copy(isRefreshing = true, refreshMessage = null)
        viewModelScope.launch {
            val result = refreshAssetPricesUseCase(assets)
            val message = buildRefreshMessage(result)
            _state.value = _state.value.copy(
                isRefreshing = false,
                refreshMessage = message
            )
        }
    }

    private fun buildRefreshMessage(result: com.sinop.sist.domain.usecase.RefreshResult): String? {
        return when {
            result.fetched.isNotEmpty() && result.failed.isEmpty() ->
                "${result.fetched.size} fiyat güncellendi"
            result.fetched.isNotEmpty() && result.failed.isNotEmpty() ->
                "${result.fetched.size} güncellendi, ${result.failed.size} otomatik bulunamadı"
            result.fetched.isEmpty() && result.failed.isNotEmpty() ->
                "Otomatik fiyat bulunamadı, fonlar için manuel giriş yapabilirsiniz"
            else -> null
        }
    }

    fun consumeRefreshMessage() {
        _state.value = _state.value.copy(refreshMessage = null)
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = SistApplication.instance
                    ?: throw IllegalStateException("Application not initialized")
                return AssetsViewModel(
                    app.container.assetRepository,
                    app.container.refreshAssetPricesUseCase
                ) as T
            }
        }
    }
}

data class AssetsState(
    val assets: List<AssetWithPrice> = emptyList(),
    val summary: PortfolioSummary? = null,
    val isRefreshing: Boolean = false,
    val refreshMessage: String? = null
)