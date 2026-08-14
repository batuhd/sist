package com.sinop.sist.domain.repository

import com.sinop.sist.domain.model.PriceAlert
import kotlinx.coroutines.flow.Flow

interface PriceAlertRepository {
    fun getActiveAlerts(): Flow<List<PriceAlert>>
    fun getActiveAlertsForAsset(assetId: Long): Flow<List<PriceAlert>>
    suspend fun addAlert(alert: PriceAlert)
    suspend fun deleteAlert(alertId: Long)
    suspend fun markAlertTriggered(alertId: Long)
}
