package com.example.aibudgetapp.ui.screens.screenContainer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ScreenContainerViewModel {
    var currentScreen by mutableStateOf(Screen.HOME)
        private set

    fun navigateTo(screen: Screen) {
        currentScreen = screen
    }
    var addTransactionError by mutableStateOf(false)
        private set

    var budgetError by mutableStateOf(false)
        private set
    var budgetErrorMessage by mutableStateOf<String?>(null)
        private set

    fun addTransaction(amount: Double, category: String) {
        if (amount <= 0 || category.isBlank()) {
            addTransactionError = true
        } else {
            addTransactionError = false
        }
    }
    fun addBudget(name: String, selecteddate: Int, chosentype: String, chosencategory: String, amount: Int, checked: Boolean){
        if (amount <= 0 || name.isBlank()) {
            budgetError = true
        } else {
            budgetError = false
        }
    }
}