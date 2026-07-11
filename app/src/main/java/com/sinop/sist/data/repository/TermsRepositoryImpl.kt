package com.sinop.sist.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.sinop.sist.domain.repository.TermsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.termsDataStore: DataStore<Preferences> by preferencesDataStore(name = "terms_prefs")

class TermsRepositoryImpl(private val context: Context) : TermsRepository {

    private val dataStore = context.termsDataStore

    override fun isTermsAccepted(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[TERMS_ACCEPTED_KEY] ?: false
        }
    }

    override suspend fun setTermsAccepted(accepted: Boolean) {
        dataStore.edit { preferences ->
            preferences[TERMS_ACCEPTED_KEY] = accepted
        }
    }

    companion object {
        private val TERMS_ACCEPTED_KEY = booleanPreferencesKey("terms_accepted")
    }
}
