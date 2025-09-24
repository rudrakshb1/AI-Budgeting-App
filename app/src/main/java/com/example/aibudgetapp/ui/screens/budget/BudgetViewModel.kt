package com.example.aibudgetapp.ui.screens.budget

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import com.example.aibudgetapp.ui.screens.transaction.Transaction
import androidx.lifecycle.ViewModelProvider


class BudgetViewModel(
    private val repository: BudgetRepository
) : ViewModel() {

    var budgetError by mutableStateOf(false)
        private set

    var budgetSuccess by mutableStateOf(false)

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    // existing mutableState list
    var budgets by mutableStateOf<List<Budget>>(emptyList())
        private set

    // LiveData-backed list
    private val _budgetList = MutableLiveData<List<Budget>>()
    val budgetList: LiveData<List<Budget>> = _budgetList

    fun addBudget(b: Budget) {
        repository.addBudget(
            budget = b,
            onSuccess = { budgetError = false; budgetSuccess = true },
            onFailure = { e -> errorMessage = e.message; budgetError = true }
        )
    }

    fun onAddBudget(
        name: String,
        chosentype: String,
        chosencategory: String,
        amount: Int,
        checked: Boolean,
        startDate: String?,
        endDate: String?
    ) {
        if (amount <= 0 || name.isBlank()) {
            budgetError = true
        } else {
            val budget = Budget(
                id = "",
                name = name,
                chosenType = chosentype,
                chosenCategory = chosencategory,
                amount = amount,
                checked = checked,
                startDate = startDate,
                endDate = endDate
            )
            addBudget(budget)
        }
    }

    fun fetchBudgets() {
        isLoading = true
        budgetError = false

        repository.getBudgets(
            onSuccess = { list ->
                budgets = list
                _budgetList.value = list   // 🔹 keep LiveData in sync
                isLoading = false
            },
            onFailure = { e ->
                isLoading = false
                budgetError = true
            }
        )
    }

    fun deleteBudget(id: String) {
        repository.deleteBudget(
            id = id,
            onSuccess = { fetchBudgets() },
            onFailure = { budgetError = true }
        )
    }

    fun fetchbudgetcategory(category: String) {
        repository.getbudgetcategory(
            category = category,
            onSuccess = { budgets ->
                _budgetList.value = budgets
            },
            onFailure = { e ->
                budgetError = true
            }
        )
    }

    fun getBudgetPieChartData(
        budget: Budget,
        transactions: List<Transaction>
    ): List<Pair<String, Float>> {
        val filtered = transactions.filter {
            it.category == budget.chosenCategory &&
                    !budget.startDate.isNullOrBlank() && !budget.endDate.isNullOrBlank() &&
                    LocalDate.parse(it.date) >= LocalDate.parse(budget.startDate) &&
                    LocalDate.parse(it.date) <= LocalDate.parse(budget.endDate)
        }
        val spent = filtered.sumOf { it.amount ?: 0.0 }
        val remaining = (budget.amount - spent).coerceAtLeast(0.0)
        return if (spent > budget.amount) {
            listOf(
                "Budget" to budget.amount.toFloat(),
                "Overspent" to (spent - budget.amount).toFloat()
            )
        } else {
            listOf(
                "Spent" to spent.toFloat(),
                "Remaining" to remaining.toFloat()
            )
        }
    }

    class Factory(
        private val repository: BudgetRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return BudgetViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}