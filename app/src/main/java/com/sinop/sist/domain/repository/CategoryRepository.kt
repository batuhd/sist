package com.sinop.sist.domain.repository

import com.sinop.sist.domain.model.Category
import com.sinop.sist.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun insert(category: Category): Long
    suspend fun update(category: Category)
    suspend fun delete(category: Category)
    suspend fun getById(id: Long): Category?
    fun getAll(): Flow<List<Category>>
    fun getByType(type: CategoryType): Flow<List<Category>>
    suspend fun seedDefaultCategories(): List<Category>
}
