package com.sinop.sist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String, // "income" or "expense"
    val categoryId: Long,
    val title: String,
    val period: String, // daily, weekly, monthly, yearly
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val paymentMethod: String? = null,
    val currencyCode: String = "TRY",
    val isActive: Boolean = true
)
