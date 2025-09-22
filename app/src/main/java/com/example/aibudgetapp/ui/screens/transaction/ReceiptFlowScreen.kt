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

@Composable
fun ReceiptFlowScreen(
    imageUri: Uri,
    addTransactionViewModel: AddTransactionViewModel,
    onComplete: () -> Unit = {}
) {
    val ocrResult = addTransactionViewModel.ocrResult
    val showCategoryDialog = addTransactionViewModel.showCategoryDialog
    val context = LocalContext.current

    // Launch OCR job via ViewModel (lifecycle-safe)
    LaunchedEffect(imageUri) {
        addTransactionViewModel.runOcr(imageUri, context)
    }

    if (showCategoryDialog && ocrResult != null) {
        CategoryDialog(
            merchant = ocrResult.merchant,
            total = ocrResult.total,
            date = ocrResult.dateEpochMs,
            addTransactionViewModel = addTransactionViewModel,
            onSaveComplete = { selectedCategory ->
                addTransactionViewModel.onSaveTransaction(selectedCategory, imageUri)
                onComplete()
            }
        )
    }
}
