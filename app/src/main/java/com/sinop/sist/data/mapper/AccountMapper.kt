package com.sinop.sist.data.mapper

import com.sinop.sist.data.local.entity.AccountEntity
import com.sinop.sist.domain.model.Account
import com.sinop.sist.domain.model.AccountType

fun AccountEntity.toDomain(): Account = Account(
    id = id,
    name = name,
    type = AccountType.valueOf(type.uppercase()),
    iconName = iconName,
    colorHex = colorHex,
    balance = balance,
    isDefault = isDefault,
    isVisible = isVisible
)

fun Account.toEntity(): AccountEntity = AccountEntity(
    id = id,
    name = name,
    type = type.name.lowercase(),
    iconName = iconName,
    colorHex = colorHex,
    balance = balance,
    isDefault = isDefault,
    isVisible = isVisible
)
