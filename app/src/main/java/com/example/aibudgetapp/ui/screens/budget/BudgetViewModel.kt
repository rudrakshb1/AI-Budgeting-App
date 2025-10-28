package com.example.aibudgetapp.ui.screens.budget

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    var filteredBudget by mutableStateOf<List<Budget>>(emptyList())
        private set

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
        amount: Double,
        checked: Boolean,
        startDate: String?,
        endDate: String?
    ) {
        //Basic validation
        budgetError = false
        budgetSuccess = false
        errorMessage = null
        if (amount <= 0 || name.isBlank()) {
            errorMessage = "Please enter an amount greater than 0."
            budgetError = true
            budgetSuccess = false
            return
        }

        //Date validation
        if (endDate.equals("Invalid date")) {
            errorMessage = "Please enter a valid date in the format yyyy-MM-dd (e.g., 2025-01-23)."
            budgetError = true
            budgetSuccess = false
            return
        }

        //Category validation — only for Weekly/Monthly
        if (!chosentype.equals("Yearly", ignoreCase = true) && chosencategory.isBlank()) {
            errorMessage = "Please select a category."
            budgetError = true
            budgetSuccess = false
            return
        }

        //Normalize category for Yearly
        val catOrNull: String? =
            if (chosentype.equals("Yearly", ignoreCase = true)) null
            else chosencategory.ifBlank { null }

        //Create and save budget
        val budget = Budget(
            id = "",
            name = name,
            chosenType = chosentype,
            chosenCategory = catOrNull,
            amount = amount,
            checked = checked,
            startDate = startDate,
            endDate = endDate
        )

        addBudget(budget)
    }


    fun fetchBudgets() {
        isLoading = true
        budgetError = false

        repository.getBudgets(
            onSuccess = { list ->
                budgets = list
                isLoading = false
                filterBudgetByCategory("")
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


    fun filterBudgetByCategory(category: String) {
        filteredBudget = budgets
            .filter { b ->
                if (category.isBlank()) {
                    true
                } else {
                    b.chosenCategory?.contains(category, ignoreCase = true) == true ||
                            b.startDate?.contains(category, ignoreCase = true) == true ||
                            b.endDate?.contains(category, ignoreCase = true) == true ||
                            b.name?.contains(category, ignoreCase = true) == true ||
                            b.chosenType?.contains(category, ignoreCase = true) == true
                }
            }
            .sortedBy { it.name }
    }
    fun updateBudget(budget: Budget){
        repository.updateBudget(
            budget = budget,
            onSuccess = {
                budgetSuccess = true
                budgetError = false
                fetchBudgets()
            },
            onFailure = { e ->
                errorMessage = e.message
                budgetError = true
                budgetSuccess = false
            }
        )
    }



    fun getBudgetPieChartData(
        budget: Budget,
        transactions: List<Transaction>
    ): List<Pair<String, Float>> {
        if (budget.startDate.isNullOrBlank() || budget.endDate.isNullOrBlank()) {
            return listOf("Spent" to 0f, "Remaining" to budget.amount.toFloat())
        }

        val start = LocalDate.parse(budget.startDate)
        val end = LocalDate.parse(budget.endDate)

        // common date filter
        val inRange = transactions.filter { t ->
            val d = LocalDate.parse(t.date)
            d >= start && d <= end
        }

        val filtered = if (budget.chosenType.equals("Yearly", ignoreCase = true)) {
            inRange                                // ← all txns for Yearly
        } else {
            inRange.filter { it.category == budget.chosenCategory } // ← existing behavior
        }

        val spent = filtered.sumOf { it.amount ?: 0.0 }
        val remaining = (budget.amount - spent).coerceAtLeast(0.0)

        return if (spent > budget.amount) {
            listOf("Budget" to budget.amount.toFloat(),
                "Overspent" to (spent - budget.amount).toFloat())
        } else {
            listOf("Spent" to spent.toFloat(),
                "Remaining" to remaining.toFloat())
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