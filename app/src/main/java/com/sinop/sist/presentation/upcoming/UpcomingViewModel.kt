package com.sinop.sist.presentation.upcoming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sinop.sist.SistApplication
import com.sinop.sist.domain.model.Debt
import com.sinop.sist.domain.model.DebtDirection
import com.sinop.sist.domain.model.Installment
import com.sinop.sist.domain.repository.DebtRepository
import com.sinop.sist.domain.repository.InstallmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate

class UpcomingViewModel(
    debtRepository: DebtRepository,
    installmentRepository: InstallmentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UpcomingState())
    val state: StateFlow<UpcomingState> = _state.asStateFlow()

    init {
        val today = LocalDate.now()
        val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())

        combine(
            debtRepository.getDueBetween(today, endOfMonth),
            installmentRepository.getActiveUntil(endOfMonth)
        ) { debts, installments ->
            val debtTotal = debts.filter { !it.isPaid }.sumOf { it.amount }
            val installmentTotal = installments.filter { !it.isCompleted }.sumOf { it.monthlyAmount }
            _state.value = UpcomingState(
                debts = debts.filter { !it.isPaid },
                installments = installments.filter { !it.isCompleted },
                totalDue = debtTotal + installmentTotal
            )
        }.launchIn(viewModelScope)
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = SistApplication.instance
                    ?: throw IllegalStateException("Application not initialized")
                return UpcomingViewModel(
                    app.container.debtRepository,
                    app.container.installmentRepository
                ) as T
            }
        }
    }
}

data class UpcomingState(
    val debts: List<Debt> = emptyList(),
    val installments: List<Installment> = emptyList(),
    val totalDue: Double = 0.0
)
