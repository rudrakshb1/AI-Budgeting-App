package com.example.aibudgetapp.ocr

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp


@Composable
fun ReceiptOcrScreen(
    imageUri: Uri,
    addTransactionViewModel: AddTransactionViewModel = viewModel(),
    onOcrComplete: () -> Unit = {}   // callback to return after saving
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 🔹 Automatically run OCR once when the screen opens
    LaunchedEffect(imageUri) {
        scope.launch {
            try {
                val parsed = ReceiptOcr.extract(imageUri, context)
                Log.d("OCR_SCREEN", "ParsedReceipt = $parsed")

                addTransactionViewModel.addFromParsed(parsed, imageUri)

                Toast.makeText(
                    context,
                    "OCR saved: ${parsed.total}",
                    Toast.LENGTH_LONG
                ).show()

                // After saving, navigate back (e.g. to Home)
                onOcrComplete()
            } catch (e: Exception) {
                Log.e("OCR_SCREEN", "OCR failed: ${e.message}", e)
                Toast.makeText(
                    context,
                    "OCR failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                onOcrComplete()
            }
        }
    }

    // Simple placeholder UI while OCR runs
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Scanning receipt...", style = MaterialTheme.typography.titleLarge)
    }
}
