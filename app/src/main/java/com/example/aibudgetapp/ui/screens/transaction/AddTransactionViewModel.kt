package com.example.aibudgetapp.ui.screens.transaction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.aibudgetapp.ocr.ParsedReceipt
import android.util.Log
import java.time.LocalDate
import com.example.aibudgetapp.ocr.AutoCategorizer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.content.Context
import com.example.aibudgetapp.ocr.ReceiptOcr



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
    var ocrResult by mutableStateOf<ParsedReceipt?>(null)
        private set
    var showCategoryDialog by mutableStateOf(false)
        private set

    fun onSaveTransaction(category: String, imageUri: Uri) {
        ocrResult?.let { parsed ->
            val transaction = Transaction(
                id = "",
                description = parsed.merchant.ifBlank { "Unknown" },
                amount = parsed.total,
                category = category,
                date = LocalDate.now().toString()
            )
            addTransaction(t = transaction)
        }
        showCategoryDialog = false
        ocrResult = null
    }

    fun runOcr(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val parsed = ReceiptOcr.extract(uri, context)
                ocrResult = parsed
                showCategoryDialog = true
            } catch (e: Exception) {
                transactionError = true
            }
        }
    }


    fun addTransaction(t: Transaction){
        repository.addTransaction(
            transaction = t,
            onSuccess = {transactionError = false},
            onFailure = {transactionError = true}
        )
    }

    fun onAddTransaction(description: String, amount: Double, category: String, date: String) {
        if (amount <= 0.0 || category.isBlank()) {
            transactionError = true
        } else {
            val transaction = Transaction(
                id = "",
                description = description,
                amount = amount,
                category = category,
                date = date
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
        imageUri: Uri,
    ) {
        val tx = Transaction(
            id = "",
            description = merchant.ifBlank { "Unknown" },
            amount = total,
            category = "Uncategorized", // later: call AutoCategorizer here,
            date = LocalDate.now().toString()
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

    fun deleteTransaction(id: String) {
        repository.deleteTransaction(
            id = id,
            onSuccess = { fetchTransactions() },
            onFailure = { transactionError = true }
        )
    }

}

class AddTransactionViewModelFactory(
    private val repository: TransactionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddTransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddTransactionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}