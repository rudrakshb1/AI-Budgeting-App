package com.example.aibudgetapp.ui.screens.budget

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BudgetViewModel(
    private val repository: BudgetRepository): ViewModel() {

    var budgetError by mutableStateOf(false)
        private set

    var budgetSuccess by mutableStateOf(false)

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var budgets by mutableStateOf<List<Budget>>(emptyList())
        private set

    val _budgetList = MutableLiveData<List<Budget>>()

    val budgetList: LiveData<List<Budget>> = _budgetList

    fun addBudget(b: Budget) {
        repository.addBudget(
            budget = b,
            onSuccess = { budgetError = false; budgetSuccess = true },
            onFailure = { e -> errorMessage = e.message; budgetError = true }
        )
    }

    fun onAddBudget(name: String, chosentype: String, chosencategory: String, amount: Int, checked: Boolean){
        if (amount <= 0 || name.isBlank()) {
            budgetError = true
        } else{
            val budget = Budget(
                id = "",
                name = name,
                // selectedDate = selecteddate,
                chosenType = chosentype,
                chosenCategory = chosencategory,
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

    fun deleteBudget(id: String) {
        repository.deleteBudget(
            id = id,
            onSuccess = { fetchBudgets() },
            onFailure = { budgetError = true }
        )
    }
    fun fetchbudgetcategory(category: String){
        repository.getbudgetcategory(
            category = category,
            onSuccess = {budgets ->
                _budgetList.value = budgets
            },
            onFailure = { e ->
                budgetError = true
            }
        )
    }

}