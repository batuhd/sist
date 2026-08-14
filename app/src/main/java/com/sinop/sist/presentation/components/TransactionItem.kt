package com.sinop.sist.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sinop.sist.domain.model.Category
import com.sinop.sist.domain.model.Transaction
import com.sinop.sist.domain.model.TransactionType
import com.sinop.sist.ui.theme.LocalSistColors
import com.sinop.sist.ui.theme.SistRadius
import com.sinop.sist.ui.theme.SistTypography
import com.sinop.sist.util.formatCurrency
import com.sinop.sist.util.formatDateTime
import com.sinop.sist.util.getCategoryIcon

@Composable
fun TransactionItem(
    transaction: Transaction,
    category: Category?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sistColors = LocalSistColors.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(SistRadius.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                CategoryIcon(category = category)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category?.name ?: "Kategorisiz",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!transaction.note.isNullOrBlank()) {
                        Text(
                            text = transaction.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = transaction.date.formatDateTime(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                if (transaction.type == TransactionType.TRANSFER) {
                    Text(
                        text = transaction.amount.formatCurrency(transaction.currencyCode),
                        style = SistTypography.amountTitle,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}${transaction.amount.formatCurrency(transaction.currencyCode)}",
                        style = SistTypography.amountTitle,
                        color = if (transaction.type == TransactionType.INCOME) sistColors.positive else sistColors.negative
                    )
                }
                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Düzenle",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Sil",
                            tint = sistColors.negative,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryIcon(category: Category?, modifier: Modifier = Modifier) {
    val colorHex = category?.colorHex
    val backgroundColor = remember(colorHex) {
        colorHex?.let { Color(android.graphics.Color.parseColor(it)) }
    } ?: MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (backgroundColor.luminance() > 0.5f) Color.Black else Color.White

    Box(
        modifier = modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getCategoryIcon(category?.iconName),
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = contentColor
        )
    }
}

private fun Color.luminance(): Float {
    val red = red * 0.2126f
    val green = green * 0.7152f
    val blue = blue * 0.0722f
    return red + green + blue
}
