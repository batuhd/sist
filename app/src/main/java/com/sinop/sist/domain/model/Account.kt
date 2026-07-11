package com.sinop.sist.domain.model

data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val iconName: String? = null,
    val colorHex: String? = null,
    val balance: Double = 0.0,
    val isDefault: Boolean = false,
    val isVisible: Boolean = true
)

enum class AccountType {
    CASH, BANK, INVESTMENT, OTHER
}
