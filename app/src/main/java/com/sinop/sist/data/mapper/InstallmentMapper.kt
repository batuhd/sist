package com.sinop.sist.data.mapper

import com.sinop.sist.data.local.entity.InstallmentEntity
import com.sinop.sist.domain.model.Installment

fun InstallmentEntity.toDomain(): Installment = Installment(
    id = id,
    title = title,
    totalAmount = totalAmount,
    installmentCount = installmentCount,
    monthlyAmount = monthlyAmount,
    startDate = startDate,
    cardOrAccount = cardOrAccount,
    remainingCount = remainingCount,
    note = note,
    currencyCode = currencyCode,
    isCompleted = isCompleted
)

fun Installment.toEntity(): InstallmentEntity = InstallmentEntity(
    id = id,
    title = title,
    totalAmount = totalAmount,
    installmentCount = installmentCount,
    monthlyAmount = monthlyAmount,
    startDate = startDate,
    cardOrAccount = cardOrAccount,
    remainingCount = remainingCount,
    note = note,
    currencyCode = currencyCode,
    isCompleted = isCompleted
)
