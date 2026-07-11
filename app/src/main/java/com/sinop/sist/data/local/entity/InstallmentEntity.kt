package com.sinop.sist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "installments")
data class InstallmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val totalAmount: Double,
    val installmentCount: Int,
    val monthlyAmount: Double,
    val startDate: LocalDate,
    val cardOrAccount: String? = null,
    val remainingCount: Int,
    val note: String? = null,
    val currencyCode: String = "TRY",
    val isCompleted: Boolean = false
)
