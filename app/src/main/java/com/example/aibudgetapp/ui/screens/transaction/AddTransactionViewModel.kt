package com.example.aibudgetapp.ui.screens.transaction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.aibudgetapp.ocr.ParsedReceipt
import android.util.Log
import java.time.LocalDate
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.content.Context
import com.example.aibudgetapp.constants.CategoryType
import com.example.aibudgetapp.ocr.ReceiptOcr
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.io.File
import java.io.FileOutputStream

enum class Period { WEEK, MONTH }

class AddTransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {
    var transactions: List<Transaction> by mutableStateOf(emptyList())
        private set

    var amount: Double by mutableStateOf(0.0)
        private set

    var category: String by mutableStateOf(CategoryType.entries.first().value)
        private set

    var receiptUri: String? by mutableStateOf(null)
        private set

    var transactionError: Boolean by mutableStateOf(false)
        private set

    var transactionSuccess: Boolean by mutableStateOf(false)

    var isLoading: Boolean by mutableStateOf(false)
        private set

    var ocrResult: ParsedReceipt? by mutableStateOf(null)
        private set

    var showCategoryDialog: Boolean by mutableStateOf(false)
        private set

    var transactionSaved: Boolean by mutableStateOf(false)
        private set

    var selectedPeriod: Period by mutableStateOf(Period.WEEK)
        private set


    private val _spendingByCategory = MutableStateFlow<Map<String, Double>>(emptyMap())
    val spendingByCategory: StateFlow<Map<String, Double>> = _spendingByCategory

    init {
        fetchTransactions()
    }


    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "${UUID.randomUUID()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath  // return saved file path
        } catch (e: Exception) {
            Log.e("LOCAL_SAVE", "Failed to save image locally", e)
            null
        }
    }


    // Call this whenever transaction list changes
    fun updateSpendingByCategory() {
        _spendingByCategory.value = getSpendingByCategory()
    }

    fun getSpendingByCategoryFlow(): Flow<Map<String, Double>> {
        return flowOf(
            transactions.groupBy { it.category }
                .mapValues { entry ->
                    entry.value.fold(0.0) { acc, tx -> acc + (tx.amount ?: 0.0) }
                }
        )
    }

    fun setPeriod(period: Period) {
        selectedPeriod = period
    }

    fun getTransactionsInPeriod(): List<Transaction> {
        val now = LocalDate.now()
        return when (selectedPeriod) {
            Period.WEEK -> transactions.filter { isInCurrentWeek(it.date, now) }
            Period.MONTH -> transactions.filter { isInCurrentMonth(it.date, now) }
        }
    }

    private fun isInCurrentWeek(dateString: String, now: LocalDate): Boolean {
        val txDate = LocalDate.parse(dateString)
        val weekStart = now.minusDays(now.dayOfWeek.value.toLong() - 1)
        val weekEnd = weekStart.plusDays(6)
        return !txDate.isBefore(weekStart) && !txDate.isAfter(weekEnd)
    }

    private fun isInCurrentMonth(dateString: String, now: LocalDate): Boolean {
        val txDate = LocalDate.parse(dateString)
        return txDate.month == now.month && txDate.year == now.year
    }

    fun getSpendingByCategory(): Map<String, Double> {
        return getTransactionsInPeriod()
            .groupBy { it.category.ifBlank { "Uncategorized" } }
            .mapValues { entry -> entry.value.sumOf { tx -> tx.amount ?: 0.0 } }
    }

    fun onAmountChange(input: String) { amount = input.toDoubleOrNull() ?: 0.0 }
    fun onCategoryChange(value: String) { category = value }

    fun onReceiptSelected(uri: Uri, context: Context) {
        val localPath = saveImageToInternalStorage(context, uri)
        receiptUri = localPath
    }

    fun resetSavedFlag() { transactionSaved = false }

    fun onSaveTransaction(category: String, receiptUrl: String?) {
        if (transactionSaved) {
            Log.w("DUPLICATE_CHECK", "Blocked duplicate save")
            return
        }
        transactionSaved = true
        Log.d("DUPLICATE_CHECK", "Saving transaction started")

        ocrResult?.let { parsed ->
            val transaction = Transaction(
                id = "",
                description = parsed.merchant.ifBlank { "Unknown" },
                amount = parsed.total,
                category = category,
                date = LocalDate.now().toString(),
                receiptUrl = receiptUrl
            )
            addTransaction(transaction)
        }

        showCategoryDialog = false
        ocrResult = null
    }

    fun addTransaction(t: Transaction) {
        Log.d("Add_Transaction", "Saving transaction to repository: $t")
        repository.addTransaction(
            transaction = t,
            onSuccess = {
                Log.d("Add_Transaction", "Successfully saved: $t")
                fetchTransactions()
                updateSpendingByCategory() // NEW: call update after every change
                transactionSuccess = true
            },
            onFailure = { e ->
                Log.e("Add_Transaction", "Failed to save: $t, error=$e")
                transactionError = true
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

    fun handleOcrResult(ocrText: String, documentType: String) {
        if (documentType == "receipt") {
            showCategoryDialog = true
        } else if (documentType == "bank_statement") {
            importBankStatement(ocrText)
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

    fun parseBankStatement(ocrText: String): List<Transaction> {
        val lines = ocrText.lines().drop(1)
        return lines.mapNotNull { line ->
            val parts = line.split(",")
            if (parts.size >= 4) {
                val id = UUID.randomUUID().toString()
                val date = parts[0].trim()
                val description = parts[1].trim()
                val amount = parts[2].toDoubleOrNull() ?: 0.0
                val category = ""
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
                updateSpendingByCategory()
                isLoading = false
            },
            onFailure = { e ->
                isLoading = false
                transactionError = true
            }
        )
    }

    fun addFromOcr(merchant: String, total: Double, rawText: String, imageUri: Uri) {
        ocrResult = ParsedReceipt(
            merchant = merchant.ifBlank { "Unknown" },
            total = total,
            dateEpochMs = System.currentTimeMillis(),
            rawText = rawText
        )
        showCategoryDialog = true
    }

    fun addFromParsed(parsed: ParsedReceipt, imageUri: Uri) {
        Log.d("VIEWMODEL", "Parsed Receipt: merchant=${parsed.merchant}, total=${parsed.total}")
        ocrResult = parsed
        showCategoryDialog = true
    }

    fun importTransactions(transactions: List<Transaction>) {
        transactions.forEach { tx ->
            repository.addTransaction(
                transaction = tx,
                onSuccess = {
                    Log.d("CSV_IMPORT", "Inserted txn: $tx")
                    fetchTransactions()
                    updateSpendingByCategory()
                },
                onFailure = { e ->
                    Log.e("CSV_IMPORT", "Failed insert: ${e.message}")
                    transactionError = true
                }
            )
        }
    }

    fun deleteTransaction(id: String) {
        repository.deleteTransaction(
            id = id,
            onSuccess = {
                fetchTransactions()
                updateSpendingByCategory()
            },
            onFailure = { e -> transactionError = true }
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
