package com.sinop.sist.data.repository

import com.sinop.sist.data.local.dao.AccountDao
import com.sinop.sist.data.mapper.toDomain
import com.sinop.sist.data.mapper.toEntity
import com.sinop.sist.domain.model.Account
import com.sinop.sist.domain.model.AccountType
import com.sinop.sist.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AccountRepositoryImpl(
    private val accountDao: AccountDao
) : AccountRepository {

    override suspend fun insert(account: Account): Long {
        return accountDao.insert(account.toEntity())
    }

    override suspend fun update(account: Account) {
        accountDao.update(account.toEntity())
    }

    override suspend fun delete(account: Account) {
        accountDao.delete(account.toEntity())
    }

    override suspend fun getById(id: Long): Account? {
        return accountDao.getById(id)?.toDomain()
    }

    override fun getAll(): Flow<List<Account>> {
        return accountDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun seedDefaultAccounts(): List<Account> {
        val defaults = listOf(
            Account(name = "Nakit", type = AccountType.CASH, iconName = "payments", colorHex = "#4CAF50", isDefault = true),
            Account(name = "Portföy", type = AccountType.INVESTMENT, iconName = "trending_up", colorHex = "#9C27B0", isDefault = true)
        )

        val existing = accountDao.getAll().first().map { it.toDomain() }
        val existingByName = existing.associateBy { it.name }
        val inserted = mutableListOf<Account>()
        val updated = mutableListOf<Account>()

        defaults.forEach { default ->
            val current = existingByName[default.name]
            if (current == null) {
                val newId = accountDao.insert(default.toEntity())
                inserted.add(default.copy(id = newId))
            } else if (current.type != default.type || current.iconName != default.iconName || current.colorHex != default.colorHex || !current.isDefault) {
                val fixed = current.copy(
                    type = default.type,
                    iconName = default.iconName,
                    colorHex = default.colorHex,
                    isDefault = true
                )
                accountDao.update(fixed.toEntity())
                updated.add(fixed)
            }
        }

        return existing.map { existingAccount ->
            updated.find { it.id == existingAccount.id } ?: existingAccount
        } + inserted
    }
}
