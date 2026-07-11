package com.sinop.sist.data.mapper

import com.sinop.sist.data.local.entity.TransactionEntity
import com.sinop.sist.domain.model.PaymentMethod
import com.sinop.sist.domain.model.Transaction
import com.sinop.sist.domain.model.TransactionType

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    amount = amount,
    type = TransactionType.valueOf(type.uppercase()),
    categoryId = categoryId,
    accountId = accountId,
    date = date,
    note = note,
    tags = tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
    paymentMethod = paymentMethod?.let { PaymentMethod.valueOf(it.uppercase()) },
    currencyCode = currencyCode,
    isRecurring = isRecurring,
    recurringId = recurringId
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    amount = amount,
    type = type.name.lowercase(),
    categoryId = categoryId,
    accountId = accountId,
    date = date,
    note = note,
    tags = tags.joinToString(","),
    paymentMethod = paymentMethod?.name?.lowercase(),
    currencyCode = currencyCode,
    isRecurring = isRecurring,
    recurringId = recurringId
)
