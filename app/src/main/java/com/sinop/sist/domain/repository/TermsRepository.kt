package com.sinop.sist.domain.repository

import kotlinx.coroutines.flow.Flow

interface TermsRepository {
    fun isTermsAccepted(): Flow<Boolean>
    suspend fun setTermsAccepted(accepted: Boolean)
}
