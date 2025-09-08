package com.example.aibudgetapp.ui.screens.budget

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.aibudgetapp.ui.screens.transaction.Transaction

class BudgetViewModel(
    private val repository: BudgetRepository): ViewModel() {

    var budgetError by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var budgets by mutableStateOf<List<Budget>>(emptyList())
        private set

    fun addBudget(b: Budget) {
        repository.addBudget(
            budget = b,
            onSuccess = { budgetError = false },
            onFailure = { e -> errorMessage = e.message; budgetError = true }
        )
    }

    fun onAddBudget(name: String, selecteddate: Int, chosentype: String, chosencategory: String, amount: Int, checked: Boolean){
        if (amount <= 0 || name.isBlank()) {
            budgetError = true
        } else{
            val budget = Budget(
                name = name,
                selecteddate = selecteddate,
                chosentype = chosentype,
                chosencategory = chosencategory,
                amount = amount,
                checked = checked
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
                isLoading = false
            },
            onFailure = { e ->
                isLoading = false
                budgetError = true
            }
        )
    }

}