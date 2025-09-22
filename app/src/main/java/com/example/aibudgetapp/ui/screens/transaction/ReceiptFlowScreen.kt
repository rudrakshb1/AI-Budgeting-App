package com.example.aibudgetapp.ui.screens.transaction

import android.net.Uri
import androidx.compose.runtime.*
import com.example.aibudgetapp.ocr.ReceiptOcrScreen
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.aibudgetapp.ocr.ParsedReceipt
import android.widget.Toast

@Composable
fun ReceiptFlowScreen(
    imageUri: Uri,
    addTransactionViewModel: AddTransactionViewModel,
    onComplete: () -> Unit = {}
) {
    val ocrResult = addTransactionViewModel.ocrResult
    val showCategoryDialog = addTransactionViewModel.showCategoryDialog
    val transactionSaved = addTransactionViewModel.transactionSaved // <-- ADD THIS
    val context = LocalContext.current

    // Launch OCR job via ViewModel (lifecycle-safe)
    LaunchedEffect(imageUri) {
        addTransactionViewModel.runOcr(imageUri, context)
    }

    // Show Toast when transaction saved
    if (transactionSaved) {
        LaunchedEffect(transactionSaved) {
            Toast.makeText(context, "Transaction saved!", Toast.LENGTH_SHORT).show()
            addTransactionViewModel.resetSavedFlag()
        }
    }

    if (showCategoryDialog && ocrResult != null) {
        CategoryDialog(
            merchant = ocrResult.merchant,
            total = ocrResult.total,
            date = ocrResult.dateEpochMs,
            addTransactionViewModel = addTransactionViewModel,
            onSaveComplete = { selectedCategory ->
                addTransactionViewModel.onSaveTransaction(selectedCategory, imageUri)
                Toast.makeText(context, "Transaction saved!", Toast.LENGTH_SHORT).show()
                onComplete()
            }
        )
    } else {
        ReceiptOcrScreen()
    }
}
