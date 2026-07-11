package com.sinop.sist.domain.repository

import com.sinop.sist.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    suspend fun insert(account: Account): Long
    suspend fun update(account: Account)
    suspend fun delete(account: Account)
    suspend fun getById(id: Long): Account?
    fun getAll(): Flow<List<Account>>
    suspend fun seedDefaultAccounts(): List<Account>
}
