package com.example.aibudgetapp.ui.screens.transaction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class AddTransactionViewModel : ViewModel() {
    var amount by mutableIntStateOf(0)
        private set

    var category by mutableStateOf("Food & Drink")
        private set

    var receiptUri by mutableStateOf<String?>(null)
        private set

    fun onAmountChange(input: String) {
        amount = input.toIntOrNull() ?: 0
    }

    fun onCategoryChange(value: String) {
        category = value
    }

    fun onReceiptSelected(uri: Uri) {
        receiptUri = uri.toString()
    }
}