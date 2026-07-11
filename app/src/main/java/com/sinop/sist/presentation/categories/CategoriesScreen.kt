package com.sinop.sist.presentation.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sinop.sist.domain.model.Category
import com.sinop.sist.domain.model.CategoryType
import com.sinop.sist.presentation.components.SistTopBar
import com.sinop.sist.util.getCategoryIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel = viewModel(factory = CategoriesViewModel.factory())
) {
    val state by viewModel.state.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { SistTopBar(title = "Kategoriler") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ekle", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            items(state.categories) { category ->
                CategoryListItem(
                    category = category,
                    onDelete = { viewModel.deleteCategory(category) }
                )
            }
        }
    }

    if (showDialog) {
        AddCategoryDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name, type, icon, color ->
                viewModel.addCategory(name, type, icon, color)
                showDialog = false
            }
        )
    }
}

@Composable
private fun CategoryListItem(
    category: Category,
    onDelete: () -> Unit
) {
    val color = category.colorHex?.let { Color(android.graphics.Color.parseColor(it)) }
        ?: MaterialTheme.colorScheme.primaryContainer
    val iconColor = if (color.luminance() > 0.5f) Color.Black else Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category.iconName),
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = when (category.type) {
                            CategoryType.INCOME -> "Gelir"
                            CategoryType.EXPENSE -> "Gider"
                            CategoryType.BOTH -> "Gelir/Gider"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (!category.isDefault) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun Color.luminance(): Float {
    val red = red * 0.2126f
    val green = green * 0.7152f
    val blue = blue * 0.0722f
    return red + green + blue
}

private data class CategoryIconOption(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val categoryIconOptions = listOf(
    CategoryIconOption("category", Icons.Default.Category),
    CategoryIconOption("shopping_cart", Icons.Default.ShoppingCart),
    CategoryIconOption("directions_car", Icons.Default.DirectionsCar),
    CategoryIconOption("movie", Icons.Default.Movie),
    CategoryIconOption("receipt", Icons.Default.Receipt),
    CategoryIconOption("local_hospital", Icons.Default.LocalHospital),
    CategoryIconOption("trending_up", Icons.AutoMirrored.Filled.TrendingUp),
    CategoryIconOption("attach_money", Icons.Default.AttachMoney),
    CategoryIconOption("money_off", Icons.Default.MoneyOff),
    CategoryIconOption("work", Icons.Default.Work)
)

@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, CategoryType, String?, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(CategoryType.EXPENSE) }
    var selectedIcon by remember { mutableStateOf(categoryIconOptions[0].name) }
    val colors = listOf(
        "#F44336" to "Kırmızı",
        "#E91E63" to "Pembe",
        "#9C27B0" to "Mor",
        "#673AB7" to "Deep Purple",
        "#3F51B5" to "Indigo",
        "#2196F3" to "Mavi",
        "#03A9F4" to "Açık Mavi",
        "#00BCD4" to "Cyan",
        "#009688" to "Teal",
        "#4CAF50" to "Yeşil",
        "#8BC34A" to "Açık Yeşil",
        "#FFEB3B" to "Sarı",
        "#FF9800" to "Turuncu",
        "#795548" to "Kahverengi",
        "#607D8B" to "Gri"
    )
    var selectedColor by remember { mutableStateOf(colors[0].first) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Kategori") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Kategori adı") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = {
                                Text(
                                    when (type) {
                                        CategoryType.INCOME -> "Gelir"
                                        CategoryType.EXPENSE -> "Gider"
                                        CategoryType.BOTH -> "Her ikisi"
                                    }
                                )
                            }
                        )
                    }
                }
                Text("Simge", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categoryIconOptions.take(5).forEach { option ->
                        IconOption(
                            option = option,
                            selected = selectedIcon == option.name,
                            onClick = { selectedIcon = option.name }
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categoryIconOptions.drop(5).forEach { option ->
                        IconOption(
                            option = option,
                            selected = selectedIcon == option.name,
                            onClick = { selectedIcon = option.name }
                        )
                    }
                }
                Text("Renk", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colors.take(7).forEach { (hex, _) ->
                        ColorOption(
                            hex = hex,
                            selected = selectedColor == hex,
                            onClick = { selectedColor = hex }
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colors.drop(7).take(7).forEach { (hex, _) ->
                        ColorOption(
                            hex = hex,
                            selected = selectedColor == hex,
                            onClick = { selectedColor = hex }
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colors.drop(14).forEach { (hex, _) ->
                        ColorOption(
                            hex = hex,
                            selected = selectedColor == hex,
                            onClick = { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, selectedType, selectedIcon, selectedColor) },
                enabled = name.isNotBlank()
            ) { Text("Ekle") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@Composable
private fun IconOption(
    option: CategoryIconOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = option.name,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ColorOption(
    hex: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = Color(android.graphics.Color.parseColor(hex))
    val borderColor = if (selected) MaterialTheme.colorScheme.onSurface else androidx.compose.ui.graphics.Color.Transparent
    Card(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(3.dp, borderColor)
        } else null
    ) {}
}
