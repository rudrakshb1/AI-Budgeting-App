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
import java.util.UUID



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
    var transactionSaved by mutableStateOf(false)
        private set

    fun resetSavedFlag() {
        transactionSaved = false
    }


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
            transactionSaved = true
        }
        showCategoryDialog = false
        ocrResult = null
    }
    fun addTransaction(t: Transaction) {
        Log.d("CSV_IMPORT", "Saving transaction to repository: $t")
        repository.addTransaction(
            transaction = t,
            onSuccess = {
                Log.d("CSV_IMPORT", "Successfully saved: $t")
                fetchTransactions() // Ensure refetch
            },
            onFailure = {
                Log.e("CSV_IMPORT", "Failed to save: $t")
            }
        )
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


    // Just parse here:
    fun handleOcrResult(ocrText: String, documentType: String) {
        if (documentType == "receipt") {
            showCategoryDialog = true
            // Set ocrResult = parsed receipt data
        } else if (documentType == "bank_statement") {
            importBankStatement(ocrText) // instantly parse and save!
        }
    }

    fun importBankStatement(ocrText: String) {
        val list = parseBankStatement(ocrText)
        Log.d("BANK_IMPORT", "Total transactions parsed: ${list.size}")
        list.forEach { tx ->
            addTransaction(tx)
        }
        fetchTransactions()
    }



    // Just parses CSV/OCR text
    fun parseBankStatement(ocrText: String): List<Transaction> {
        val lines = ocrText.lines().drop(1) // skip header
        return lines.mapNotNull { line ->
            val parts = line.split(",")
            // IMPORTANT: Only include these five fields, just like manual/receipt!
            if (parts.size >= 4) {
                val id = UUID.randomUUID().toString()   // Ensure unique id
                val date = parts[0].trim()
                val description = parts[1].trim()
                val amount = parts[2].toDoubleOrNull() ?: 0.0
                val category = ""  // (you can categorize later if needed)

                // This matches what's stored in Firestore for all other transaction types
                Transaction(
                    id = id,
                    description = description,
                    amount = amount,
                    category = category,
                    date = date
                )
            } else null
        }
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