package com.sinop.sist.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sinop.sist.SistApplication
import com.sinop.sist.domain.model.Category
import com.sinop.sist.domain.model.CategoryType
import com.sinop.sist.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CategoriesState())
    val state: StateFlow<CategoriesState> = _state.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        categoryRepository.getAll().onEach { categories ->
            _state.value = _state.value.copy(categories = categories)
        }.launchIn(viewModelScope)
    }

    fun addCategory(name: String, type: CategoryType, iconName: String?, colorHex: String?) {
        viewModelScope.launch {
            categoryRepository.insert(
                Category(
                    name = name,
                    type = type,
                    iconName = iconName,
                    colorHex = colorHex
                )
            )
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.update(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.delete(category)
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = SistApplication.instance
                    ?: throw IllegalStateException("Application not initialized")
                return CategoriesViewModel(app.container.categoryRepository) as T
            }
        }
    }
}

data class CategoriesState(
    val categories: List<Category> = emptyList()
)
