package com.sinop.sist.domain.repository

import kotlinx.coroutines.flow.Flow

interface NotificationSettingsRepository {
    fun isPriceAlertsEnabled(): Flow<Boolean>
    fun isMarketCloseEnabled(): Flow<Boolean>
    fun isBudgetAlertsEnabled(): Flow<Boolean>
    suspend fun setPriceAlertsEnabled(enabled: Boolean)
    suspend fun setMarketCloseEnabled(enabled: Boolean)
    suspend fun setBudgetAlertsEnabled(enabled: Boolean)
}
