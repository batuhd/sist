package com.sinop.sist.data.mapper

import com.sinop.sist.data.local.entity.RecurringTransactionEntity
import com.sinop.sist.domain.model.PaymentMethod
import com.sinop.sist.domain.model.RecurrencePeriod
import com.sinop.sist.domain.model.RecurringTransaction
import com.sinop.sist.domain.model.TransactionType

fun RecurringTransactionEntity.toDomain(): RecurringTransaction = RecurringTransaction(
    id = id,
    amount = amount,
    type = TransactionType.valueOf(type.uppercase()),
    categoryId = categoryId,
    title = title,
    period = RecurrencePeriod.valueOf(period.uppercase()),
    startDate = startDate,
    endDate = endDate,
    paymentMethod = paymentMethod?.let { PaymentMethod.valueOf(it.uppercase()) },
    currencyCode = currencyCode,
    isActive = isActive
)

fun RecurringTransaction.toEntity(): RecurringTransactionEntity = RecurringTransactionEntity(
    id = id,
    amount = amount,
    type = type.name.lowercase(),
    categoryId = categoryId,
    title = title,
    period = period.name.lowercase(),
    startDate = startDate,
    endDate = endDate,
    paymentMethod = paymentMethod?.name?.lowercase(),
    currencyCode = currencyCode,
    isActive = isActive
)
