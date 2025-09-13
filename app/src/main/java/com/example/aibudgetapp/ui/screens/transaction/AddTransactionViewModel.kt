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
import com.example.aibudgetapp.ocr.ParsedReceipt
import android.util.Log

class AddTransactionViewModel(
    private val repository: TransactionRepository) : ViewModel() {
    var amount by mutableStateOf(0.0)
        private set

    var category by mutableStateOf("Food & Drink")
        private set

    var receiptUri by mutableStateOf<String?>(null)
        private set

    var transactionError by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var transactions by mutableStateOf<List<Transaction>>(emptyList())
        private set

    fun onAmountChange(input: String) {
        amount = (input.toDoubleOrNull() ?: 0) as Double
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
        if (amount <= 0.0 || category.isBlank()) {
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

    fun fetchTransactions() {
        isLoading = true
        transactionError = false

        repository.getTransactions(
            onSuccess = { list ->
                transactions = list
                isLoading = false
            },
            onFailure = {
                isLoading = false
                transactionError = true
            }
        )
    }

    fun addFromOcr(
        merchant: String,
        total: Double,
        rawText: String,
        imageUri: Uri
    ) {
        val tx = Transaction(
            id = "",
            description = merchant.ifBlank { "Unknown" },
            amount = total,
            category = "Uncategorized" // later: call AutoCategorizer here
        )

        repository.addTransaction(
            transaction = tx,
            onSuccess = { fetchTransactions() },
            onFailure = { transactionError = true }
        )
    }

    fun addFromParsed(parsed: ParsedReceipt, imageUri: Uri) {
        // Debug log to check what OCR extracted
        Log.d("VIEWMODEL", "Saving Transaction: merchant=${parsed.merchant}, total=${parsed.total}")

        addFromOcr(
            merchant = parsed.merchant,
            total = parsed.total,
            rawText = parsed.rawText,
            imageUri = imageUri
        )
    }





}