package com.sinop.sist.data.mapper

import com.sinop.sist.data.local.entity.DebtEntity
import com.sinop.sist.domain.model.Debt
import com.sinop.sist.domain.model.DebtDirection

fun DebtEntity.toDomain(): Debt = Debt(
    id = id,
    personName = personName,
    amount = amount,
    direction = DebtDirection.valueOf(direction.uppercase()),
    dueDate = dueDate,
    isPaid = isPaid,
    paidDate = paidDate,
    note = note,
    currencyCode = currencyCode
)

fun Debt.toEntity(): DebtEntity = DebtEntity(
    id = id,
    personName = personName,
    amount = amount,
    direction = direction.name.lowercase(),
    dueDate = dueDate,
    isPaid = isPaid,
    paidDate = paidDate,
    note = note,
    currencyCode = currencyCode
)
