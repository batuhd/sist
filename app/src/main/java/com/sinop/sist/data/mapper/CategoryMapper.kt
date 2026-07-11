package com.sinop.sist.data.mapper

import com.sinop.sist.data.local.entity.CategoryEntity
import com.sinop.sist.domain.model.Category
import com.sinop.sist.domain.model.CategoryType

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    iconName = iconName,
    colorHex = colorHex,
    parentCategoryId = parentCategoryId,
    isDefault = isDefault,
    type = CategoryType.valueOf(type.uppercase())
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    iconName = iconName,
    colorHex = colorHex,
    parentCategoryId = parentCategoryId,
    isDefault = isDefault,
    type = type.name.lowercase()
)
