package com.sinop.sist.data.repository

import com.sinop.sist.data.local.dao.PriceAlertDao
import com.sinop.sist.data.mapper.toDomain
import com.sinop.sist.data.mapper.toEntity
import com.sinop.sist.domain.model.PriceAlert
import com.sinop.sist.domain.repository.PriceAlertRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class PriceAlertRepositoryImpl(private val priceAlertDao: PriceAlertDao) : PriceAlertRepository {

    override fun getActiveAlerts(): Flow<List<PriceAlert>> {
        return priceAlertDao.getActiveAlerts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActiveAlertsForAsset(assetId: Long): Flow<List<PriceAlert>> {
        return priceAlertDao.getActiveAlertsForAsset(assetId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addAlert(alert: PriceAlert) {
        priceAlertDao.insertAlert(alert.toEntity())
    }

    override suspend fun deleteAlert(alertId: Long) {
        priceAlertDao.deleteAlertById(alertId)
    }

    override suspend fun markAlertTriggered(alertId: Long) {
        val alert = priceAlertDao.getAllAlerts().find { it.id == alertId } ?: return
        priceAlertDao.updateAlert(
            alert.copy(
                isActive = false,
                triggeredAt = LocalDateTime.now()
            )
        )
    }
}
