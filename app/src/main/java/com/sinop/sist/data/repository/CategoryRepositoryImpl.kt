package com.sinop.sist.data.repository

import com.sinop.sist.data.local.dao.CategoryDao
import com.sinop.sist.data.mapper.toDomain
import com.sinop.sist.data.mapper.toEntity
import com.sinop.sist.domain.model.Category
import com.sinop.sist.domain.model.CategoryType
import com.sinop.sist.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override suspend fun insert(category: Category): Long {
        return categoryDao.insert(category.toEntity())
    }

    override suspend fun update(category: Category) {
        categoryDao.update(category.toEntity())
    }

    override suspend fun delete(category: Category) {
        categoryDao.delete(category.toEntity())
    }

    override suspend fun getById(id: Long): Category? {
        return categoryDao.getById(id)?.toDomain()
    }

    override fun getAll(): Flow<List<Category>> {
        return categoryDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override fun getByType(type: CategoryType): Flow<List<Category>> {
        return categoryDao.getByType(type.name.lowercase()).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun seedDefaultCategories(): List<Category> {
        val defaults = listOf(
            Category(name = "Maaş", iconName = "work", colorHex = "#4CAF50", type = CategoryType.INCOME, isDefault = true),
            Category(name = "Market", iconName = "shopping_cart", colorHex = "#FF9800", type = CategoryType.EXPENSE, isDefault = true),
            Category(name = "Ulaşım", iconName = "directions_car", colorHex = "#2196F3", type = CategoryType.EXPENSE, isDefault = true),
            Category(name = "Eğlence", iconName = "movie", colorHex = "#9C27B0", type = CategoryType.EXPENSE, isDefault = true),
            Category(name = "Faturalar", iconName = "receipt", colorHex = "#F44336", type = CategoryType.EXPENSE, isDefault = true),
            Category(name = "Sağlık", iconName = "local_hospital", colorHex = "#00BCD4", type = CategoryType.EXPENSE, isDefault = true),
            Category(name = "Yatırım", iconName = "trending_up", colorHex = "#3F51B5", type = CategoryType.EXPENSE, isDefault = true),
            Category(name = "Borç/Taksit", iconName = "receipt", colorHex = "#607D8B", type = CategoryType.EXPENSE, isDefault = true),
            Category(name = "Borç Geri Alma", iconName = "attach_money", colorHex = "#009688", type = CategoryType.INCOME, isDefault = true),
            Category(name = "Diğer Gelir", iconName = "attach_money", colorHex = "#8BC34A", type = CategoryType.INCOME, isDefault = true),
            Category(name = "Diğer Gider", iconName = "money_off", colorHex = "#795548", type = CategoryType.EXPENSE, isDefault = true)
        )

        val existing = categoryDao.getAll().first().map { it.toDomain() }
        val existingNames = existing.map { it.name }.toSet()
        val inserted = mutableListOf<Category>()

        defaults.forEach { default ->
            if (default.name !in existingNames) {
                val newId = categoryDao.insert(default.toEntity())
                inserted.add(default.copy(id = newId))
            }
        }

        return existing + inserted
    }
}
