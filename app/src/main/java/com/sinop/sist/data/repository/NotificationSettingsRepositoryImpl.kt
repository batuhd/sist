package com.sinop.sist.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.sinop.sist.domain.repository.NotificationSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_settings")

class NotificationSettingsRepositoryImpl(private val context: Context) : NotificationSettingsRepository {

    private val dataStore = context.notificationDataStore

    override fun isPriceAlertsEnabled(): Flow<Boolean> {
        return dataStore.data.map { it[PRICE_ALERTS_KEY] ?: true }
    }

    override fun isMarketCloseEnabled(): Flow<Boolean> {
        return dataStore.data.map { it[MARKET_CLOSE_KEY] ?: true }
    }

    override fun isBudgetAlertsEnabled(): Flow<Boolean> {
        return dataStore.data.map { it[BUDGET_ALERTS_KEY] ?: true }
    }

    override suspend fun setPriceAlertsEnabled(enabled: Boolean) {
        dataStore.edit { it[PRICE_ALERTS_KEY] = enabled }
    }

    override suspend fun setMarketCloseEnabled(enabled: Boolean) {
        dataStore.edit { it[MARKET_CLOSE_KEY] = enabled }
    }

    override suspend fun setBudgetAlertsEnabled(enabled: Boolean) {
        dataStore.edit { it[BUDGET_ALERTS_KEY] = enabled }
    }

    companion object {
        private val PRICE_ALERTS_KEY = booleanPreferencesKey("price_alerts_enabled")
        private val MARKET_CLOSE_KEY = booleanPreferencesKey("market_close_enabled")
        private val BUDGET_ALERTS_KEY = booleanPreferencesKey("budget_alerts_enabled")
    }
}
