package com.example.aibudgetapp.ui.screens.screenContainer

import androidx.compose.runtime.Composable
import com.example.aibudgetapp.ui.screens.transaction.Transaction
import com.example.aibudgetapp.ui.screens.transaction.AddTransactionViewModel
import com.example.aibudgetapp.ui.screens.transaction.BankTransactionCategoryDialog

@Composable
fun BankStatementUploadScreen(
    transactions: List<Transaction>,
    viewModel: AddTransactionViewModel
) {
    transactions.forEach { tx ->
        BankTransactionCategoryDialog(
            transaction = tx,
            onCategorySelected = { category ->
                tx.category = category
                viewModel.addTransaction(tx)
            }
        )
    }
}
