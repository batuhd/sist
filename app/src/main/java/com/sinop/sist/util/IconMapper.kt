package com.sinop.sist.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.sinop.sist.domain.model.AccountType

fun getCategoryIcon(iconName: String?): ImageVector {
    return when (iconName) {
        "work" -> Icons.Default.Work
        "shopping_cart" -> Icons.Default.ShoppingCart
        "directions_car" -> Icons.Default.DirectionsCar
        "movie" -> Icons.Default.Movie
        "receipt" -> Icons.Default.Receipt
        "local_hospital" -> Icons.Default.LocalHospital
        "trending_up" -> Icons.AutoMirrored.Filled.TrendingUp
        "attach_money" -> Icons.Default.AttachMoney
        "money_off" -> Icons.Default.MoneyOff
        else -> Icons.Default.Category
    }
}

fun getAccountIcon(type: AccountType): ImageVector {
    return when (type) {
        AccountType.CASH -> Icons.Default.Payments
        AccountType.BANK -> Icons.Default.AccountBalance
        AccountType.INVESTMENT -> Icons.AutoMirrored.Filled.TrendingUp
        AccountType.OTHER -> Icons.Default.Category
    }
}
