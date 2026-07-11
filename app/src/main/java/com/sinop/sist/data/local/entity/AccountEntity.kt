package com.sinop.sist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String,
    val iconName: String? = null,
    val colorHex: String? = null,
    val balance: Double = 0.0,
    val isDefault: Boolean = false,
    val isVisible: Boolean = true
)
