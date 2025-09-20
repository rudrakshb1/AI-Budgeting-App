package com.example.aibudgetapp.ui.screens.transaction

import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aibudgetapp.ocr.ReceiptOcrScreen

@Composable
fun ReceiptFlowScreen(
    imageUri: Uri,
    addTransactionViewModel: AddTransactionViewModel = viewModel(),
    onComplete: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var merchant by remember { mutableStateOf("") }
    var total by remember { mutableStateOf(0.0) }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }

    // Step 1: Run OCR
    ReceiptOcrScreen(
        imageUri = imageUri,
        onOcrComplete = { /* optional navigation back */ },
        onCategoryDetected = { _, m, t, _, _ ->
            merchant = m
            total = t
            date = System.currentTimeMillis()
            showDialog = true
        }
    )

    // Step 2: Show category dialog after OCR
    if (showDialog) {
        CategoryDialog(
            merchant = merchant,
            total = total,
            date = date,
            addTransactionViewModel = addTransactionViewModel,
            onSaveComplete = {
                showDialog = false
                onComplete()
            }
        )
    }
}
