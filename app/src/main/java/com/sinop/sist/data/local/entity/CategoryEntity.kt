package com.sinop.sist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconName: String? = null,
    val colorHex: String? = null,
    val parentCategoryId: Long? = null,
    val isDefault: Boolean = false,
    val type: String // "income", "expense", "both"
)
