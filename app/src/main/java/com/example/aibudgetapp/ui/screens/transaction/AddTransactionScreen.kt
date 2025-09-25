package com.example.aibudgetapp.ui.screens.transaction


import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.aibudgetapp.ui.components.UploadPhotoButton
import java.time.LocalDate
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResult
import com.yalantis.ucrop.UCrop
import androidx.compose.runtime.saveable.rememberSaveable
import android.app.Activity



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onReceiptPicked: (Uri) -> Unit = {}     // new parameter
) {
    val addTransactionViewModel = remember { AddTransactionViewModel(TransactionRepository()) }
    val transactionError by remember { derivedStateOf { addTransactionViewModel.transactionError } }
    val transactionSuccess by remember { derivedStateOf { addTransactionViewModel.transactionSuccess } }

    val categories = listOf("Food & Drink", "Rent", "Gas", "Other")
    var selected by remember { mutableStateOf(categories[0]) }
    var amount by remember { mutableStateOf(0.0) }
    var isExpanded by remember { mutableStateOf(false) }
    var transactionDate by remember { mutableStateOf(LocalDate.now().toString()) }

    var receiptUri by remember { mutableStateOf<Uri?>(null) }
    var croppingInProgress by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    // --- UCrop Launcher at screen level! ---
    val cropLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        croppingInProgress = false
        println("UCrop result handler called: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val resultUri = UCrop.getOutput(result.data!!)
            println("UCrop OK resultUri: $resultUri")
            if (resultUri != null) {
                onReceiptPicked(resultUri) // For external handler, e.g., navigation, preview, etc.
                receiptUri = resultUri     // For this screen
                addTransactionViewModel.runOcr(resultUri, context)
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR && result.data != null) {
            val cropError = UCrop.getError(result.data!!)
            println("UCrop error: $cropError")
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            println("UCrop canceled")
        }
    }


    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(text = "Add Transaction", style = MaterialTheme.typography.bodyLarge)

        UploadPhotoButton(
            onImagePicked = { uri ->
                // addTransactionViewModel.onReceiptSelected(uri)
                onReceiptPicked(uri)                               // ✅ navigate to ReceiptFlow
                receiptUri = uri
            },
            addTxViewModel = addTransactionViewModel
        )

        if (receiptUri != null) {
            Text(
                text = "Receipt attached",
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // everything below stays the same (manual transaction form, list, etc.)
        OutlinedTextField(
            value = transactionDate,
            onValueChange = {
                transactionDate = it
                addTransactionViewModel.transactionSuccess = false
            },
            label = { Text("Date (yyyy-mm-dd)") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = amount.toString(),
            onValueChange = {
                amount = it.toDoubleOrNull() ?: 0.0
                addTransactionViewModel.transactionSuccess = false
            },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
        )

        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = !isExpanded },
        ) {
            TextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                value = selected,
                onValueChange = { addTransactionViewModel.transactionSuccess = false },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) }
            )
            ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                categories.forEach { text ->
                    DropdownMenuItem(
                        text = { Text(text = text) },
                        onClick = {
                            selected = text
                            isExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Text(text = "Currently selected: $selected")

        // ✅ Only show manual add flow if no OCR result is waiting
        if (addTransactionViewModel.ocrResult == null) {
            Button(
                onClick = {
                    addTransactionViewModel.onAddTransaction(
                        description = "",
                        amount = amount,
                        category = selected,
                        date = transactionDate
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Save")
            }

            if (transactionError) {
                Text(
                    text = "Failed to add transaction",
                    color = Color.Red,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            if (transactionSuccess) {
                transactionDate = LocalDate.now().toString()
                amount = 0.0
                Text(
                    text = "Transaction Saved",
                    color = Color.Green,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }

    }
}