package com.sinop.sist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personName: String,
    val amount: Double,
    val direction: String, // "given" or "received"
    val dueDate: LocalDate? = null,
    val isPaid: Boolean = false,
    val paidDate: LocalDate? = null,
    val note: String? = null,
    val currencyCode: String = "TRY"
)
