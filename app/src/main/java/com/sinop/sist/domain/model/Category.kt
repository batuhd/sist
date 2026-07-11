package com.sinop.sist.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val iconName: String? = null,
    val colorHex: String? = null,
    val parentCategoryId: Long? = null,
    val isDefault: Boolean = false,
    val type: CategoryType = CategoryType.BOTH
)

enum class CategoryType { INCOME, EXPENSE, BOTH }
