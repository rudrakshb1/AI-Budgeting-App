package com.example.aibudgetapp.ui.screens.transaction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.components.Description


class AddTransactionViewModel(
    private val repository: TransactionRepository) : ViewModel() {
    var amount by mutableIntStateOf(0)
        private set

    var category by mutableStateOf("Food & Drink")
        private set

    var receiptUri by mutableStateOf<String?>(null)
        private set

    var transactionError by mutableStateOf(false)
        private set

    fun onAmountChange(input: String) {
        amount = input.toIntOrNull() ?: 0
    }

    fun onCategoryChange(value: String) {
        category = value
    }

    fun onReceiptSelected(uri: Uri) {
        receiptUri = uri.toString()
    }

    fun addTransaction(t: Transaction){
        repository.addTransaction(
            transaction = t,
            onSuccess = {transactionError = false},
            onFailure = {transactionError = true}
        )
    }

    fun onAddTransaction(id: String, description: String, amount: Double, category: String) {
        if (amount <= 0 || category.isBlank()) {
            transactionError = true
        } else {
            val transaction = Transaction(
                id = id,
                description = description,
                amount = amount,
                category = category
            )
            addTransaction(t = transaction)
        }
    }
}