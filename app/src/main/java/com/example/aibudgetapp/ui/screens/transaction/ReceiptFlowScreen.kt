package com.example.aibudgetapp.ui.screens.transaction

import android.net.Uri
import androidx.compose.runtime.*
import com.example.aibudgetapp.ocr.ReceiptOcrScreen
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import java.io.File
import java.io.FileOutputStream
import android.util.Log

@Composable
fun ReceiptFlowScreen(
    imageUri: Uri,
    addTransactionViewModel: AddTransactionViewModel,
    onComplete: () -> Unit = {}
) {
    val ocrResult = addTransactionViewModel.ocrResult
    val showCategoryDialog = addTransactionViewModel.showCategoryDialog
    val transactionSaved = addTransactionViewModel.transactionSaved
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
                // Try Firebase upload first, fallback to local save if Firebase unavailable
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference.child("receipts/${UUID.randomUUID()}")

                storageRef.putFile(imageUri)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { url ->
                            addTransactionViewModel.onSaveTransaction(selectedCategory, url.toString())
                            Toast.makeText(context, "Transaction saved!", Toast.LENGTH_SHORT).show()
                            onComplete()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Firebase upload failed — fallback to local image save
                        Log.e("FirebaseUpload", "Upload failed, saving locally instead: ${e.message}")
                        val localPath = try {
                            val inputStream = context.contentResolver.openInputStream(imageUri)
                            val file = File(context.filesDir, "${UUID.randomUUID()}.jpg")
                            val outputStream = FileOutputStream(file)
                            inputStream?.use { input ->
                                outputStream.use { output ->
                                    input.copyTo(output)
                                }
                            }
                            file.absolutePath
                        } catch (ex: Exception) {
                            Log.e("LOCAL_SAVE", "Failed to save image locally", ex)
                            null
                        }

                        if (localPath != null) {
                            addTransactionViewModel.onSaveTransaction(selectedCategory, localPath)
                            Toast.makeText(context, "Saved locally!", Toast.LENGTH_SHORT).show()
                            onComplete()
                        } else {
                            Toast.makeText(context, "Failed to save receipt image", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        )
    } else {
        ReceiptOcrScreen()
    }
}
