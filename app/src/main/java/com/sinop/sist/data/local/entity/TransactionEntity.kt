package com.sinop.sist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String, // "income" or "expense"
    val categoryId: Long,
    val accountId: Long? = null,
    val date: LocalDateTime,
    val note: String? = null,
    val tags: String? = null, // comma separated tags
    val paymentMethod: String? = null, // cash, card, bank
    val currencyCode: String = "TRY",
    val isRecurring: Boolean = false,
    val recurringId: Long? = null
)
