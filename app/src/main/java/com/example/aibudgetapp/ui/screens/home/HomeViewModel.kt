package com.example.aibudgetapp.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.aibudgetapp.ui.screens.budget.BudgetRepository
import com.example.aibudgetapp.ui.screens.transaction.TransactionRepository
import java.time.YearMonth

class HomeViewModel(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
): ViewModel() {

    var budgetError by mutableStateOf(false)
        private set

    var budgetAmount by mutableStateOf(0)
        private set

    var transactionError by mutableStateOf(false)
        private set

    var totalSpent by mutableStateOf(0.0)
        private set

    fun getMonthlyBudget() {
        budgetError = false
        budgetRepository.getBudgets(
            onSuccess = { list ->
                budgetAmount = list
                    .filter { it.chosentype.equals("monthly", ignoreCase = true) }
                    .sumOf { it.amount }
            },
            onFailure = { e ->
                budgetError = true
            }
        )
    }

    fun getMonthlyTransaction() {
        val ymPrefix = YearMonth.now().toString()

        transactionError = false
        transactionRepository.getTransactions(
            onSuccess = { list ->
                totalSpent = list
                    .filter { it.date.contains(ymPrefix, ignoreCase = true) }
                    .sumOf { it.amount }
            },
            onFailure = { e ->
                transactionError = true
            }
        )
    }




}
