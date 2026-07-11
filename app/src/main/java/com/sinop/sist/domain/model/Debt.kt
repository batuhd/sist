package com.sinop.sist.domain.model

import java.time.LocalDate

data class Debt(
    val id: Long = 0,
    val personName: String,
    val amount: Double,
    val direction: DebtDirection,
    val dueDate: LocalDate? = null,
    val isPaid: Boolean = false,
    val paidDate: LocalDate? = null,
    val note: String? = null,
    val currencyCode: String = "TRY"
)

enum class DebtDirection { GIVEN, RECEIVED }
